package com.workruta.android;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.workruta.android.Utils.SharedPrefMngr;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class StartAct extends SharedCompatActivity {

    HorizontalScrollView scrollView;
    LinearLayout linearLayout, blackFade;
    ObjectAnimator animator;
    TextView signUp, signIn;
    ImageButton fingerPrint;
    public static Activity activity;
    SharedPrefMngr sharedPrefMngr;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        activity = this;
        sharedPrefMngr = new SharedPrefMngr(this);

        scrollView = findViewById(R.id.scrollView);
        linearLayout = findViewById(R.id.linearLayout);
        blackFade = findViewById(R.id.blackFade);
        signUp = findViewById(R.id.signUp);
        signIn = findViewById(R.id.signIn);
        fingerPrint = findViewById(R.id.fingerPrint);
        linearLayout.setBackgroundResource(R.drawable.slider);
        linearLayout.post(() -> {
            int LLHeight = linearLayout.getHeight();
            int intWidth = (int) (LLHeight * 1.5);
            populateView(intWidth, 0);
            AnimationDrawable animationDrawable = (AnimationDrawable) linearLayout.getBackground();
            animationDrawable.setEnterFadeDuration(300);
            animationDrawable.setExitFadeDuration(300);
            animationDrawable.start();
        });
        scrollView.setOnTouchListener((v, event) -> true);
        signUp.setOnClickListener(v -> startActivity(new Intent(this, SignupAct.class)));
        signIn.setOnClickListener(v -> startActivity(new Intent(this, SigninAct.class)));
        fingerPrint.setOnClickListener(v -> openFingerprint());
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @SuppressLint("InflateParams")
    private void openFingerprint() {
        boolean fingerEnabled = sharedPrefMngr.getFingerPrintEnabled();
        if(!fingerEnabled){
            if(blackFade.getChildCount() > 0)
                blackFade.removeAllViews();
            String posTxt = getResources().getString(R.string.ok),
                    alertTxt = getResources().getString(R.string.alert_ef);
            LinearLayout alertLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.alert_layout, null);
            TextView textView = alertLayout.findViewById(R.id.textView);
            Button negativeBtn = alertLayout.findViewById(R.id.negative);
            Button positiveBtn = alertLayout.findViewById(R.id.positive);
            textView.setText(alertTxt);
            negativeBtn.setVisibility(View.GONE);
            positiveBtn.setText(posTxt);
            positiveBtn.setOnClickListener((v) -> blackFade.setVisibility(View.GONE));
            blackFade.addView(alertLayout);
            blackFade.setVisibility(View.VISIBLE);
            return;
        }
        boolean fingerprintAccess = checkBiometricSupport();
        if(fingerprintAccess) {
            BiometricPrompt.AuthenticationCallback authenticationCallback = new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    loginFingerId();
                }
            };
            BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(getApplicationContext())
                    .setTitle("Login with your fingerprint")
                    .setNegativeButton("Cancel", getMainExecutor(), (dialogInterface, i) -> {}).build();
            biometricPrompt.authenticate(
                    getCancellationSignal(),
                    getMainExecutor(),
                    authenticationCallback);
        }
    }

    @SuppressLint("InflateParams")
    private void loginFingerId() {
        try {
            JSONObject object = sharedPrefMngr.getFingerCachedObject();
            int id = object.getInt("id");
            String photo = object.getString("photo");
            String name = object.getString("name");
            String email = object.getString("email");
            String phone = object.getString("phone");
            sharedPrefMngr.storeUserInfo(id, photo, name, email);
            sharedPrefMngr.cachePhoneNumber(phone);
            sharedPrefMngr.verifyLicence(true);
            sharedPrefMngr.detailBank(true);
            sharedPrefMngr.detailCar(true);
            sharedPrefMngr.verifyLicence(true);
            if(blackFade.getChildCount() > 0)
                blackFade.removeAllViews();
            String alertTxt = getResources().getString(R.string.logging_in);
            LinearLayout alertLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.requesting_layout, null);
            TextView textView = alertLayout.findViewById(R.id.textView);
            textView.setText(alertTxt);
            blackFade.addView(alertLayout);
            blackFade.setVisibility(View.VISIBLE);
            Intent intent = new Intent(this, DashboardAct.class);
            new android.os.Handler().postDelayed(() -> {
                startActivity(intent);
                finish();
            }, 2000);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private CancellationSignal getCancellationSignal(){
        return new CancellationSignal();
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private Boolean checkBiometricSupport(){
        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        if (!keyguardManager.isDeviceSecure()) {
            notifyUser("Fingerprint authentication has not been enabled in settings");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_BIOMETRIC)!= PackageManager.PERMISSION_GRANTED) {
            notifyUser("Fingerprint Authentication Permission is not enabled");
            return false;
        }
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT);
    }

    @SuppressLint("InflateParams")
    private void notifyUser(String text) {
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        String posTxt = getResources().getString(R.string.close);
        LinearLayout alertLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.alert_layout, null);
        TextView textView = alertLayout.findViewById(R.id.textView);
        Button negativeBtn = alertLayout.findViewById(R.id.negative);
        Button positiveBtn = alertLayout.findViewById(R.id.positive);
        textView.setText(text);
        negativeBtn.setVisibility(View.GONE);
        positiveBtn.setText(posTxt);
        positiveBtn.setOnClickListener((v) -> blackFade.setVisibility(View.GONE));
        blackFade.addView(alertLayout);
        blackFade.setVisibility(View.VISIBLE);
    }

    private void populateView(int intWidth, int accWidth){
        int oneTenth = intWidth / 5;
        if((oneTenth + accWidth > intWidth))
            oneTenth = intWidth - accWidth;
        int totWidth = oneTenth + accWidth;
        @SuppressLint("InflateParams") LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.empty_layer, null);
        linearLayout.addView(layout);
        int finalOneTenth = oneTenth;
        layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)layout.getLayoutParams();
                params.width = finalOneTenth;
                layout.setLayoutParams(params);
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if(intWidth > totWidth)
                    populateView(intWidth, totWidth);
                else
                    startScrollAnimation();
            }
        });
    }

    @SuppressLint("Recycle")
    private void startScrollAnimation() {
        linearLayout.setVisibility(View.VISIBLE);
        int timer = 3000, scrollWidth = scrollView.getChildAt(0).getWidth() - scrollView.getWidth();
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if(animator != null)
                        animator.cancel();
                    int scrllTo = scrollWidth, scrllFrm = 0, scrlledWidth = scrollView.getScrollX();
                    if((scrlledWidth + 10) > scrollWidth){
                        scrllFrm = scrollWidth;
                        scrllTo = 0;
                    }
                    animator = ObjectAnimator.ofInt(scrollView, "scrollX", scrllFrm, scrllTo).setDuration(timer);
                    animator.start();
                });
            }
        }, 0, timer);
    }

    @Override
    @SuppressLint("InflateParams")
    public void onBackPressed() {
        if(blackFade.getVisibility() == View.VISIBLE){
            blackFade.setVisibility(View.GONE);
            return;
        }
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
        negativeBtn.setOnClickListener((v) -> blackFade.setVisibility(View.GONE));
        positiveBtn.setOnClickListener((v) -> android.os.Process.killProcess(android.os.Process.myPid()));
        blackFade.addView(alertLayout);
        blackFade.setVisibility(View.VISIBLE);
    }
}