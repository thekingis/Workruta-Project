package com.workruta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.workruta.android.Utils.Constants;
import com.workruta.android.Utils.SharedPrefMngr;
import com.workruta.android.Utils.StringUtils;

import org.json.JSONObject;

import java.util.Objects;

import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.workruta.android.Utils.Constants.www;

public class SigninAct extends SharedCompatActivity {

    RelativeLayout mainView;
    EditText emailET, passwordET;
    TextView forgotPass, signIn;
    LinearLayout whiteFade;
    boolean requesting;
    SharedPrefMngr sharedPrefMngr;
    Activity startAct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        startAct = StartAct.activity;
        sharedPrefMngr = new SharedPrefMngr(this);
        requesting = false;

        mainView = findViewById(R.id.mainView);
        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        forgotPass = findViewById(R.id.forgotPass);
        signIn = findViewById(R.id.signIn);
        whiteFade = findViewById(R.id.whiteFade);

        signIn.setOnClickListener(v -> submitForm());
        forgotPass.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPassActivity.class));
        });

        setupUI(mainView);
    }

    @SuppressLint("InflateParams")
    private void submitForm(){
        String email = emailET.getText().toString(),
                password = passwordET.getText().toString();
        if(StringUtils.isEmpty(email) || StringUtils.isEmpty(password)){
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show();
            return;
        }
        requesting = true;
        whiteFade.setVisibility(View.VISIBLE);
        if(whiteFade.getChildCount() > 0)
            whiteFade.removeAllViews();
        RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.slide_loader, null);
        whiteFade.addView(layout);
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("email", email)
                    .addFormDataPart("password", password)
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.signInUrl)
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
                        boolean fingerEnabled = sharedPrefMngr.getFingerPrintEnabled();
                        object = new JSONObject(dataStr);
                        int id = object.getInt("id");
                        String photo = www + object.getString("photo");
                        String fName = object.getString("fName");
                        String lName = object.getString("lName");
                        String phone = object.getString("phone");
                        sharedPrefMngr.storeUserInfo(id, photo, fName+" "+lName, email);
                        sharedPrefMngr.cachePhoneNumber(phone);
                        sharedPrefMngr.verifyLicence(true);
                        sharedPrefMngr.detailBank(true);
                        sharedPrefMngr.detailCar(true);
                        sharedPrefMngr.verifyLicence(true);
                        if(fingerEnabled){
                            int fingerId = sharedPrefMngr.getFingerId();
                            if(fingerId != id)
                                sharedPrefMngr.clearFingerCache();
                            else
                                sharedPrefMngr.saveFingerCache(id, photo, fName+" "+lName, email, phone);
                        }
                        if(!(startAct == null))
                            startAct.finish();
                        finish();
                        if(StringUtils.isEmpty(photo)) {
                            Intent intent = new Intent(this, ChangePhotoAct.class);
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("backEnabled", false);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            return;
                        }
                        startActivity(new Intent(this, DashboardAct.class));
                    } else
                        Toast.makeText(this, dataStr, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    public void onBackPressed(){
        if(!requesting)
            finish();
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(v);
                emailET.clearFocus();
                passwordET.clearFocus();
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
}