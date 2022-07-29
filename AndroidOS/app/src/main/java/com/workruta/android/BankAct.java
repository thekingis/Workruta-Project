package com.workruta.android;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.workruta.android.Utils.Constants;
import com.workruta.android.Utils.SharedPrefMngr;
import com.workruta.android.Utils.StringUtils;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.workruta.android.PaymentsActivity.listenForUpdates;
import static com.workruta.android.Utils.Constants.allBanks;
import static com.workruta.android.Utils.Constants.allCars;

public class BankAct extends SharedCompatActivity implements AdapterView.OnItemSelectedListener {

    RelativeLayout mainView;
    EditText accountNoET, passwordET;
    TextView finish, headText;
    LinearLayout whiteFade, blackFade;
    Spinner bankSp;
    boolean requesting;
    String accountNo;
    int user, bankIndex;
    SharedPrefMngr sharedPrefMngr;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);
        sharedPrefMngr = new SharedPrefMngr(this);
        requesting = false;
        user = sharedPrefMngr.getMyId();

        mainView = findViewById(R.id.mainView);
        bankSp = findViewById(R.id.bank);
        passwordET = findViewById(R.id.passwordET);
        accountNoET = findViewById(R.id.accountNo);
        finish = findViewById(R.id.finish);
        headText = findViewById(R.id.headText);
        whiteFade = findViewById(R.id.whiteFade);
        blackFade = findViewById(R.id.blackFade);

        Arrays.sort(allBanks, String.CASE_INSENSITIVE_ORDER);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, allBanks);
        bankSp.setAdapter(adapter);
        bankSp.setOnItemSelectedListener(this);

        finish.setOnClickListener((v) -> submitForm());
        headText.setOnClickListener((v) -> onBackPressed());
        whiteFade.setOnClickListener((v) -> {
            return;
        });

        setupUI(mainView);
        getDetails();
    }

    @SuppressLint("InflateParams")
    private void getDetails() {
        RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.slide_loader, null);
        whiteFade.addView(layout);
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", String.valueOf(getMyId()))
                    .addFormDataPart("cat", "bank")
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.editorUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                if(response.isSuccessful()) {
                    String responseString = Objects.requireNonNull(response.body()).string();
                    JSONObject object = new JSONObject(responseString);
                    boolean available = object.getBoolean("available");
                    if(available) {
                        accountNo = object.getString("accountNo");
                        bankIndex = object.getInt("bankIndex");
                        accountNoET.setText(accountNo);
                        bankSp.setSelection(bankIndex);
                    }
                    whiteFade.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    @SuppressLint("InflateParams")
    private void submitForm(){
        accountNo = accountNoET.getText().toString();
        String password = passwordET.getText().toString();
        if(StringUtils.isEmpty(password) || StringUtils.isEmpty(accountNo) || bankIndex == 0){
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show();
            return;
        }
        if(accountNo.length() < 10 || accountNo.length() > 12){
            Toast.makeText(this, "Your Account Number is invalid", Toast.LENGTH_LONG).show();
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
                    .addFormDataPart("user", String.valueOf(user))
                    .addFormDataPart("bank", String.valueOf(bankIndex))
                    .addFormDataPart("accountNo", accountNo)
                    .addFormDataPart("password", password)
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.submitBankUrl)
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
                        JSONObject jsonObject = new JSONObject(dataStr);
                        sharedPrefMngr.detailBank(true);
                        receiveInfoChange("bank", jsonObject);
                        Toast.makeText(context, "Bank Details Saved", Toast.LENGTH_LONG).show();
                        listenForUpdates(context, "bankDetail", "attached");
                    } else
                        Toast.makeText(this, dataStr, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        bankIndex = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void onBackPressed(){
        if(!requesting) {
            if(blackFade.getVisibility() == View.VISIBLE){
                blackFade.setVisibility(View.GONE);
                return;
            }
            finish();
        }
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
                accountNoET.clearFocus();
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