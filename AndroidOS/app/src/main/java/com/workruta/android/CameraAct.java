package com.workruta.android;

import androidx.annotation.NonNull;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.workruta.android.Utils.Functions;
import com.workruta.android.Utils.RandomString;
import com.workruta.android.Utils.StorageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class CameraAct extends SharedCompatActivity {

    Context context;
    RelativeLayout previewLayer;
    TextureView textureView;
    ImageView imagePreview;
    ImageButton captureButton, flashButton, toggleLens, doneButton, cancelButton;
    String tempDir, filePath;
    private static final SparseIntArray ORIENTATION = new SparseIntArray();
    CameraDevice cameraDevice;
    CameraCaptureSession captureSession;
    CaptureRequest.Builder captureRequestBuilder;
    private Size imageDim;
    File file;
    Handler handler;
    MediaPlayer shutterSound;
    HandlerThread handlerThread;
    int actIndex, curLens, FRONT_LENS = 1, BACK_LENS = 0;
    int[] lenses = new int[]{FRONT_LENS, BACK_LENS};
    boolean hasFrontCamera, notSet;

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AllBlack);
        setContentView(R.layout.activity_camera);
        context = this;
        notSet = true;
        hasFrontCamera = false;

        Bundle bundle = getIntent().getExtras();
        actIndex = bundle.getInt("activity");

        tempDir = StorageUtils.getStorageDirectories(context)[0] + "/Android/data/" + context.getPackageName() + "/tempFiles";
        if(!(new File(tempDir).exists()))
            new File(tempDir).mkdir();

        previewLayer = findViewById(R.id.previewLayer);
        textureView = findViewById(R.id.textureView);
        imagePreview = findViewById(R.id.imagePreview);
        captureButton = findViewById(R.id.captureButton);
        flashButton = findViewById(R.id.flashButton);
        toggleLens = findViewById(R.id.toggleLens);
        doneButton = findViewById(R.id.doneButton);
        cancelButton = findViewById(R.id.cancelButton);

        textureView.setSurfaceTextureListener(textureListener);
        captureButton.setOnClickListener((v) -> takePicture());
        cancelButton.setOnClickListener((v) -> discardPicture());
        flashButton.setOnClickListener((v) -> toggleFlash());
        doneButton.setOnClickListener((v) -> {
            if(actIndex == 0)
                ChangePhotoAct.displayImage(filePath);
            else if(actIndex == 1)
                LicenceAct.displayImage(filePath);
            else if(actIndex == 2)
                IdentityActivity.displayImage(filePath);
            cameraDevice.close();
            finish();
        });
    }

    private void discardPicture() {
        try {
            boolean flashState = Boolean.parseBoolean(flashButton.getTag().toString());
            if(file.exists()){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        Files.delete(file.toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                    file.delete();
            }
            filePath = null;
            file = null;
            imagePreview.setImageBitmap(null);
            previewLayer.setVisibility(View.INVISIBLE);
            if(flashState) {
                captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, handler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

        }
    };

    private final CameraDevice.StateCallback stateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            try {
                cameraDevice = camera;
                createCameraPreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private void createCameraPreview() throws CameraAccessException {
        SurfaceTexture texture = textureView.getSurfaceTexture();
        texture.setDefaultBufferSize(imageDim.getWidth(), imageDim.getHeight());
        Surface surface = new Surface(texture);
        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequestBuilder.addTarget(surface);
        cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(CameraCaptureSession session) {
                try {
                    if(cameraDevice == null)
                        return;
                    captureSession = session;
                    updatePreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(CameraCaptureSession session) {

            }
        }, null);
    }

    private void updatePreview() throws CameraAccessException {
        if(cameraDevice == null)
            return;
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, handler);
    }

    @SuppressLint("MissingPermission")
    private void openCamera() throws CameraAccessException {
        if(!(cameraDevice == null))
            cameraDevice.close();
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        hasFrontCamera = manager.getCameraIdList().length > 1;
        curLens = 0;
        if(actIndex == 0 && hasFrontCamera)
            curLens = 1;
        if(hasFrontCamera && notSet){
            notSet = false;
            toggleLens.setBackgroundResource(R.drawable.ic_flip_camera);
            toggleLens.setOnClickListener((v) -> {
                try {
                    curLens = lenses[curLens];
                    turnOffFlash();
                    openCamera(curLens);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            });
        }
        String cameraId = manager.getCameraIdList()[curLens];
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        imageDim = map.getOutputSizes(SurfaceTexture.class)[0];
        manager.openCamera(cameraId, stateCallBack, null);
    }

    @SuppressLint("MissingPermission")
    private void openCamera(int lensFacing) throws CameraAccessException {
        if(!(cameraDevice == null))
            cameraDevice.close();
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String cameraId = manager.getCameraIdList()[lensFacing];
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        imageDim = map.getOutputSizes(SurfaceTexture.class)[0];
        manager.openCamera(cameraId, stateCallBack, null);
    }

    private void takePicture() {
        try {
            if (cameraDevice == null)
                return;
            boolean usingFrontCamera = curLens == FRONT_LENS;
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            boolean flashState = Boolean.parseBoolean(flashButton.getTag().toString());
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            int width = 640, height = 480;
            /*if(jpegSizes != null && jpegSizes.length > 0){
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }*/
            ImageReader imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> surfaces = new ArrayList<>(2);
            surfaces.add(imageReader.getSurface());
            surfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.addTarget(imageReader.getSurface());
            builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            builder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION.get(rotation));
            if(flashState) {
                builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            }
            playShutterSound();
            @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()),
                    randStr = UUID.randomUUID().toString();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                RandomString gen = new RandomString(8, ThreadLocalRandom.current());
                randStr = gen.toString();
            }
            filePath = tempDir + "/IMG_" + randStr + timeStamp +".jpg";
            file = new File(filePath);
            ImageReader.OnImageAvailableListener listener = reader -> {
                Image image = reader.acquireLatestImage();
                ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[byteBuffer.capacity()];
                byteBuffer.get(bytes);
                try {
                    saveImage(bytes);
                    if(usingFrontCamera){
                        int halfW = width / 2;
                        int halfH = height / 2;
                        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                        Matrix matrix = new Matrix();
                        matrix.postScale(1, -1, halfW, halfH);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        try {
                            FileOutputStream outputStream = new FileOutputStream(filePath);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            imageReader.setOnImageAvailableListener(listener, handler);
            final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    try {
                        createCameraPreview();
                        displayImagePreview();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            };
            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(builder.build(), captureCallback, handler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void playShutterSound() {
        if(!(shutterSound == null) && shutterSound.isPlaying())
            shutterSound.pause();
        shutterSound = MediaPlayer.create(this, R.raw.camera_shutter);
        shutterSound.setLooping(false);
        shutterSound.start();
    }

    @SuppressLint("MissingPermission")
    private void toggleFlash() {
        try {
            boolean flashState = Boolean.parseBoolean(flashButton.getTag().toString());
            int newIcon = R.drawable.ic_flash_on;
            if(!flashState) {
                newIcon = R.drawable.ic_flash_off;
                captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            } else {
                captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            }
            captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, handler);
            flashButton.setTag(!flashState);
            flashButton.setBackgroundResource(newIcon);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void turnOffFlash() {
        try {
            boolean flashState = Boolean.parseBoolean(flashButton.getTag().toString());
            if(flashState){
                captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, handler);
                flashButton.setTag(false);
                flashButton.setBackgroundResource(R.drawable.ic_flash_on);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void displayImagePreview() {
        if(filePath == null)
            return;
        runOnUiThread(() ->{
            Bitmap bitmap = Functions.decodeFiles(filePath, false);
            imagePreview.setImageBitmap(bitmap);
            previewLayer.setVisibility(View.VISIBLE);
        });
    }

    private void saveImage(byte[] bytes) throws IOException {
        OutputStream outputStream = new FileOutputStream(file);
        outputStream.write(bytes);
        outputStream.close();
    }

    private void startBackgroundThread() {
        handlerThread = new HandlerThread("Camera Background");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    protected void stopBackgroundThread() {
        try {
            if(!(cameraDevice == null))
                cameraDevice.close();
            handlerThread.quitSafely();
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if(textureView.isAvailable()) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else
            textureView.setSurfaceTextureListener(textureListener);
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }
}