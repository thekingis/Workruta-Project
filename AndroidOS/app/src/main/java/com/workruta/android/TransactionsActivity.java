package com.workruta.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.workruta.android.Utils.Constants;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kotlin.Pair;

import static com.workruta.android.Utils.Functions.stripeToDollar;

public class TransactionsActivity extends SharedCompatActivity {

    TextView headText;
    LinearLayout linearLayout, whiteFade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        headText = findViewById(R.id.headText);
        linearLayout = findViewById(R.id.linearLayout);
        whiteFade = findViewById(R.id.whiteFade);

        headText.setOnClickListener(v -> finish());

        getContents();

    }

    @SuppressLint("InflateParams")
    private void getContents() {
        RelativeLayout layoutX = (RelativeLayout) getLayoutInflater().inflate(R.layout.slide_loader, null, false);
        whiteFade.addView(layoutX);
        List<Pair<String, String>> parameters = new ArrayList<>();
        parameters.add(new Pair<>("user", String.valueOf(getMyId())));
        Fuel.INSTANCE.post(Constants.transactionsURL, parameters).responseString(new Handler<String>() {
            @Override
            public void success(String s) {
                runOnUiThread(() -> {
                    try {
                        whiteFade.setVisibility(View.GONE);
                        JSONArray dataArray = new JSONArray(s);
                        for(int i = 0; i < dataArray.length(); i++){
                            JSONObject object = dataArray.getJSONObject(i);
                            String name = object.getString("name");
                            String amount = object.getString("amount");
                            String date = object.getString("date");
                            String dateStr = functions.minify(date);
                            String money = stripeToDollar(amount);
                            boolean fromMe = object.getBoolean("fromMe");
                            int background = (i % 2) == 0 ? R.drawable.linter_ash : R.drawable.linter_white;
                            int icon = fromMe ? R.drawable.ic_arrow_circle_up : R.drawable.ic_arrow_circle_down;
                            int textColor = fromMe ? R.color.normalRed : R.color.green;
                            Drawable drawable = getResources().getDrawable(background);
                            RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.linter, null, false);
                            LinearLayout layout = relativeLayout.findViewById(R.id.layout);
                            ImageView imageView = relativeLayout.findViewById(R.id.imageView);
                            TextView nameTextView = relativeLayout.findViewById(R.id.nameTextView);
                            TextView costTextView = relativeLayout.findViewById(R.id.costTextView);
                            TextView dateTextView = relativeLayout.findViewById(R.id.dateTextView);
                            layout.setBackground(drawable);
                            imageView.setImageResource(icon);
                            nameTextView.setText(name);
                            costTextView.setText(money);
                            dateTextView.setText(dateStr);
                            costTextView.setTextColor(ContextCompat.getColor(context, textColor));
                            linearLayout.addView(relativeLayout);
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
}