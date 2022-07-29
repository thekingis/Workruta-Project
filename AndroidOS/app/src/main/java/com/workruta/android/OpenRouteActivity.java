package com.workruta.android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

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
import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.workruta.android.Utils.Constants.monthsOnly;
import static com.workruta.android.Utils.Constants.www;
import static com.workruta.android.Utils.Functions.formatPhoneNumber;
import static com.workruta.android.Utils.Functions.getTripCost;
import static com.workruta.android.Utils.Util.convertDateToArray;
import static com.workruta.android.Utils.Util.convertDateToString;

public class OpenRouteActivity extends SharedCompatActivity {

    LinearLayout resultLayout, whiteFade, blackFade, menuBtn, menuLayout, dummyLayout, bottomSheet;
    TextView headText, nameTextView;
    ImageView imgView;
    ImageButton imageBtn;
    String routeId, routeIdTo, routerId, userId, pathKey;
    int[] icons;
    JSONObject optionsObject;
    public static boolean isPaused, hasBeenEdited, requesting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_route);
        optionsObject = new JSONObject();
        hasBeenEdited = false;
        requesting = false;
        isPaused = false;

        Bundle bundle = getIntent().getExtras();
        routeId = bundle.getString("routeId");

        try {
            optionsObject.put("viewOwnRoute", new int[]{R.drawable.ic_carm, R.string.viewOwnRoute});
            optionsObject.put("respond", new int[]{R.drawable.ic_respond, R.string.respondToRequest});
            optionsObject.put("viewMergedRoute", new int[]{R.drawable.ic_carm, R.string.viewMergedRoute});
            optionsObject.put("searchForRoutes", new int[]{R.drawable.ic_search, R.string.searching_for_matching_routes});
            optionsObject.put("canEdit", new int[]{R.drawable.ic_edit, R.string.edit_route});
            optionsObject.put("canCancel", new int[]{R.drawable.ic_cancel, R.string.cancel});
            optionsObject.put("hasPassengers", new int[]{R.drawable.ic_group_c, R.string.viewUsers});
            optionsObject.put("canStart", new int[]{R.drawable.ic_start, R.string.startRide});
            optionsObject.put("canEnd", new int[]{R.drawable.ic_stop, R.string.end_route});
        } catch (Exception e) {
            e.printStackTrace();
        }

        icons = new int[]{
                R.drawable.ic_location_from,
                R.drawable.ic_location_to,
                R.drawable.ic_length,
                R.drawable.ic_time,
                R.drawable.ic_date,
                R.drawable.ic_money,
                R.drawable.ic_group,
                R.drawable.ic_group_add
        };

        resultLayout = findViewById(R.id.resultLayout);
        blackFade = findViewById(R.id.blackFade);
        whiteFade = findViewById(R.id.whiteFade);
        headText = findViewById(R.id.headText);
        nameTextView = findViewById(R.id.nameTextView);
        imageBtn = findViewById(R.id.imageBtn);
        menuBtn = findViewById(R.id.menuBtn);
        menuLayout = findViewById(R.id.menuLayout);
        bottomSheet = findViewById(R.id.bottomSheet);
        dummyLayout = findViewById(R.id.dummyLayout);
        imgView = findViewById(R.id.imgView);

        headText.setOnClickListener((v) -> finish());
        whiteFade.setOnClickListener((v) -> {
            return;
        });
        menuBtn.setOnClickListener((v) -> {
            if(menuLayout.getVisibility() == View.VISIBLE) {
                menuLayout.setVisibility(View.GONE);
                dummyLayout.setVisibility(View.GONE);
            } else {
                menuLayout.setVisibility(View.VISIBLE);
                dummyLayout.setVisibility(View.VISIBLE);
            }
        });
        imageBtn.setOnClickListener((v) -> {
            if(menuLayout.getVisibility() == View.VISIBLE) {
                menuLayout.setVisibility(View.GONE);
                dummyLayout.setVisibility(View.GONE);
            } else {
                menuLayout.setVisibility(View.VISIBLE);
                dummyLayout.setVisibility(View.VISIBLE);
            }
        });
        dummyLayout.setOnClickListener((v) -> hideMenuBox());
        bottomSheet.setOnClickListener((v) -> {
            return;
        });

        getRouteContents();
    }

    @SuppressLint("InflateParams")
    private void getRouteContents() {
        RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.slide_loader, null);
        whiteFade.addView(layout);
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("routeId", routeId)
                    .addFormDataPart("user", String.valueOf(getMyId()))
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.routeURL)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                if (response.isSuccessful()) {
                    whiteFade.setVisibility(View.GONE);
                    String responseString = Objects.requireNonNull(response.body()).string();
                    JSONObject object = new JSONObject(responseString);
                    routeIdTo = object.getString("routeIdTo");
                    routerId = object.getString("routerId");
                    userId = object.getString("userId");
                    pathKey = object.getString("pathKey");
                    String name = object.getString("name");
                    String photo = www + object.getString("photo");
                    String type = object.getString("type");
                    String locationFrom = object.getString("locationFrom");
                    String locationTo = object.getString("locationTo");
                    String passengers = object.getString("passengers");
                    String passNum = object.getString("passNum");
                    String passN = object.getString("passN");
                    String routeDate = object.getString("routeDate");
                    JSONObject options = object.optJSONObject("options");
                    boolean canRespond = object.getBoolean("canRespond");
                    boolean notResponded = object.getBoolean("notResponded");
                    boolean freeRide = object.getBoolean("freeRide");
                    double latitudeFrom = object.getDouble("latitudeFrom");
                    double longitudeFrom = object.getDouble("longitudeFrom");
                    double latitudeTo = object.getDouble("latitudeTo");
                    double longitudeTo = object.getDouble("longitudeTo");
                    double distance = functions.getDistance(latitudeFrom, longitudeFrom, latitudeTo, longitudeTo, null);
                    String time = functions.getRouteTime(latitudeFrom, longitudeFrom, latitudeTo, longitudeTo);
                    String cost = freeRide ? "Free" : getTripCost(distance);
                    JSONObject newObject = new JSONObject();
                    newObject.put("id", routeId);
                    newObject.put("type", type);
                    newObject.put("locationFrom", locationFrom);
                    newObject.put("locationTo", locationTo);
                    newObject.put("routeDate", routeDate);
                    newObject.put("passNum", passN);
                    newObject.put("freeRide", freeRide);
                    newObject.put("latitudeFrom", latitudeFrom);
                    newObject.put("longitudeFrom", longitudeFrom);
                    newObject.put("latitudeTo", latitudeTo);
                    newObject.put("longitudeTo", longitudeTo);
                    String[] strings = new String[]{
                            locationFrom,
                            locationTo,
                            distance + "mi",
                            time,
                            convertDateToString(routeDate, true),
                            cost,
                            passNum,
                            passengers
                    };
                    imageLoader.displayImage(photo, imgView);
                    nameTextView.setText(name);
                    for(int i = 0; i < icons.length; i++){
                        if(type.equals("P") || i < 5) {
                            LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.option_text, null);
                            TextView textView = linearLayout.findViewById(R.id.textView);
                            String text = strings[i];
                            int icon = icons[i];
                            textView.setElegantTextHeight(true);
                            textView.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                            textView.setSingleLine(false);
                            textView.setText(text);
                            textView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
                            resultLayout.addView(linearLayout, i);
                        }
                    }
                    if(options != null){
                        bottomSheet.setVisibility(View.VISIBLE);
                        if(canRespond && notResponded)
                            options.put("respond", true);
                        for(int x = 0; x < options.length(); x++){
                            String key = Objects.requireNonNull(options.names()).getString(x);
                            boolean isTrue = options.getBoolean(key);
                            if((isTrue && !key.equals("canFollow")) || (key.equals("isFollowingRide") && options.getBoolean("canFollow"))){
                                int menuText, menuIcon = ((int[]) optionsObject.get(key))[0];
                                if(key.equals("isFollowingRide")){
                                    menuText = isTrue ? R.string.unfollow_ride : R.string.follow_ride;
                                } else {
                                    menuText = ((int[]) optionsObject.get(key))[1];
                                }
                                LinearLayout optionText = (LinearLayout) getLayoutInflater().inflate(R.layout.option_text, null);
                                TextView textView = optionText.findViewById(R.id.textView);
                                textView.setTag(isTrue);
                                textView.setText(menuText);
                                textView.setTextColor(ContextCompat.getColor(context, R.color.mainColor));
                                textView.setCompoundDrawablesWithIntrinsicBounds(menuIcon, 0, 0, 0);
                                textView.setOnClickListener(v -> listenToMenuClick(optionText, key, newObject));
                                menuLayout.addView(optionText);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    private void listenToMenuClick(LinearLayout optionText, String key, JSONObject object) {
        hideMenuBox();
        Intent intent;
        Bundle bundle = new Bundle();
        switch (key){
            case "viewOwnRoute":
                bundle.putString("routeId", routeIdTo);
                intent = new Intent(context, OpenRouteActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case "viewMergedRoute":
                bundle.putString("routeIdFrom", routeId);
                bundle.putString("routeIdTo", routeIdTo);
                intent = new Intent(context, RouteActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case "searchForRoutes":
                startRouteSearch();
                break;
            case "hasPassengers":
                showPassengers();
                break;
            case "canEdit":
                editRoute(object);
                break;
            case "canEnd":
            case "respond":
            case "canStart":
            case "canCancel":
                displayAction(key, optionText);
                break;
        }
    }

    @SuppressLint("InflateParams")
    private void displayAction(String key, LinearLayout optionText) {
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        String negTxt = getResources().getString(R.string.no),
                posTxt = getResources().getString(R.string.yes),
                alertTxt = getResources().getString(R.string.cancel_route);
        if(key.equals("respond")){
            negTxt = "Reject";
            posTxt = "Accept";
            alertTxt = "Respond to Request";
        } else if(key.equals("canStart")){
            negTxt = "Cancel";
            posTxt = "Start";
            alertTxt = "Do you want to start this ride?";
        } else if(key.equals("canEnd")){
            negTxt = "Cancel";
            posTxt = "End";
            alertTxt = "Do you want to end this ride?";
        }
        LinearLayout alertLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.alert_layout, null);
        TextView textView = alertLayout.findViewById(R.id.textView);
        Button negativeBtn = alertLayout.findViewById(R.id.negative);
        Button positiveBtn = alertLayout.findViewById(R.id.positive);
        textView.setText(alertTxt);
        negativeBtn.setText(negTxt);
        positiveBtn.setText(posTxt);
        negativeBtn.setOnClickListener((v) -> {
            blackFade.setVisibility(View.GONE);
            if(key.equals("respond"))
                respondToRequest(optionText, "rejected");
        });
        positiveBtn.setOnClickListener((v) -> {
            blackFade.setVisibility(View.GONE);
            if(key.equals("respond")){
                respondToRequest(optionText, "accepted");
                return;
            }
            if(key.equals("canStart")){
                startRoute(optionText);
                return;
            }
            if(key.equals("canEnd")){
                endRoute(optionText);
                return;
            }
            cancelRoute();
        });
        blackFade.addView(alertLayout);
        blackFade.setVisibility(View.VISIBLE);
    }

    private void endRoute(LinearLayout optionText) {
        requesting = true;
        whiteFade.setVisibility(View.VISIBLE);
        new android.os.Handler().postDelayed(() -> {
            List<Pair<String, String>> parameters = new ArrayList<>();
            parameters.add(new Pair<>("user", String.valueOf(getMyId())));
            parameters.add(new Pair<>("routeId", routeId));
            parameters.add(new Pair<>("action", "endRoute"));
            Fuel.INSTANCE.post(Constants.actionsUrl, parameters).responseString(new Handler<String>() {
                @Override
                public void success(String s) {
                    runOnUiThread(() -> {
                        try {
                            requesting = false;
                            whiteFade.setVisibility(View.GONE);
                            JSONObject object = new JSONObject(s);
                            boolean noError = object.getBoolean("noError");
                            if(noError) {
                                optionText.setVisibility(View.GONE);
                                Toast.makeText(context, "Ride Ended", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void failure(@NotNull FuelError fuelError) {
                    runOnUiThread(() -> {
                        whiteFade.setVisibility(View.GONE);
                        requesting = false;
                        Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }, 1000);
    }

    private void startRoute(LinearLayout optionText) {
        requesting = true;
        whiteFade.setVisibility(View.VISIBLE);
        new android.os.Handler().postDelayed(() -> {
            List<Pair<String, String>> parameters = new ArrayList<>();
            parameters.add(new Pair<>("user", String.valueOf(getMyId())));
            parameters.add(new Pair<>("routeId", routeId));
            parameters.add(new Pair<>("action", "startRoute"));
            Fuel.INSTANCE.post(Constants.actionsUrl, parameters).responseString(new Handler<String>() {
                @Override
                public void success(String s) {
                    runOnUiThread(() -> {
                        try {
                            requesting = false;
                            whiteFade.setVisibility(View.GONE);
                            JSONObject object = new JSONObject(s);
                            boolean noError = object.getBoolean("noError");
                            if(noError) {
                                View view1 = menuLayout.getChildAt(menuLayout.getChildCount() - 1);
                                View view2 = menuLayout.getChildAt(menuLayout.getChildCount() - 2);
                                view1.setVisibility(View.GONE);
                                view2.setVisibility(View.GONE);
                                TextView textView = optionText.findViewById(R.id.textView);
                                textView.setText(R.string.end_route);
                                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stop, 0, 0, 0);
                                textView.setOnClickListener(v -> listenToMenuClick(optionText, "canEnd", null));
                                Toast.makeText(context, "Ride Started", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void failure(@NotNull FuelError fuelError) {
                    runOnUiThread(() -> {
                        whiteFade.setVisibility(View.GONE);
                        requesting = false;
                        Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }, 1000);
    }

    private void respondToRequest(LinearLayout optionText, String action) {
        requesting = true;
        whiteFade.setVisibility(View.VISIBLE);
        new android.os.Handler().postDelayed(() -> {
            List<Pair<String, String>> parameters = new ArrayList<>();
            parameters.add(new Pair<>("key", pathKey));
            parameters.add(new Pair<>("user", String.valueOf(getMyId())));
            parameters.add(new Pair<>("userTo", userId));
            parameters.add(new Pair<>("dataId", routerId));
            parameters.add(new Pair<>("extraId", routeIdTo));
            parameters.add(new Pair<>("todo", action));
            parameters.add(new Pair<>("action", "followAction"));
            Fuel.INSTANCE.post(Constants.actionsUrl, parameters).responseString(new Handler<String>() {
                @Override
                public void success(String s) {
                    runOnUiThread(() -> {
                        try {
                            requesting = false;
                            whiteFade.setVisibility(View.GONE);
                            JSONObject object = new JSONObject(s);
                            boolean noError = object.getBoolean("noError");
                            if(noError) {
                                optionText.setVisibility(View.GONE);
                                Toast.makeText(context, "Request " + action, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void failure(@NotNull FuelError fuelError) {
                    runOnUiThread(() -> {
                        whiteFade.setVisibility(View.GONE);
                        requesting = false;
                        Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }, 1000);
    }

    private void editRoute(JSONObject object) {
        Intent intent = new Intent(context, EditRouteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("activity", 1);
        bundle.putString("objectString", object.toString());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void startRouteSearch() {
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        RelativeLayout searchLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.long_box, null);
        LinearLayout loaderLayout = searchLayout.findViewById(R.id.loaderLayout);
        LinearLayout resultLayout = searchLayout.findViewById(R.id.resultLayout);
        blackFade.addView(searchLayout);
        blackFade.setVisibility(View.VISIBLE);
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", String.valueOf(getMyId()))
                    .addFormDataPart("routeId", routeId)
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.searchRoutesUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                if (response.isSuccessful()) {
                    loaderLayout.setVisibility(View.GONE);
                    String responseString = Objects.requireNonNull(response.body()).string();
                    JSONObject jsonObject = new JSONObject(responseString);
                    JSONArray data = jsonObject.getJSONArray("data");
                    JSONArray coordinates = jsonObject.getJSONArray("coordinates");
                    if(data.length() > 0){
                        for(int i = 0; i < data.length(); i++){
                            JSONObject dataObj = data.getJSONObject(i);
                            String id = dataObj.getString("id");
                            String name = dataObj.getString("name");
                            String photo = www + dataObj.getString("photo");
                            String phone = dataObj.getString("phone");
                            String locationFrom = dataObj.getString("locationFrom");
                            String locationTo = dataObj.getString("locationTo");
                            String routeDate = dataObj.getString("routeDate");
                            boolean freeRide = dataObj.getBoolean("freeRide");
                            double[][] doubles = functions.jsonToDoubles(coordinates);
                            double fromLat = doubles[0][0],
                                    fromLng = doubles[0][1],
                                    latFrom = dataObj.getDouble("latitudeFrom"),
                                    lngFrom = dataObj.getDouble("longitudeFrom"),
                                    latTo = dataObj.getDouble("latitudeTo"),
                                    lngTo = dataObj.getDouble("longitudeTo");
                            double distance = functions.getDistance(fromLat, fromLng, latFrom, lngFrom, null);
                            double dis = functions.getDistance(latFrom, lngFrom, latTo, lngTo, null);
                            String cost = getTripCost(dis);
                            String disText = distance + "mi from you";
                            int[] dateIntArr = convertDateToArray(routeDate);
                            int d = dateIntArr[0], m = dateIntArr[1], y = dateIntArr[2];
                            String mon = monthsOnly[m], day = String.valueOf(d), year = String.valueOf(y);
                            if(d < 10)
                                day = "0" + d;
                            RelativeLayout routeBox = (RelativeLayout) getLayoutInflater().inflate(R.layout.route_box, null);
                            ImageView imgView = routeBox.findViewById(R.id.imgView);
                            TextView nameTextView = routeBox.findViewById(R.id.nameTextView);
                            TextView phoneTextView = routeBox.findViewById(R.id.phoneTextView);
                            TextView costTextView = routeBox.findViewById(R.id.costTextView);
                            TextView fromTextView = routeBox.findViewById(R.id.fromTextView);
                            TextView toTextView = routeBox.findViewById(R.id.toTextView);
                            TextView monthTextView = routeBox.findViewById(R.id.monthTextView);
                            TextView dayTextView = routeBox.findViewById(R.id.dayTextView);
                            TextView yearTextView = routeBox.findViewById(R.id.yearTextView);
                            TextView distanceTextView = routeBox.findViewById(R.id.distanceTextView);
                            TextView open = routeBox.findViewById(R.id.open);
                            if(freeRide){
                                cost = "Free";
                                costTextView.setBackgroundResource(R.color.green);
                            }
                            imageLoader.displayImage(photo, imgView);
                            nameTextView.setText(name);
                            phoneTextView.setText(formatPhoneNumber(phone));
                            costTextView.setText(cost);
                            fromTextView.setText(locationFrom);
                            toTextView.setText(locationTo);
                            monthTextView.setText(mon);
                            dayTextView.setText(day);
                            yearTextView.setText(year);
                            distanceTextView.setText(disText);
                            open.setOnClickListener((v) -> openRoute(routeId, id));
                            resultLayout.addView(routeBox);
                        }
                    } else {
                        TextView textView = (TextView) getLayoutInflater().inflate(R.layout.empty_text_view, null);
                        textView.setText("No match found");
                        resultLayout.addView(textView);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void showPassengers() {
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        RelativeLayout searchLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.long_box, null);
        LinearLayout loaderLayout = searchLayout.findViewById(R.id.loaderLayout);
        LinearLayout resultLayout = searchLayout.findViewById(R.id.resultLayout);
        blackFade.addView(searchLayout);
        blackFade.setVisibility(View.VISIBLE);
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("routeId", routeId)
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.showRoutersURL)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                if (response.isSuccessful()) {
                    loaderLayout.setVisibility(View.GONE);
                    String responseString = Objects.requireNonNull(response.body()).string();
                    JSONObject jsonObject = new JSONObject(responseString);
                    JSONArray data = jsonObject.getJSONArray("data");
                    JSONArray coordinates = jsonObject.getJSONArray("coordinates");
                    if(data.length() > 0){
                        for(int i = 0; i < data.length(); i++){
                            JSONObject dataObj = data.getJSONObject(i);
                            String id = dataObj.getString("id");
                            String name = dataObj.getString("name");
                            String photo = www + dataObj.getString("photo");
                            String phone = dataObj.getString("phone");
                            String status = dataObj.getString("status");
                            String locationFrom = dataObj.getString("locationFrom");
                            String locationTo = dataObj.getString("locationTo");
                            double[][] doubles = functions.jsonToDoubles(coordinates);
                            double fromLat = doubles[0][0],
                                    fromLng = doubles[0][1],
                                    latFrom = dataObj.getDouble("latitudeFrom"),
                                    lngFrom = dataObj.getDouble("longitudeFrom");
                            double distance = functions.getDistance(fromLat, fromLng, latFrom, lngFrom, null);
                            String disText = distance + "mi from you";
                            RelativeLayout routeBox = (RelativeLayout) getLayoutInflater().inflate(R.layout.route_box, null);
                            ImageView imgView = routeBox.findViewById(R.id.imgView);
                            TextView nameTextView = routeBox.findViewById(R.id.nameTextView);
                            TextView phoneTextView = routeBox.findViewById(R.id.phoneTextView);
                            TextView costTextView = routeBox.findViewById(R.id.costTextView);
                            TextView fromTextView = routeBox.findViewById(R.id.fromTextView);
                            TextView toTextView = routeBox.findViewById(R.id.toTextView);
                            LinearLayout orangeView = routeBox.findViewById(R.id.orangeView);
                            TextView distanceTextView = routeBox.findViewById(R.id.distanceTextView);
                            TextView open = routeBox.findViewById(R.id.open);
                            if(status.equals("accepted"))
                                costTextView.setBackgroundResource(R.color.green);
                            else {
                                costTextView.setTextColor(ContextCompat.getColor(context, R.color.black));
                                costTextView.setBackgroundResource(R.color.quantum_yellow);
                            }
                            ViewGroup.LayoutParams params = costTextView.getLayoutParams();
                            params.width = 120;
                            costTextView.setLayoutParams(params);
                            orangeView.setVisibility(View.INVISIBLE);
                            imageLoader.displayImage(photo, imgView);
                            nameTextView.setText(name);
                            phoneTextView.setText(formatPhoneNumber(phone));
                            fromTextView.setText(locationFrom);
                            toTextView.setText(locationTo);
                            costTextView.setText(status);
                            distanceTextView.setText(disText);
                            open.setOnClickListener(v -> {
                                Bundle bundle = new Bundle();
                                bundle.putString("routeId", id);
                                Intent intent = new Intent(context, OpenRouteActivity.class);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            });
                            resultLayout.addView(routeBox);
                        }
                    } else {
                        TextView textView = (TextView) getLayoutInflater().inflate(R.layout.empty_text_view, null);
                        textView.setText("No Passenger linked with this ride");
                        resultLayout.addView(textView);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    private void openRoute(String routeId, String id) {
        Intent intent = new Intent(context, RouteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("routeIdFrom", routeId);
        bundle.putString("routeIdTo", id);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void cancelRoute() {
        whiteFade.setVisibility(View.VISIBLE);
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", String.valueOf(getMyId()))
                    .addFormDataPart("action", "cancelRoute")
                    .addFormDataPart("id", routeId)
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.actionsUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                if (response.isSuccessful()) {
                    whiteFade.setVisibility(View.GONE);
                    String responseString = Objects.requireNonNull(response.body()).string();
                    JSONObject jsonObject = new JSONObject(responseString);
                    boolean noError = jsonObject.getBoolean("noError");
                    String dataStr = jsonObject.getString("dataStr");
                    if(noError) {
                        bottomSheet.setVisibility(View.GONE);
                        Toast.makeText(context, "Route Cancelled", Toast.LENGTH_LONG).show();
                    } else
                        Toast.makeText(context, dataStr, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1000);
    }

    private void hideMenuBox() {
        menuLayout.setVisibility(View.GONE);
        dummyLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if(requesting)
            return;
        if(menuLayout.getVisibility() == View.VISIBLE){
            hideMenuBox();
            return;
        }
        if(blackFade.getVisibility() == View.VISIBLE){
            blackFade.setVisibility(View.GONE);
            return;
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isPaused && hasBeenEdited) {
            hasBeenEdited = false;
            isPaused = false;
            finish();
            startActivity(getIntent());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }
}