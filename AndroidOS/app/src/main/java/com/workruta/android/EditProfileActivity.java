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
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.workruta.android.Utils.Constants;
import com.workruta.android.Utils.StringUtils;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.workruta.android.Utils.Constants.country;
import static com.workruta.android.Utils.Constants.www;

public class EditProfileActivity extends SharedCompatActivity {

    RelativeLayout mainView;
    EditText firstNameET, lastNameET, addressET, passwordET;
    TextView saveBtn, headText;
    LinearLayout whiteFade, blackFade;
    RadioGroup radio;
    boolean requesting;
    int myId;
    int[] radioViews;
    String address;
    double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        myId = getMyId();
        requesting = false;
        radioViews = new int[]{R.id.female, R.id.male, R.id.other};

        mainView = findViewById(R.id.mainView);
        whiteFade = findViewById(R.id.whiteFade);
        blackFade = findViewById(R.id.blackFade);
        firstNameET = findViewById(R.id.firstNameET);
        lastNameET = findViewById(R.id.lastNameET);
        passwordET = findViewById(R.id.passwordET);
        radio = findViewById(R.id.gender);
        addressET = findViewById(R.id.addressET);
        headText = findViewById(R.id.headText);
        saveBtn = findViewById(R.id.saveBtn);

        addressET.setFocusable(false);
        whiteFade.setOnClickListener(v -> {
            return;
        });
        blackFade.setOnClickListener(v -> {
            return;
        });
        saveBtn.setOnClickListener(v -> submitForm());
        headText.setOnClickListener(v -> onBackPressed());
        addressET.setOnClickListener((v) -> openAddressAutoComplete());

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
                    .addFormDataPart("cat", "profile")
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
                    address = object.getString("address");
                    String fName = object.getString("fName");
                    String lName = object.getString("lName");
                    String gender = object.getString("gender");
                    firstNameET.setText(fName);
                    lastNameET.setText(lName);
                    addressET.setText(address);
                    int index = -1;
                    switch (gender){
                        case "Female":
                            index = 0;
                            break;
                        case "Male":
                            index = 1;
                            break;
                        case "Other":
                            index = 2;
                            break;
                    }
                    RadioButton radioButton = radio.findViewById(radioViews[index]);
                    radioButton.setChecked(true);
                    whiteFade.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    @SuppressLint("InflateParams")
    private void submitForm(){
        String fName = firstNameET.getText().toString(),
                lName = lastNameET.getText().toString(),
                password = passwordET.getText().toString(),
                address = addressET.getText().toString();
        int radioId = radio.getCheckedRadioButtonId();
        if(StringUtils.isEmpty(fName) || StringUtils.isEmpty(lName) || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(address) || radioId == -1){
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show();
            return;
        }
        RadioButton selGnd = radio.findViewById(radioId);
        String gender = selGnd.getText().toString();
        requesting = true;
        whiteFade.setVisibility(View.VISIBLE);
        if(whiteFade.getChildCount() > 0)
            whiteFade.removeAllViews();
        RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.slide_loader, null);
        whiteFade.addView(layout);
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", String.valueOf(myId))
                    .addFormDataPart("fName", fName)
                    .addFormDataPart("lName", lName)
                    .addFormDataPart("address", address)
                    .addFormDataPart("password", password)
                    .addFormDataPart("gender", gender)
                    .addFormDataPart("latitude", String.valueOf(latitude))
                    .addFormDataPart("longitude", String.valueOf(longitude))
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.saveInfoUrl)
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
                        String email = jsonObject.getString("email");
                        String photo = www + jsonObject.getString("photo");
                        jsonObject.put("name", fName + " " +lName);
                        sharedPrefMngr.storeUserInfo(myId, photo, fName+" "+lName, email);
                        receiveInfoChange("profile", jsonObject);
                        Toast.makeText(this, "Data Saved", Toast.LENGTH_LONG).show();
                    } else
                        Toast.makeText(this, dataStr, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    private void openAddressAutoComplete() {
        List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList)
                .setCountry(country).build(getApplicationContext());
        startActivityIntent.launch(intent);
    }

    private final ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getData() != null && result.getResultCode() == RESULT_OK) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    address = place.getAddress();
                    latitude = Objects.requireNonNull(place.getLatLng()).latitude;
                    longitude = place.getLatLng().longitude;
                    addressET.setText(address);
                }
            });

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
                firstNameET.clearFocus();
                lastNameET.clearFocus();
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