package com.workruta.android;

import android.accounts.Account;
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
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.workruta.android.Utils.Constants;
import com.workruta.android.Utils.SharedPrefMngr;
import com.workruta.android.Utils.StringUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.workruta.android.Utils.Constants.country;
import static com.workruta.android.Utils.Constants.months;

public class SignFormAct extends SharedCompatActivity {

    RelativeLayout mainView;
    LinearLayout whiteFade, blackFade;
    TextView townTextVw, signUp, returnBack;
    EditText firstNameET, lastNameET, phoneET, emailET, passwordET, conPassET, addressET;
    RadioGroup radio;
    String phoneNumber, address;
    boolean requesting;
    double latitude, longitude;
    SharedPrefMngr sharedPrefMngr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_form);
        sharedPrefMngr = new SharedPrefMngr(this);

        Places.initialize(getApplicationContext(), Constants.mapAPIKey);
        requesting = false;

        mainView = findViewById(R.id.mainView);
        whiteFade = findViewById(R.id.whiteFade);
        blackFade = findViewById(R.id.blackFade);
        firstNameET = findViewById(R.id.firstNameET);
        lastNameET = findViewById(R.id.lastNameET);
        phoneET = findViewById(R.id.phoneET);
        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        conPassET = findViewById(R.id.conPassET);
        radio = findViewById(R.id.gender);
        addressET = findViewById(R.id.addressET);
        townTextVw = findViewById(R.id.townTextVw);
        signUp = findViewById(R.id.signUp);
        returnBack = findViewById(R.id.returnBack);

        addressET.setFocusable(false);
        signUp.setOnClickListener(v -> submitForm());
        returnBack.setOnClickListener(v -> {
            sharedPrefMngr.clearCachedNumber();
            finish();
            if(!SignupAct.isActive)
                startActivity(new Intent(this, SignupAct.class));
        });
        addressET.setOnClickListener((v) -> openAddressAutoComplete());

        setupUI(mainView);
        boolean checkCachedNumber = sharedPrefMngr.checkCachedNumber(), verified = sharedPrefMngr.getVerification();
        if(checkCachedNumber){
            if(verified) {
                phoneNumber = sharedPrefMngr.getPhoneNumber();
                String newStr = "+1 ";
                newStr += phoneNumber.substring(0, 3) + " ";
                newStr += phoneNumber.substring(3, 7) + " ";
                newStr += phoneNumber.substring(7);
                phoneET.setText(newStr);
            } else {
                finish();
                startActivity(new Intent(this, SignupAct.class));
            }
        } else {
            finish();
            startActivity(new Intent(this, SignupAct.class));
        }

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

    private void submitForm(){
        String fName = firstNameET.getText().toString(),
                lName = lastNameET.getText().toString(),
                email = emailET.getText().toString(),
                password = passwordET.getText().toString(),
                conPass = conPassET.getText().toString(),
                address = addressET.getText().toString();
        int radioId = radio.getCheckedRadioButtonId();
        if(StringUtils.isEmpty(fName) || StringUtils.isEmpty(lName) || StringUtils.isEmpty(email) || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(address) || StringUtils.isEmpty(conPass) || radioId == -1){
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show();
            return;
        }
        requesting = true;
        RadioButton selGnd = radio.findViewById(radioId);
        String gender = selGnd.getText().toString();
        whiteFade.setVisibility(View.VISIBLE);
        if(whiteFade.getChildCount() > 0)
            whiteFade.removeAllViews();
        @SuppressLint("InflateParams") RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.slide_loader, null);
        whiteFade.addView(layout);
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("fName", fName)
                    .addFormDataPart("lName", lName)
                    .addFormDataPart("email", email)
                    .addFormDataPart("phoneNumber", phoneNumber)
                    .addFormDataPart("address", address)
                    .addFormDataPart("password", password)
                    .addFormDataPart("conPass", conPass)
                    .addFormDataPart("gender", gender)
                    .addFormDataPart("latitude", String.valueOf(latitude))
                    .addFormDataPart("longitude", String.valueOf(longitude))
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.signUpUrl)
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
                        int id = Integer.parseInt(dataStr);
                        String name = fName+" "+lName;
                        String safeEmail = functions.safeEmail(email);
                        sharedPrefMngr.storeUserInfo(id, null, name, email);
                        DatabaseReference database = FirebaseDatabase.getInstance().getReference(safeEmail);
                        Map<String, String> userData = new HashMap<>();
                        userData.put("userId", String.valueOf(id));
                        userData.put("name", name);
                        createPaymentAccount(id, safeEmail);
                        database.setValue(userData);
                        startActivity(new Intent(this, FinishSetupAct.class));
                        finish();
                    } else
                        Toast.makeText(this, dataStr, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    private void createPaymentAccount(int userId, String safeEmail) throws StripeException {
        String accountKey = "acct_" + userId + "_" + safeEmail;
        Stripe.apiKey = Constants.stripeSKTestAPIKey;
        AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                .setAccount(accountKey)
                .setRefreshUrl(Constants.www)
                .setReturnUrl(Constants.www)
                .build();
    }

    public void onBackPressed(){
        if(blackFade.getVisibility() == View.VISIBLE){
            blackFade.setVisibility(View.GONE);
            return;
        }
        if(!requesting){
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
            negativeBtn.setOnClickListener((v) -> {blackFade.setVisibility(View.GONE);});
            positiveBtn.setOnClickListener((v) -> android.os.Process.killProcess(android.os.Process.myPid()));
            blackFade.addView(alertLayout);
            blackFade.setVisibility(View.VISIBLE);
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
                emailET.clearFocus();
                passwordET.clearFocus();
                conPassET.clearFocus();
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