package com.workruta.android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.workruta.android.Utils.Constants;
import com.workruta.android.Utils.Functions;

import org.json.JSONObject;

import java.util.Objects;

import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.workruta.android.Utils.Constants.www;
import static com.workruta.android.Utils.Functions.formatPhoneNumber;
import static com.workruta.android.Utils.Util.convertDateToString;

public class ProfileActivity extends SharedCompatActivity {

    LinearLayout linearLayout, whiteFade, blackFade;
    ImageView imageView;
    TextView headText, textView, sendMessage, changeBtn;
    ScrollView scrollView;
    int myId, userId;
    JSONObject jsonObject;
    boolean toShow, access;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        myId = getMyId();
        toShow = false;

        Bundle bundle = getIntent().getExtras();
        userId = bundle.getInt("userId");
        access = myId == userId;

        linearLayout = findViewById(R.id.linearLayout);
        whiteFade = findViewById(R.id.whiteFade);
        blackFade = findViewById(R.id.blackFade);
        imageView = findViewById(R.id.imageView);
        headText = findViewById(R.id.headText);
        textView = findViewById(R.id.textView);
        changeBtn = findViewById(R.id.changeBtn);
        sendMessage = findViewById(R.id.sendMessage);
        scrollView = findViewById(R.id.scrollView);

