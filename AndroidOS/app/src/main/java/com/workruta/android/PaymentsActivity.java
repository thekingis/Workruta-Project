package com.workruta.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.workruta.android.Utils.Constants;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kotlin.Pair;

public class PaymentsActivity extends SharedCompatActivity {

    TextView headText;
    @SuppressLint("StaticFieldLeak")
    static LinearLayout linearLayout;
    JSONObject titles, bgImages;
    static JSONObject states;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);

        try {
            titles = new JSONObject();
            titles.put("idCard", "Identity Card");
            titles.put("licenceDetail", "Driver's Licence");
            titles.put("carDetail", "Car Details");
            titles.put("bankDetail", "Bank Details");
            bgImages = new JSONObject();
            bgImages.put("idCard", R.drawable.details_idcard);
            bgImages.put("licenceDetail", R.drawable.details_licence);
            bgImages.put("carDetail", R.drawable.details_car);
            bgImages.put("bankDetail", R.drawable.details_bank);
            states = new JSONObject();
            states.put("expired", new int[]{R.string.idExpired, R.color.normalRed});
            states.put("pending", new int[]{R.string.pendingVer, R.color.quantum_yellow});
            states.put("verified", new int[]{R.string.idVerified, R.color.green});
            states.put("invalid", new int[]{R.string.invalidID, R.color.normalRed});
            states.put("attached", new int[]{R.string.infoAttached, R.color.green});
            states.put("none", new int[]{R.string.notAttached, R.color.normalRed});
        } catch (Exception e) {
            e.printStackTrace();
        }

        headText = findViewById(R.id.headText);
        linearLayout = findViewById(R.id.linearLayout);

        headText.setOnClickListener(v -> finish());

        setCardBoxes();

    }

    @SuppressLint("InflateParams")
    private void setCardBoxes() {
        try {
            for(int i = 0; i < titles.length(); i++){
                String key = Objects.requireNonNull(titles.names()).getString(i);
                String title = titles.getString(key);
                int bgImage = bgImages.getInt(key);
                RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.card_layout, null, false);
                ImageView bgImageView = relativeLayout.findViewById(R.id.bgImageView);
                TextView titleTextView = relativeLayout.findViewById(R.id.titleTextView);
                relativeLayout.setTag(key);
                titleTextView.setText(title);
                bgImageView.setBackgroundResource(bgImage);
                linearLayout.addView(relativeLayout);
            }
            getDataContents();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getDataContents() {
        List<Pair<String, String>> parameters = new ArrayList<>();
        parameters.add(new Pair<>("user", String.valueOf(getMyId())));
        Fuel.INSTANCE.post(Constants.getDataURL, parameters).responseString(new Handler<String>() {
            @Override
            public void success(String s) {
                runOnUiThread(() -> {
                    try {
                        JSONObject dataObject = new JSONObject(s);
                        for(int i = 0; i < dataObject.length(); i++){
                            String key = Objects.requireNonNull(dataObject.names()).getString(i);
                            String stateKey = dataObject.getString(key);
                            int[] state = (int[]) states.get(stateKey);
                            int stateText = state[0];
                            int stateColor = state[1];
                            int textColor = stateKey.equals("pending") ? R.color.black : R.color.white;
                            RelativeLayout relativeLayout = linearLayout.findViewWithTag(key);
                            ImageView cupertino = relativeLayout.findViewById(R.id.cupertino);
                            LinearLayout layout = relativeLayout.findViewById(R.id.layout);
                            LinearLayout textHolder = relativeLayout.findViewById(R.id.textHolder);
                            TextView stateTextView = relativeLayout.findViewById(R.id.stateTextView);
                            LinearLayout editBtn = relativeLayout.findViewById(R.id.editBtn);
                            cupertino.setVisibility(View.GONE);
                            layout.setVisibility(View.VISIBLE);
                            stateTextView.setText(stateText);
                            textHolder.setBackgroundColor(ContextCompat.getColor(context, stateColor));
                            stateTextView.setTextColor(ContextCompat.getColor(context, textColor));
                            editBtn.setOnClickListener(v -> listenToClick(key));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void failure(@NotNull FuelError fuelError) {
            }
        });
    }

    private void listenToClick(String key) {
        Intent intent;
        switch (key) {
            case "idCard":
                intent = new Intent(context, IdentityActivity.class);
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
                throw new IllegalStateException("Unexpected value: " + key);
        }
        startActivity(intent);
    }

    public static void listenForUpdates(Context context, String key, String stateKey){
        if(linearLayout != null) {
            try {
                int[] state = (int[]) states.get(stateKey);
                int stateText = state[0];
                int stateColor = state[1];
                int textColor = stateKey.equals("pending") ? R.color.black : R.color.white;
                RelativeLayout relativeLayout = linearLayout.findViewWithTag(key);
                LinearLayout textHolder = relativeLayout.findViewById(R.id.textHolder);
                TextView stateTextView = relativeLayout.findViewById(R.id.stateTextView);
                stateTextView.setText(stateText);
                stateTextView.setTextColor(context.getResources().getColor(textColor));
                textHolder.setBackgroundColor(context.getResources().getColor(stateColor));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}