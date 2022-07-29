package com.workruta.android;

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
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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

import static com.workruta.android.PaymentsActivity.listenForUpdates;
import static com.workruta.android.Utils.Constants.allCars;
import static com.workruta.android.Utils.Constants.www;
import static com.workruta.android.Utils.Util.convertStringToArrayInt;

public class CarAct extends SharedCompatActivity implements AdapterView.OnItemSelectedListener {

    RelativeLayout mainView;
    EditText plateNoET, carModelET, carProductET, passwordET;
    TextView next, toggleCar, headText;
    LinearLayout whiteFade, blackFade;
    Spinner carProductSp;
    boolean requesting;
    String plateNumber, carModel, carProduct;
    int user, carProductIndex, toggleIndex;
    SharedPrefMngr sharedPrefMngr;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);
        sharedPrefMngr = new SharedPrefMngr(this);
        requesting = false;
        toggleIndex = 1;
        user = sharedPrefMngr.getMyId();

        mainView = findViewById(R.id.mainView);
        plateNoET = findViewById(R.id.plateNo);
        carModelET = findViewById(R.id.model);
        passwordET = findViewById(R.id.passwordET);
        carProductET = findViewById(R.id.tProduct);
        carProductSp = findViewById(R.id.sProduct);
        next = findViewById(R.id.next);
        toggleCar = findViewById(R.id.toggleCar);
        headText = findViewById(R.id.headText);
        whiteFade = findViewById(R.id.whiteFade);
        blackFade = findViewById(R.id.blackFade);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, allCars);
        carProductSp.setAdapter(adapter);
        carProductSp.setOnItemSelectedListener(this);

        toggleCar.setOnClickListener((v) -> toggleInputOption());
        next.setOnClickListener((v) -> submitForm());
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
                    .addFormDataPart("cat", "car")
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
                        plateNumber = object.getString("plateNumber");
                        carModel = object.getString("carModel");
                        carProduct = object.getString("carProduct");
                        int carIndex = adapter.getPosition(carProduct);
                        plateNoET.setText(plateNumber);
                        carModelET.setText(carModel);
                        if(carIndex > -1)
                            carProductSp.setSelection(carIndex);
                        else {
                            carProductET.setText(carProduct);
                            toggleInputOption();
                        }
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
        plateNumber = plateNoET.getText().toString();
        carModel = carModelET.getText().toString();
        carProduct = carProductET.getText().toString();
        String password = passwordET.getText().toString();
        if(StringUtils.isEmpty(password) || StringUtils.isEmpty(plateNumber) || StringUtils.isEmpty(carModel)
                || (StringUtils.isEmpty(carProduct) && carProductIndex == 0)){
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show();
            return;
        }
        requesting = true;
        if(toggleIndex == 1)
            carProduct = allCars[carProductIndex];
        whiteFade.setVisibility(View.VISIBLE);
        if(whiteFade.getChildCount() > 0)
            whiteFade.removeAllViews();
        RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.slide_loader, null);
        whiteFade.addView(layout);
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", String.valueOf(user))
                    .addFormDataPart("plateNumber", plateNumber)
                    .addFormDataPart("carProduct", carProduct)
                    .addFormDataPart("carModel", carModel)
                    .addFormDataPart("password", password)
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.submitCarUrl)
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
                        sharedPrefMngr.detailCar(true);
                        receiveInfoChange("car", jsonObject);
                        Toast.makeText(context, "Car Details Saved", Toast.LENGTH_LONG).show();
                        listenForUpdates(context, "carDetail", "attached");
                    } else
                        Toast.makeText(this, dataStr, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    private void toggleInputOption() {
        if(toggleIndex == 1){
            toggleIndex = 2;
            carProductSp.setVisibility(View.GONE);
            carProductET.setVisibility(View.VISIBLE);
            toggleCar.setText(R.string.select_car_product);
        } else {
            toggleIndex = 1;
            carProductET.setVisibility(View.GONE);
            carProductSp.setVisibility(View.VISIBLE);
            toggleCar.setText(R.string.type_car_product);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        carProductIndex = position;
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
                plateNoET.clearFocus();
                carModelET.clearFocus();
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