        try{
            jsonObject = new JSONObject();
            jsonObject.put("email", "Email");
            jsonObject.put("phone", "Phone Number");
            jsonObject.put("gender", "Gender");
            jsonObject.put("address", "Residential Address");
            jsonObject.put("plateNumber", "Plate Number");
            jsonObject.put("carProduct", "Car Make");
            jsonObject.put("carModel", "Car Model");
            jsonObject.put("bank", "Bank");
            jsonObject.put("accountNo", "Account Number");
            jsonObject.put("licenceNo", "Licence Number");
            jsonObject.put("licenceCat", "Licence Category");
            jsonObject.put("classIndex", "Class");
            jsonObject.put("stateIndex", "Issued State");
            jsonObject.put("isDate", "Issued Date");
            jsonObject.put("exDate", "Expiry Date");
            jsonObject.put("data", "Profile Details");
            jsonObject.put("licenceDetail", "Licence Details");
            jsonObject.put("carDetail", "Car Details");
            jsonObject.put("bankDetail", "Bank Details");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!access){
            sendMessage.setVisibility(View.VISIBLE);
        } else {
            changeBtn.setVisibility(View.VISIBLE);
            changeBtn.setOnClickListener((v) -> {
                Intent intent = new Intent(context, ChangePhotoAct.class);
                Bundle bundle1 = new Bundle();
                bundle1.putBoolean("backEnabled", true);
                intent.putExtras(bundle1);
                startActivity(intent);
            });
        }
        headText.setOnClickListener((v) -> onBackPressed());
        whiteFade.setOnClickListener((v) -> {
            return;
        });
        blackFade.setOnClickListener((v) -> {
            return;
        });
        scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            int maxScrll = textView.getTop() + textView.getHeight();
            toShow = scrollY > maxScrll;
            if(toShow){
                String s = textView.getText().toString();
                headText.setText(s);
            } else
                headText.setText("");
        });
        setOnInfoChangeListener(this::changePageInfo);
        
        getUserProfile();

    }

    private void changePageInfo(String key, JSONObject object) throws Exception {
        switch (key){
            case "photo":
                String photo = object.getString("photo");
                imageLoader.displayImage(photo, imageView);
                break;
            case "profile":
                String name = object.getString("name");
                textView.setText(name);
                if(toShow)
                    headText.setText(name);
                setNewInfo(object);
                break;
            default:
                setNewInfo(object);
                break;
        }
    }

    private void setNewInfo(JSONObject object) throws Exception {
        for(int i = 0; i < object.length(); i++){
            String key = Objects.requireNonNull(object.names()).getString(i);
            TextView textView = (TextView) linearLayout.findViewWithTag(key);
            if(!(textView == null)){
                String text = object.getString(key);
                switch (key) {
                    case "phone":
                        text = formatPhoneNumber(text);
                        break;
                    case "isDate":
                    case "exDate":
                        text = convertDateToString(text, false);
                        break;
                    case "classIndex":
                        text = Constants.classes[Integer.parseInt(text)];
                        break;
                    case "stateIndex":
                        text = Constants.allStates[Integer.parseInt(text)];
                        break;
                    case "bank":
                        text = Constants.allBanks[Integer.parseInt(text)];
                        break;
                }
                textView.setText(text);
            }
        }
    }

    @SuppressLint("InflateParams")
    private void getUserProfile() {
        RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.slide_loader, null);
        whiteFade.addView(layout);
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("myId", String.valueOf(myId))
                    .addFormDataPart("userId", String.valueOf(userId))
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.profileUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                if (response.isSuccessful()) {
                    whiteFade.setVisibility(View.GONE);
                    String responseString = Objects.requireNonNull(response.body()).string();
                    JSONObject object = new JSONObject(responseString);
                    JSONObject data = object.getJSONObject("data");
                    String name = data.getString("name");
                    String photo = www + data.getString("photo");
                    imageLoader.displayImage(photo, imageView);
                    textView.setText(name);
                    if(!access) {
                        String userEmail = functions.safeEmail(data.getString("email"));
                        sendMessage.setOnClickListener((v) -> {
                            Intent intent = new Intent(context, MessageActivity.class);
                            Bundle bundle1 = new Bundle();
                            bundle1.putString("photoUrl", photo);
                            bundle1.putString("name", name);
                            bundle1.putString("userEmail", userEmail);
                            bundle1.putInt("userId", userId);
                            intent.putExtras(bundle1);
                            startActivity(intent);
                        });
                    }
                    for(int i = 0; i < object.length(); i++){
                        String objKey = Objects.requireNonNull(object.names()).getString(i);
                        String textHead = jsonObject.getString(objKey);
                        JSONObject obj = object.getJSONObject(objKey);
                        LinearLayout layout1 = (LinearLayout) getLayoutInflater().inflate(R.layout.head_text_view, null);
                        TextView headTextView = layout1.findViewById(R.id.headTextView);
                        TextView editTextView = layout1.findViewById(R.id.editTextView);
                        headTextView.setText(textHead);
                        linearLayout.addView(layout1);
                        if(userId == myId){
                            editTextView.setVisibility(View.VISIBLE);
                            editTextView.setOnClickListener((v) -> editOption(objKey));
                        }
                        for(int x = 0; x < obj.length(); x++){
                            String key = Objects.requireNonNull(obj.names()).getString(x);
                            if(!key.equals("photo") && !key.equals("name") && !key.equals("filePath")){
                                if(!key.equals("address") || myId == userId){
                                    String headText = jsonObject.getString(key);
                                    String text = obj.getString(key);
                                    switch (key) {
                                        case "phone":
                                            text = formatPhoneNumber(text);
                                            break;
                                        case "isDate":
                                        case "exDate":
                                            text = convertDateToString(text, false);
                                            break;
                                        case "classIndex":
                                            text = Constants.classes[Integer.parseInt(text)];
                                            break;
                                        case "stateIndex":
                                            text = Constants.allStates[Integer.parseInt(text)];
                                            break;
                                        case "bank":
                                            text = Constants.allBanks[Integer.parseInt(text)];
                                            break;
                                    }
                                    LinearLayout layout2 = (LinearLayout) getLayoutInflater().inflate(R.layout.extra_text_view, null);
                                    TextView headTextView2 = layout2.findViewById(R.id.headTextView);
                                    TextView textView2 = layout2.findViewById(R.id.textView);
                                    textView2.setTag(key);
                                    headTextView2.setText(headText);
                                    textView2.setText(text);
                                    linearLayout.addView(layout2);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    private void editOption(String objKey) {
        Intent intent;
        switch (objKey){
            case "data":
                intent = new Intent(context, EditProfileActivity.class);
                break;
            case "licenceDetail":
                intent = new Intent(context, LicenceAct.class);
                break;
            case "carDetail":
                intent = new Intent(context, CarAct.class);
                break;
            case "bankDetail":
                intent = new Intent(context, BankAct.class);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + objKey);
        }
        startActivity(intent);
    }
}