package com.workruta.android;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.workruta.android.Utils.Constants;
import com.workruta.android.Utils.CountingRequestBody;
import com.workruta.android.Utils.Functions;
import com.workruta.android.Utils.ImageLoader;
import com.workruta.android.Utils.RandomString;
import com.workruta.android.Utils.SharedPrefMngr;
import com.workruta.android.Utils.StorageUtils;
import com.yalantis.ucrop.UCrop;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.workruta.android.Utils.Constants.www;

public class ChangePhotoAct extends SharedCompatActivity {

    Context context;
    @SuppressLint("StaticFieldLeak")
    static Activity activity;
    ImageView imageView;
    TextView nameTV, save, done, next, headText;
    GridView gridView;
    LinearLayout blackOut, blackFade;
    RelativeLayout photoHolder;
    String myEmail, mySafeEmail;
    ArrayList<String> allMediaList;
    String[] allPath, menuTexts;
    File[] allFiles;
    static File selectedFile, file;
    ImageAdaptor imageAdaptor;
    static String name, tempDir, myPht;
    Random random;
    int[] colors, menuIcons;
    private int myId;
    boolean uploading, processing, backEnabled;
    ImageLoader imageLoader;
    SharedPrefMngr sharedPrefMngr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_photo);
        context = this;
        activity = this;

        myEmail = sharedPrefMngr.getMyEmail();
        mySafeEmail = functions.safeEmail(myEmail);

        Bundle bundle = getIntent().getExtras();
        backEnabled = bundle.getBoolean("backEnabled");

        sharedPrefMngr = new SharedPrefMngr(this);
        imageLoader = new ImageLoader(this);
        random = new Random();
        uploading = false;
        allMediaList = new ArrayList<>();
        myId = sharedPrefMngr.getMyId();
        myPht = sharedPrefMngr.getMyPht();
        name = sharedPrefMngr.getMyName();
        colors = new int[]{
                R.color.blash,
                R.color.blash1,
                R.color.blash2,
                R.color.blash3,
                R.color.blash4,
                R.color.blash5
        };
        menuIcons = new int[]{R.drawable.ic_camera_ash, R.drawable.ic_gallery_ash};
        menuTexts = new String[]{"Take Photo", "Choose from Gallery"};
        tempDir = StorageUtils.getStorageDirectories(context)[0] + "/Android/data/" + getApplicationContext().getPackageName() + "/tempFiles";
        if(!(new File(tempDir).exists()))
            new File(tempDir).mkdir();

        blackFade = findViewById(R.id.blackFade);
        imageView = findViewById(R.id.imageView);
        nameTV = findViewById(R.id.nameTV);
        save = findViewById(R.id.save);
        headText = findViewById(R.id.headText);
        done = findViewById(R.id.done);
        next = findViewById(R.id.next);
        gridView = findViewById(R.id.gridView);
        blackOut = findViewById(R.id.blackOut);
        photoHolder = findViewById(R.id.photoHolder);

        if(!backEnabled) {
            headText.setVisibility(View.GONE);
        }

        nameTV.setText(name);
        processing = myPht == null;
        if(!processing)
            imageLoader.displayImage(myPht, imageView);

        imageView.setOnClickListener(v -> showOptions());
        headText.setOnClickListener(v -> onBackPressed());
        save.setOnClickListener(v -> savePhotoChange());
        done.setOnClickListener(v -> {
            if(selectedFile != null)
                handleSelectedImage();
        });
        next.setOnClickListener(v -> {
            if(!backEnabled)
                startActivity(new Intent(context, DashboardAct.class));
        });
        gridView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if(gridView.getVisibility() == View.VISIBLE)
                blackOut.setVisibility(View.INVISIBLE);
        });

    }

    @SuppressLint("InflateParams")
    private void showOptions() {
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        RelativeLayout optionBox = (RelativeLayout) getLayoutInflater().inflate(R.layout.option_box, null);
        LinearLayout box = optionBox.findViewById(R.id.box);
        for(int i = 0; i < 2; i++){
            String menuText = menuTexts[i];
            int menuIcon = menuIcons[i];
            LinearLayout optionText = (LinearLayout) getLayoutInflater().inflate(R.layout.option_text, null);
            TextView textView = optionText.findViewById(R.id.textView);
            textView.setText(menuText);
            textView.setCompoundDrawablesWithIntrinsicBounds(menuIcon, 0, 0, 0);
            int finalI = i;
            textView.setOnClickListener((v) -> {
                blackFade.setVisibility(View.GONE);
                if(finalI == 0){
                    Intent intent = new Intent(context, CameraAct.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("activity", 0);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else
                    startLoader();
            });
            box.addView(optionText);
        }
        blackFade.addView(optionBox);
        blackFade.setVisibility(View.VISIBLE);
    }

    public static void displayImage(String filePath) {
        selectedFile = new File(filePath);
        handleSelectedImage();
    }

    public static void handleSelectedImage(){
        @SuppressLint("SimpleDateFormat") String filePath,
                timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()),
                randStr = UUID.randomUUID().toString();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            RandomString gen = new RandomString(8, ThreadLocalRandom.current());
            randStr = gen.toString();
        }
        Uri fileUri = Uri.fromFile(selectedFile);
        filePath = tempDir + "/IMG_" + randStr + timeStamp +".jpg";
        file = new File(filePath);
        UCrop uCrop = UCrop.of(fileUri, Uri.fromFile(file))
                .withAspectRatio(1, 1);
        uCrop.start(activity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == UCrop.REQUEST_CROP && !(data == null)) {
            Uri resultUri = UCrop.getOutput(data);
            handleCropper(resultUri);
        }
    }

    private void handleCropper(Uri resultUri) {
        imageView.setImageURI(resultUri);
        gridView.setVisibility(View.INVISIBLE);
        done.setVisibility(View.INVISIBLE);
    }

    @SuppressLint("InflateParams")
    private void savePhotoChange() {
        if(file != null && !uploading) {
            uploading = true;
            RelativeLayout progressView = (RelativeLayout) getLayoutInflater().inflate(R.layout.progress_bar, null);
            TextView progressText = progressView.findViewById(R.id.postProgressText);
            ProgressBar progressBar = progressView.findViewById(R.id.postProgressBar);
            photoHolder.addView(progressView);
            progressView.setOnClickListener(v -> {
                return;
            });
            int photoHolderW = photoHolder.getWidth(), photoHolderH = photoHolder.getHeight();
            progressView.setLayoutParams(new RelativeLayout.LayoutParams(photoHolderW, photoHolderH));
            MultipartBody.Builder multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
            Uri uris = Uri.fromFile(file);
            String fileExt = MimeTypeMap.getFileExtensionFromUrl(uris.toString());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt.toLowerCase());
            multipartBody.addFormDataPart("file", file.getName(), RequestBody.create(file, MediaType.parse(mimeType)));
            multipartBody.addFormDataPart("id", String.valueOf(myId));

            @SuppressLint("SetTextI18n") final CountingRequestBody.Listener progressListener = (bytesRead, contentLength) -> {
                if (bytesRead >= contentLength && contentLength > 0) {
                    final int progress = (int) Math.round((((double) bytesRead / contentLength) * 100));
                    runOnUiThread(() -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            progressBar.setProgress(progress, true);
                        else
                            progressBar.setProgress(progress);
                        progressText.setText(progress + "%");
                    });
                }
            };

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addNetworkInterceptor(chain -> {
                        Request originalRequest = chain.request();

                        if (originalRequest.body() == null) {
                            return chain.proceed(originalRequest);
                        }
                        Request progressRequest = originalRequest.newBuilder()
                                .method(originalRequest.method(),
                                        new CountingRequestBody(originalRequest.body(), progressListener))
                                .build();

                        return chain.proceed(progressRequest);

                    })
                    .build();
            RequestBody requestBody = multipartBody.build();
            Request request = new Request.Builder()
                    .url(Constants.changePhotoUrl)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .post(requestBody)
                    .build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    //onError
                    //Log.e("failure Response", mMessage);
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        uploading = false;
                        String responseString = Objects.requireNonNull(response.body()).string();
                        String safeUrl = functions.safeUrl(responseString);
                        FirebaseDatabase dbInstance = FirebaseDatabase.getInstance();
                        DatabaseReference database = dbInstance.getReference(mySafeEmail + "/photoUrl");
                        database.setValue(safeUrl);
                        runOnUiThread(() -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                progressBar.setProgress(100, true);
                            else
                                progressBar.setProgress(100);
                            progressText.setText("100%");
                            new android.os.Handler().postDelayed(() ->{
                                runOnUiThread(() -> {
                                    progressText.setText("");
                                    progressText.setBackgroundResource(R.drawable.ic_check);
                                });
                            }, 500);
                            new android.os.Handler().postDelayed(() ->{
                                runOnUiThread(() -> {
                                    progressView.setVisibility(View.GONE);
                                });
                            }, 1500);
                        });
                        myPht = www + responseString;
                        JSONObject object = new JSONObject();
                        object.put("photo", myPht);
                        sharedPrefMngr.savePhoto(myPht);
                        receiveInfoChange("photo", object);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Files.delete(file.toPath());
                        } else
                            file.delete();
                        file = null;
                        selectedFile = null;
                        if(processing)
                            runOnUiThread(() -> {
                                next.setVisibility(View.VISIBLE);
                            });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void startLoader(){
        selectedFile = null;
        blackOut.setVisibility(View.VISIBLE);
        if(blackOut.getChildCount() == 0) {
            @SuppressLint("InflateParams") LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.box_loader, null);
            blackOut.addView(layout);
            layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    populateChanges(layout);
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    displayGallery();
                }
            });
        } else
            displayGallery();
    }

    private void displayGallery() {
        new android.os.Handler().postDelayed(() -> {
            gridView.setVisibility(View.VISIBLE);
            done.setVisibility(View.VISIBLE);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels - 5;
            int imgW = (width/3) - 10;
            allMediaList = new ArrayList<>();
            allPath = StorageUtils.getStorageDirectories(context);
            for(String path: allPath){
                File storage = new File(path);
                loadDirectoryFiles(storage);
            }

            allFiles = new File[allMediaList.size()];
            for (int x = 0; x < allMediaList.size(); x++){
                String filePth = allMediaList.get(x);
                allFiles[x] = new File(filePth);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Arrays.sort(allFiles, Comparator.comparingLong(File::lastModified).reversed());
            }
            imageAdaptor = new ImageAdaptor(allFiles, imgW);
            gridView.setAdapter(imageAdaptor);
        }, 500);
    }

    private void populateChanges(View view) {
        if (view instanceof ImageView){
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void run() {
                    Drawable drawable = view.getBackground();
                    int i = ThreadLocalRandom.current().nextInt(0, 5 + 1), bColor = R.color.black;
                    if(drawable != null)
                        bColor = ((ColorDrawable) drawable).getColor();
                    int colorTo = ContextCompat.getColor(context, colors[i]), colorFrom = bColor;
                    runOnUiThread(() -> ObjectAnimator.ofObject(view, "backgroundColor", new ArgbEvaluator(), colorFrom , colorTo)
                            .setDuration(400).start());
                }
            }, 0, 500);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                populateChanges(innerView);
            }
        }
    }

    public void loadDirectoryFiles(File directory){
        boolean notForbidden = false;
        String[] forbiddenPaths = new String[]{
                "/Android/data",
                "/Android/obb",
                "/LOST.DIR",
                "/.thumbnail"
        };
        File[] fileList = directory.listFiles();
        if(fileList != null && fileList.length > 0){
            for (File file : fileList) {
                String filePath = file.getAbsolutePath();
                if (file.isDirectory()) {
                    for (String forbiddenPath : forbiddenPaths) {
                        if (filePath.contains(forbiddenPath)) {
                            notForbidden = true;
                            break;
                        }
                    }
                    if (!notForbidden)
                        loadDirectoryFiles(file);
                } else {
                    String pthPar = file.getParent();

                    String[] pthPars = Objects.requireNonNull(pthPar).split("/");
                    if (!(pthPars[pthPars.length - 1].equals("LOST.DIR") || pthPars[pthPars.length - 1].equals(".thumbnails"))) {
                        String name = file.getName().toLowerCase();
                        for (String ext : Constants.allowedExtImg) {
                            if (name.endsWith(ext)) {
                                allMediaList.add(filePath);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private class ImageAdaptor extends BaseAdapter {

        private final int imageWidth;
        File[] itemList;
        Bitmap bitmap;

        public ImageAdaptor(File[] itemList, int imageWidth) {
            this.itemList = itemList;
            this.imageWidth = imageWidth;
        }

        @Override
        public int getCount() {
            return this.itemList.length;
        }

        @Override
        public Object getItem(int position) {
            return this.itemList[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.image_box, parent, false);
            convertView.setLayoutParams(new GridView.LayoutParams(imageWidth ,imageWidth ));
            File thisFile = itemList[position];
            final String filePath = String.valueOf(thisFile);
            final ImageView imageView = convertView.findViewById(R.id.imgView);
            convertView.setContentDescription(filePath);
            bitmap = Functions.decodeFiles(filePath, true);
            imageView.setImageBitmap(bitmap);
            View finalConvertView = convertView;
            imageView.setOnClickListener(v -> {
                for (int i = 0; i < gridView.getChildCount(); i++){
                    View view = gridView.getChildAt(i);
                    if(!(view == finalConvertView))
                        view.setBackgroundResource(R.drawable.null_border);
                }
                selectedFile = thisFile;
                finalConvertView.setBackgroundResource(R.drawable.border_box_x);
            });
            return convertView;
        }
    }

    @SuppressLint("InflateParams")
    public void onBackPressed(){
        if(gridView.getVisibility() == View.VISIBLE){
            gridView.setVisibility(View.INVISIBLE);
            done.setVisibility(View.INVISIBLE);
            return;
        }
        if(blackFade.getVisibility() == View.VISIBLE){
            blackFade.setVisibility(View.GONE);
            return;
        }
        if(!backEnabled) {
            if(blackFade.getChildCount() > 0)
                blackFade.removeAllViews();
            String negTxt = getResources().getString(R.string.cancel),
                    posTxt = getResources().getString(R.string.quit_app),
                    alertTxt = getResources().getString(R.string.quit_text);
            LinearLayout alertLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.alert_layout, null);
            TextView textView = alertLayout.findViewById(R.id.textView);
            Button negativeBtn = alertLayout.findViewById(R.id.negative);
            Button positiveBtn = alertLayout.findViewById(R.id.positive);
            textView.setText(alertTxt);
            negativeBtn.setText(negTxt);
            positiveBtn.setText(posTxt);
            negativeBtn.setOnClickListener((v) -> {
                blackFade.setVisibility(View.GONE);
            });
            positiveBtn.setOnClickListener((v) -> android.os.Process.killProcess(android.os.Process.myPid()));
            blackFade.addView(alertLayout);
            blackFade.setVisibility(View.VISIBLE);
            return;
        }
        finish();
    }
}