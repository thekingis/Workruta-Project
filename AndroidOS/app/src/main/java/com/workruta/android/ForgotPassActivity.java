package com.workruta.android;

import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.workruta.android.Adapter.ViewPagerAdapter;
import com.workruta.android.Utils.Constants;

import org.json.JSONObject;

import java.util.Objects;

import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForgotPassActivity extends SharedCompatActivity {

    Context context;
    static ViewPager viewPager;
    @SuppressLint("StaticFieldLeak")
    static LinearLayout whiteFade, blackFade;
    FPCodeFragment fpCodeFragment;
    FPChangePasswordFragment fpChangePasswordFragment;
    FPEmailFragment fpEmailFragment;
    FPPhoneFragment fpPhoneFragment;
    FPStartFragment fpStartFragment;
    FPSuccessFragment fpSuccessFragment;
    public static int curPos;
    public static String curType, text, user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);
        context = this;
        curPos = 0;

        viewPager = findViewById(R.id.viewPager);
        whiteFade = findViewById(R.id.whiteFade);
        blackFade = findViewById(R.id.blackFade);

        whiteFade.setOnClickListener((v) -> {return;});

        fpCodeFragment = new FPCodeFragment(context);
        fpChangePasswordFragment = new FPChangePasswordFragment(context);
        fpEmailFragment = new FPEmailFragment(context);
        fpPhoneFragment = new FPPhoneFragment(context);
        fpStartFragment = new FPStartFragment(context);
        fpSuccessFragment = new FPSuccessFragment(context);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(fpStartFragment, null);
        adapter.addFragment(fpEmailFragment, null);
        adapter.addFragment(fpPhoneFragment, null);
        adapter.addFragment(fpCodeFragment, null);
        adapter.addFragment(fpChangePasswordFragment, null);
        adapter.addFragment(fpSuccessFragment, null);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                curPos = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    public static void changeViewPage(int position, boolean s){
        viewPager.setCurrentItem(position, s);
    }

    public static void getVerificationCode(Context context, String value, String type){
        curType = type;
        text = value;
        whiteFade.setVisibility(View.VISIBLE);
        if(whiteFade.getChildCount() > 0)
            whiteFade.removeAllViews();
        @SuppressLint("InflateParams") RelativeLayout layout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.slide_loader, null);
        whiteFade.addView(layout);
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("value", value)
                    .addFormDataPart("type", type)
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.sendMailUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                whiteFade.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    String responseString = Objects.requireNonNull(response.body()).string();
                    Log.i("responseString", responseString);
                    JSONObject object = new JSONObject(responseString);
                    boolean noError = object.getBoolean("noError");
                    String dataStr = object.getString("dataStr");
                    if(noError) {
                        user = dataStr;
                        changeViewPage(3, true);
                    } else {
                        if(type.equals("email"))
                            FPEmailFragment.displayError(dataStr);
                        else if(type.equals("phone"))
                            FPPhoneFragment.displayError(dataStr);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    public static void getVerificationCode(Context context, String code){
        whiteFade.setVisibility(View.VISIBLE);
        if(whiteFade.getChildCount() > 0)
            whiteFade.removeAllViews();
        @SuppressLint("InflateParams") RelativeLayout layout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.slide_loader, null);
        whiteFade.addView(layout);
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("type", curType)
                    .addFormDataPart("code", code)
                    .addFormDataPart("phoneNumberText", text)
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.verifyCodeUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                whiteFade.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    String responseString = Objects.requireNonNull(response.body()).string();
                    JSONObject object = new JSONObject(responseString);
                    boolean noError = object.getBoolean("noError");
                    String dataStr = object.getString("dataStr");
                    if(noError) {
                        changeViewPage(4, true);
                    } else {
                        FPCodeFragment.displayError(dataStr);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    public static void changePassword(Context context, String password, String conPass){
        whiteFade.setVisibility(View.VISIBLE);
        if(whiteFade.getChildCount() > 0)
            whiteFade.removeAllViews();
        @SuppressLint("InflateParams") RelativeLayout layout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.slide_loader, null);
        whiteFade.addView(layout);
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("action", "changePassword")
                    .addFormDataPart("user", user)
                    .addFormDataPart("password", password)
                    .addFormDataPart("conPass", conPass)
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.actionsUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                whiteFade.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    String responseString = Objects.requireNonNull(response.body()).string();
                    JSONObject object = new JSONObject(responseString);
                    boolean noError = object.getBoolean("noError");
                    String dataStr = object.getString("dataStr");
                    if(noError) {
                        changeViewPage(5, true);
                    } else {
                        FPChangePasswordFragment.displayError(dataStr);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    public static void finishActivity(Context context){
        context.startActivity(new Intent(context, SigninAct.class));
        ((Activity)context).finish();
    }

    @Override
    @SuppressLint("InflateParams")
    public void onBackPressed() {
        if(whiteFade.getVisibility() == View.VISIBLE)
            return;
        if(blackFade.getVisibility() == View.VISIBLE){
            blackFade.setVisibility(View.GONE);
            return;
        }
        if(curPos == 1 || curPos == 2)
            changeViewPage(0, true);
        else if(curPos == 3 || curPos == 4){
            if(blackFade.getChildCount() > 0)
                blackFade.removeAllViews();
            String negTxt = getResources().getString(R.string.cancel),
                    posTxt = getResources().getString(R.string.discard),
                    alertTxt = getResources().getString(R.string.discard_text);
            LinearLayout alertLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.alert_layout, null);
            TextView textView = alertLayout.findViewById(R.id.textView);
            Button negativeBtn = alertLayout.findViewById(R.id.negative);
            Button positiveBtn = alertLayout.findViewById(R.id.positive);
            textView.setText(alertTxt);
            negativeBtn.setText(negTxt);
            positiveBtn.setText(posTxt);
            negativeBtn.setOnClickListener((v) ->  blackFade.setVisibility(View.GONE));
            positiveBtn.setOnClickListener((v) -> finish());
            blackFade.addView(alertLayout);
            blackFade.setVisibility(View.VISIBLE);
        } else if(curPos == 0)
            finish();
    }
}