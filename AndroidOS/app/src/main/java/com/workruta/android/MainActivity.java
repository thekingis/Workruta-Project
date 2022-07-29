package com.workruta.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.workruta.android.Utils.SharedPrefMngr;
import com.workruta.android.Utils.StorageUtils;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    Context context;
    boolean loggedIn;
    String[] requestPermissions;
    Spanned spanned;
    String text;
    TextView textView;
    Button yes, no;
    LinearLayout reqLayout;
    public static int screenWidth;
    DisplayMetrics displayMetrics;
    String tempDirPath, primaryStorage, tempFilesPath;
    File tempDir;
    SharedPrefMngr sharedPrefMngr;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        context = this;
        sharedPrefMngr = new SharedPrefMngr(this);

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;

        loggedIn = sharedPrefMngr.loggedIn();

        requestPermissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.USE_BIOMETRIC,
                Manifest.permission.CAMERA,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        };
        text = "from <b>KanaSoft</b>";
        reqLayout = findViewById(R.id.reqLayout);
        textView = findViewById(R.id.textView);
        yes = findViewById(R.id.yes);
        no = findViewById(R.id.no);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            spanned = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
        else
            spanned = Html.fromHtml(text);
        textView.setText(spanned);
        yes.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
            startActivityForResult(intent, 51);
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        });
        no.setOnClickListener(v -> {
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        });

        tempFilesPath = sharedPrefMngr.getTempFiles();
        primaryStorage = StorageUtils.getStorageDirectories(context)[0];
        tempDirPath = primaryStorage + "/Android/data/" + context.getPackageName() + "/tempFiles";
        tempDir = new File(tempDirPath);
        if(tempDir.exists() && Objects.requireNonNull(tempDir.listFiles()).length > 0) {
            try {
                for (File file : Objects.requireNonNull(tempDir.listFiles())){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        Files.delete(file.toPath());
                    else
                        file.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(!tempDir.exists())
            tempDir.mkdir();
        if(!(tempFilesPath == null)){
            try {
                JSONArray array = new JSONArray(tempFilesPath);
                for (int i = 0; i < array.length(); i++){
                    String tempFilePath = array.getString(i);
                    File file = new File(tempFilePath);
                    if(file.exists()){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            Files.delete(file.toPath());
                        else
                            file.delete();
                    }
                }
                sharedPrefMngr.emptyTempFile();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        new android.os.Handler().postDelayed(
                () -> {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (checkPermissionGrant()) {
                            loadNextPage();
                        } else {
                            requestPermissionGrant();
                        }
                    } else loadNextPage();
                }, 3000);
    }

    private void requestPermissionGrant() {
        ActivityCompat.requestPermissions(this, requestPermissions, 10);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private boolean checkPermissionGrant() {
        int WESR = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                RESR = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE),
                AFLN = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION),
                ACLN = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION),
                ABLN = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                BIOM = ContextCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC),
                CAMR = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA),
                CALL = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE),
                pckMngr = PackageManager.PERMISSION_GRANTED;
        return WESR == pckMngr && RESR == pckMngr && AFLN == pckMngr && ACLN == pckMngr && ABLN == pckMngr && BIOM == pckMngr && CAMR == pckMngr && CALL == pckMngr;
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            getWindow().setDecorFitsSystemWindows(false);
            return;
        }
        int uiOptions = decorView.getSystemUiVisibility();
        /*uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE;
        uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;*/
        // Hide the nav bar and status bar
        uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkPermissionGrant()) {
            loadNextPage();
        } else {
            reqLayout.setVisibility(View.VISIBLE);
        }
    }

    private void loadNextPage() {
        Intent intent;
        if(loggedIn){
            intent = new Intent(MainActivity.this, DashboardAct.class);
            String photo = sharedPrefMngr.getMyPht();
            if(photo == null) {
                intent = new Intent(MainActivity.this, ChangePhotoAct.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("backEnabled", false);
                intent.putExtras(bundle);
            }
        } else {
            intent = new Intent(MainActivity.this, StartAct.class);
            boolean checkCachedNumber = sharedPrefMngr.checkCachedNumber();
            if(checkCachedNumber) {
                boolean verified = sharedPrefMngr.getVerification();
                if(verified)
                    intent = new Intent(this, SignFormAct.class);
                else {
                    long curTime = System.currentTimeMillis(),
                            cachedTime = sharedPrefMngr.getCachedTime(),
                            expTime = cachedTime + (1000 * 60 * 30);
                    if (curTime > expTime)
                        sharedPrefMngr.clearCachedNumber();
                    else
                        intent = new Intent(MainActivity.this, SignupAct.class);
                }
            }
        }
        finish();
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
    }
}