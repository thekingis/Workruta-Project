package com.workruta.android;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.workruta.android.Utils.Constants;
import com.workruta.android.Utils.SharedPrefMngr;

import org.json.JSONObject;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignupAct extends SharedCompatActivity {

    RelativeLayout mainView;
    HorizontalScrollView scrollView;
    EditText phoneNumber, vCode1, vCode2;
    TextView next, counterView, returnBack;
    LinearLayout whiteFade, stageOne, stageTwo;
    boolean changingText, requesting, keyPressed, signed;
    String phoneNumberText;;
    ObjectAnimator animator;
    SharedPrefMngr sharedPrefMngr;
    static boolean isActive = false;
    Activity startAct;
    int w;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        sharedPrefMngr = new SharedPrefMngr(this);

        startAct = StartAct.activity;
        changingText = false;
        requesting = false;
        keyPressed = false;
        signed = false;
        boolean checkCachedNumber = sharedPrefMngr.checkCachedNumber();
        boolean verified = sharedPrefMngr.getVerification();

        mainView = findViewById(R.id.mainView);
        scrollView = findViewById(R.id.scrollView);
        whiteFade = findViewById(R.id.whiteFade);
        stageOne = findViewById(R.id.stageOne);
        stageTwo = findViewById(R.id.stageTwo);
        phoneNumber = findViewById(R.id.phoneNumber);
        vCode1 = findViewById(R.id.vCode1);
        vCode2 = findViewById(R.id.vCode2);
        counterView = findViewById(R.id.counterView);
        returnBack = findViewById(R.id.returnBack);
        next = findViewById(R.id.next);

        setupUI(mainView);
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                w = scrollView.getWidth();
                LinearLayout.LayoutParams paramsOne = (LinearLayout.LayoutParams)stageOne.getLayoutParams(),
                        paramsTwo  = (LinearLayout.LayoutParams)stageTwo.getLayoutParams();
                paramsOne.width = w;
                paramsTwo.width = w;
                stageOne.setLayoutParams(paramsOne);
                stageTwo.setLayoutParams(paramsTwo);
                scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        scrollView.post(() -> {
            if(checkCachedNumber && !verified) {
                startCountDown();
                swipeStage(false);
            }
        });
        vCode1.setLongClickable(false);
        vCode2.setLongClickable(false);
        phoneNumber.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE)
                submitNumber();
            return false;
        });
        vCode1.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE)
                verifyCode();
            return false;
        });
        vCode2.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE)
                verifyCode();
            return false;
        });
        phoneNumber.setAccessibilityDelegate(new View.AccessibilityDelegate(){
            @Override
            public void sendAccessibilityEvent(View host, int eventType) {
                super.sendAccessibilityEvent(host, eventType);
                if(eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED){
                    int selStart = phoneNumber.getSelectionStart(),
                    selEnd = phoneNumber.getSelectionEnd();
                    if(selStart == selEnd && selStart > 0){
                        char c = phoneNumber.getText().charAt(selStart - 1);
                        if(String.valueOf(c).equals(" "))
                            phoneNumber.setSelection(selStart - 1);
                    }
                }
            }
        });
        vCode1.setAccessibilityDelegate(new View.AccessibilityDelegate(){
            @Override
            public void sendAccessibilityEvent(View host, int eventType) {
                super.sendAccessibilityEvent(host, eventType);
                if(eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED){
                    int selStart = vCode1.getSelectionStart(),
                    selEnd = vCode1.getSelectionEnd();
                    if(selStart != selEnd)
                        vCode1.setSelection(selEnd);
                    if(selEnd == 3){
                        vCode1.clearFocus();
                        vCode2.requestFocus();
                        vCode2.setSelection(0);
                    }
                }
            }
        });
        vCode2.setAccessibilityDelegate(new View.AccessibilityDelegate(){
            @Override
            public void sendAccessibilityEvent(View host, int eventType) {
                super.sendAccessibilityEvent(host, eventType);
                if(eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED){
                    int selStart = vCode2.getSelectionStart(),
                    selEnd = vCode2.getSelectionEnd();
                    if(selStart != selEnd)
                        vCode2.setSelection(selEnd);
                    if(vCode2.getText().length() == 0 && vCode1.getText().length() < 3) {
                        vCode2.clearFocus();
                        vCode1.requestFocus();
                        vCode1.setSelection(vCode1.getText().length());
                    }
                }
            }
        });
        vCode2.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus &&vCode2.getText().length() == 0 && vCode1.getText().length() < 3) {
                vCode2.clearFocus();
                vCode1.requestFocus();
                vCode1.setSelection(vCode1.getText().length());
                toggleSoftKeyboard(vCode1, false);
            }
        });
        vCode1.setOnKeyListener((v, keyCode, event) -> {
            if(event.getAction() == KeyEvent.ACTION_DOWN)
                keyPressed = true;
            if(event.getAction() == KeyEvent.ACTION_UP)
                keyPressed = false;
            return keyCode != KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN && vCode1.getText().length() == 3 && vCode2.getText().length() == 3;
        });
        vCode2.setOnKeyListener((v, keyCode, event) -> {
            if(event.getAction() == KeyEvent.ACTION_DOWN){
                keyPressed = true;
                if(keyCode != KeyEvent.KEYCODE_DEL && vCode2.getText().length() == 3)
                    return true;
                if(keyCode == KeyEvent.KEYCODE_DEL && vCode2.getSelectionStart() == 0){
                    vCode1.setText(vCode1.getText().delete(2, 3));
                    vCode2.clearFocus();
                    vCode1.requestFocus();
                    vCode1.setSelection(2);
                }
            }
            if(event.getAction() == KeyEvent.ACTION_UP)
                keyPressed = false;
            return false;
        });
        phoneNumber.setOnKeyListener((v, keyCode, event) -> {
            if(event.getAction() == KeyEvent.ACTION_DOWN) {
                keyPressed = true;
                if(keyCode != KeyEvent.KEYCODE_DEL && phoneNumber.getText().toString().replaceAll(" ", "").length() > 9)
                    return true;
            }
            if(event.getAction() == KeyEvent.ACTION_UP){
                if(keyCode == KeyEvent.KEYCODE_DEL){
                    int caret = phoneNumber.getSelectionStart() - 1;
                    if(caret > 0) {
                        char c = phoneNumber.getText().charAt(caret);
                        if (String.valueOf(c).equals(" ")) {
                            phoneNumber.setText(phoneNumber.getText().delete(caret, caret + 1));
                            phoneNumber.setSelection(caret);
                        }
                    }
                }
                Editable s = phoneNumber.getText();
                arraignNumber(s);
                new android.os.Handler().postDelayed(() -> keyPressed = false, 50);
            }
            return false;
        });
        phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!changingText && !keyPressed)
                    arraignNumber(s);
            }
        });
        vCode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!changingText && s.length() > 3) {
                    changingText = true;
                    String extraStr = s.subSequence(3, s.length()).toString();
                    int caret = vCode1.getSelectionStart();
                    vCode1.setText(vCode1.getText().delete(3, s.length()));
                    vCode2.setText(vCode2.getText().insert(0, extraStr));
                    if(caret < 3)
                        vCode1.setSelection(caret);
                    else {
                        vCode1.clearFocus();
                        vCode2.requestFocus();
                        vCode2.setSelection(caret - 3);
                    }
                    changingText = false;
                }
                if(!changingText && s.length() < 3 && vCode2.getText().length() > 0) {
                    changingText = true;
                    String extraStr = vCode2.getText().subSequence(0, 3 - s.length()).toString();
                    int caret = vCode1.getSelectionStart();
                    vCode2.setText(vCode2.getText().delete(0, 3 - s.length()));
                    vCode1.setText(vCode1.getText().insert(s.length(), extraStr));
                    vCode1.setSelection(caret);
                    changingText = false;
                }
            }
        });
        whiteFade.setOnClickListener(v -> {
            return;
        });
        next.setOnClickListener(v -> {
            if(!signed)
                submitNumber();
            else
                verifyCode();
        });
        returnBack.setOnClickListener(v -> {
            signed = false;
            returnBack.setVisibility(View.INVISIBLE);
            swipeStage(true);
            sharedPrefMngr.clearCachedNumber();
        });
        scrollView.setOnTouchListener((v, event) -> true);

        if(checkCachedNumber){
            if(!verified) {
                signed = true;
                returnBack.setVisibility(View.VISIBLE);
                phoneNumberText = sharedPrefMngr.getPhoneNumber();
                phoneNumber.setText(phoneNumberText);
            } else {
                finish();
                startActivity(new Intent(this, SignFormAct.class));
            }
        }

    }

    private void verifyCode() {
        String code = vCode1.getText().toString();
        code += vCode2.getText().toString();
        if(code.length() < 6){
            Toast.makeText(this, "Incomplete Verification Code", Toast.LENGTH_LONG).show();
            return;
        }
        requesting = true;
        whiteFade.setVisibility(View.VISIBLE);
        if(whiteFade.getChildCount() > 0)
            whiteFade.removeAllViews();
        @SuppressLint("InflateParams") RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.slide_loader, null);
        whiteFade.addView(layout);
        String finalCode = code;
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("phoneNumberText", phoneNumberText)
                    .addFormDataPart("type", "normal")
                    .addFormDataPart("code", finalCode)
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.verifyCodeUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                requesting = false;
                if (response.isSuccessful()) {
                    String responseString = Objects.requireNonNull(response.body()).string();
                    JSONObject object = new JSONObject(responseString);
                    boolean noError = object.getBoolean("noError");
                    String dataStr = object.getString("dataStr");
                    if(noError) {
                        sharedPrefMngr.verifyPhoneNumber();
                        if(!(startAct == null))
                            startAct.finish();
                        finish();
                        startActivity(new Intent(this, SignFormAct.class));
                    } else {
                        whiteFade.setVisibility(View.GONE);
                        Toast.makeText(this, dataStr, Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    private void submitNumber(){
        phoneNumberText = phoneNumber.getText().toString().replaceAll(" ", "");
        if(phoneNumberText.startsWith("0") || phoneNumberText.length() != 10){
            Toast.makeText(this, "Invalid Phone Number", Toast.LENGTH_LONG).show();
            return;
        }
        requesting = true;
        whiteFade.setVisibility(View.VISIBLE);
        if(whiteFade.getChildCount() > 0)
            whiteFade.removeAllViews();
        @SuppressLint("InflateParams") RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.slide_loader, null);
        whiteFade.addView(layout);
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("phoneNumber", phoneNumberText)
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.phoneVerifyUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                requesting = false;
                whiteFade.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    String responseString = Objects.requireNonNull(response.body()).string();
                    JSONObject object = new JSONObject(responseString);
                    boolean noError = object.getBoolean("noError");
                    String dataStr = object.getString("dataStr");
                    if(noError) {
                        signed = true;
                        vCode1.setText(dataStr.substring(0, 3));
                        vCode2.setText(dataStr.substring(3));
                        returnBack.setVisibility(View.VISIBLE);
                        swipeStage(false);
                        sharedPrefMngr.cachePhoneNumber(phoneNumberText);
                        startCountDown();
                    } else
                        Toast.makeText(this, dataStr, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    private void swipeStage(boolean revert) {
        int scrllTo = w, scrllFrm = 0;
        if(revert) {
            scrllTo = 0;
            scrllFrm = w;
        }
        if(animator != null)
            animator.cancel();
        animator = ObjectAnimator.ofInt(scrollView, "scrollX", scrllFrm, scrllTo).setDuration(300);
        animator.start();
    }

    private void arraignNumber(Editable s){
        String text = s.toString().replaceAll(" ", "");
        int textLn = text.length(), caret = phoneNumber.getSelectionStart();
        String str = phoneNumber.getText().subSequence(0, caret).toString();
        int numSpc = str.replaceAll("[^ ]", "").length();
        caret -= numSpc;
        if(textLn > 2) {
            changingText = true;
            String newStr = text.substring(0, 3);
            if (textLn > 3) {
                int strt = 3;
                newStr += " ";
                caret++;
                if (textLn > 6) {
                    strt = 6;
                    newStr += text.substring(3, 6);
                    newStr += " ";
                    caret++;
                }
                newStr += text.substring(strt);
            }
            phoneNumber.setText(newStr);
            phoneNumber.setSelection(caret);
            changingText = false;
        }
    }

    private void startCountDown(){
        long curTime = System.currentTimeMillis() / 1000;
        long expTime = (sharedPrefMngr.getCachedTime() / 1000) + (60 * 30);
        final long[] countTime = {expTime - curTime};
        if(countTime[0] > 0){
            String timeStr = getTimeString(countTime[0]);
            counterView.setText(timeStr);
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    countTime[0]--;
                    if(countTime[0] < 0){
                        timer.cancel();
                        runOnUiThread(() -> swipeStage(true));
                        sharedPrefMngr.clearCachedNumber();
                        return;
                    }
                    String timeStr = getTimeString(countTime[0]);
                    runOnUiThread(() -> counterView.setText(timeStr));
                }
            }, 0, 1000);
        } else {
            swipeStage(true);
            sharedPrefMngr.clearCachedNumber();
        }
    }

    private String getTimeString(long countTime){
        int m = (int) (countTime / 60),
                s = (int) (countTime % 60);
        String min = String.valueOf(m),
                sec = String.valueOf(s);
        if(m < 10)
            min = "0" + min;
        if(s < 10)
            sec = "0" + sec;
        return min + ":" + sec;
    }

    public void onBackPressed(){
        if(!requesting)
            finish();
    }

    public void toggleSoftKeyboard(View view, boolean hide) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(hide)
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        else
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                toggleSoftKeyboard(v, true);
                phoneNumber.clearFocus();
                vCode1.clearFocus();
                vCode2.clearFocus();
                return false;
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActive = false;
    }
}