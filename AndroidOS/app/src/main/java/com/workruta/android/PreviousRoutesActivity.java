package com.workruta.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
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
import java.util.Calendar;
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

public class PreviousRoutesActivity extends SharedCompatActivity {

    @SuppressLint("StaticFieldLeak")
    static Context context;
    ScrollView scrollView;
    @SuppressLint("StaticFieldLeak")
    static LinearLayout routesLayout;
    LinearLayout blackFade, loadingLayout;
    RelativeLayout loadingView, setLayout;
    TextView headText, noText;
    String maxId;
    static int myId;
    boolean allLoaded, loadingRoutes, firstLoaded;
    DisplayMetrics displayMetrics;
    int width, height;
    int[] menuIcons;
    String[] menuTexts;
    JSONObject requestingObj;
    static JSONObject dataObject;

    @Override
    @SuppressLint("InflateParams")
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_routes);
        context = this;
        allLoaded = false;
        firstLoaded = false;
        loadingRoutes = false;
        maxId = "0";
        myId = getMyId();
        dataObject = new JSONObject();
        requestingObj = new JSONObject();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long currentDate = cal.getTimeInMillis();

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;

        scrollView = findViewById(R.id.scrollView);
        routesLayout = findViewById(R.id.routesLayout);
        loadingLayout = findViewById(R.id.loadingLayout);
        blackFade = findViewById(R.id.blackFade);
        headText = findViewById(R.id.headText);
        noText = findViewById(R.id.noText);

        headText.setOnClickListener((v) -> finish());
        blackFade.setOnClickListener((v) -> {
            return;
        });
        scrollView.setSmoothScrollingEnabled(true);
        scrollView.setOnScrollChangeListener((scrllVw, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            int scrollH = routesLayout.getHeight() - scrllVw.getHeight() - 50;
            if(scrollY > scrollH && !loadingRoutes && !allLoaded){
                loadingView = (RelativeLayout) getLayoutInflater().inflate(R.layout.loading_view, null, false);
                routesLayout.addView(loadingView);
                ViewTreeObserver viewTreeObserver = loadingView.getViewTreeObserver();
                viewTreeObserver.addOnGlobalLayoutListener(this::getRoutes);
            }
        });

        menuIcons = new int[]{
                R.drawable.ic_search,
                R.drawable.ic_carm,
                R.drawable.ic_scissor,
                R.drawable.ic_edit,
                R.drawable.ic_cancel,
                R.drawable.ic_search_dis,
                R.drawable.ic_carm_dis,
                R.drawable.ic_scissor_dis,
                R.drawable.ic_edit_dis,
                R.drawable.ic_cancel_dis
        };
        menuTexts = new String[]{
                "Search for Available Routes",
                "View Merged Ride",
                "Exclude from Merged Ride",
                "Edit Route",
                "Cancel Route"
        };

        getRoutes();

    }

    @SuppressLint("InflateParams")
    private void getRoutes() {
        loadingRoutes = true;
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", String.valueOf(getMyId()))
                    .addFormDataPart("maxId", maxId)
                    .addFormDataPart("excluded", "cancel,success")
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.getRoutesUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                if (response.isSuccessful()) {
                    loadingRoutes = false;
                    loadingLayout.setVisibility(View.GONE);
                    if(!(loadingView == null)){
                        routesLayout.removeView(loadingView);
                        loadingView = null;
                    }
                    String responseString = Objects.requireNonNull(response.body()).string();
                    JSONObject object = new JSONObject(responseString);
                    maxId = object.getString("maxId");
                    allLoaded = object.getBoolean("allLoaded");
                    JSONArray data = object.getJSONArray("data");
                    if(data.length() > 0){
                        for(int i = 0; i < data.length(); i++){
                            JSONObject dataObj = data.getJSONObject(i);
                            String id = dataObj.getString("id");
                            String locationFrom = dataObj.getString("locationFrom");
                            String locationTo = dataObj.getString("locationTo");
                            String routeDate = dataObj.getString("routeDate");
                            String type = dataObj.getString("type");
                            boolean notStarted = dataObj.getBoolean("notStarted");
                            dataObject.put(id, dataObj);
                            RelativeLayout routeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.route_view, null);
                            TextView monthTextView = routeLayout.findViewById(R.id.monthTextView);
                            TextView dayTextView = routeLayout.findViewById(R.id.dayTextView);
                            TextView yearTextView = routeLayout.findViewById(R.id.yearTextView);
                            TextView fromTextView = routeLayout.findViewById(R.id.fromTextView);
                            TextView toTextView = routeLayout.findViewById(R.id.toTextView);
                            TextView typeTextView = routeLayout.findViewById(R.id.typeTextView);
                            View statusBar = routeLayout.findViewById(R.id.statusBar);
                            routeLayout.setTag(id);
                            int[] dateIntArr = convertDateToArray(routeDate);
                            int d = dateIntArr[0], m = dateIntArr[1], y = dateIntArr[2];
                            String mon = monthsOnly[m], day = String.valueOf(d), year = String.valueOf(y);
                            if(d < 10)
                                day = "0" + d;
                            monthTextView.setText(mon);
                            dayTextView.setText(day);
                            yearTextView.setText(year);
                            fromTextView.setText(locationFrom);
                            toTextView.setText(locationTo);
                            typeTextView.setText(type);
                            statusBar.setBackgroundResource(R.drawable.radius_yellow);
                            routeLayout.setOnLongClickListener((v) -> {
                                try {
                                    setLayout = routeLayout;
                                    displayMenuOptions(dataObject.getJSONObject(id), notStarted);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                return false;
                            });
                            routesLayout.addView(routeLayout);
                        }
                    } else if(!firstLoaded)
                        noText.setVisibility(View.VISIBLE);
                    firstLoaded = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1000);
    }

    @SuppressLint("InflateParams")
    private void displayMenuOptions(JSONObject dataObj, boolean notStarted) throws JSONException {
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        String type = dataObj.getString("type");
        boolean isActive = dataObj.getBoolean("isActive");
        RelativeLayout optionBox = (RelativeLayout) getLayoutInflater().inflate(R.layout.option_box, null);
        LinearLayout box = optionBox.findViewById(R.id.box);
        int menuLength = notStarted ? menuTexts.length : menuTexts.length - 2;
        for(int i = 0; i < menuLength; i++){
            boolean clickable = true;
            String menuText = menuTexts[i];
            int menuIcon = menuIcons[i], color = ContextCompat.getColor(context, R.color.mainColor);
            LinearLayout optionText = (LinearLayout) getLayoutInflater().inflate(R.layout.option_text, null);
            TextView textView = optionText.findViewById(R.id.textView);
            if((type.equals("P") && i < 3) || ((isActive && i == 0) || (!isActive && (i == 1 || i == 2)))) {
                clickable = false;
                menuIcon = menuIcons[i + 5];
                color = ContextCompat.getColor(context, R.color.asher);
            }
            textView.setText(menuText);
            textView.setTextColor(color);
            textView.setCompoundDrawablesWithIntrinsicBounds(menuIcon, 0, 0, 0);
            boolean finalClickable = clickable;
            int finalI = i;
            textView.setOnClickListener((v) -> {
                if(!finalClickable)
                    return;
                try {
                    executeMenuOptions(dataObj, finalI);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            box.addView(optionText);
        }
        blackFade.addView(optionBox);
        blackFade.setVisibility(View.VISIBLE);
    }

    private void executeMenuOptions(JSONObject object, int index) throws JSONException {
        blackFade.setVisibility(View.GONE);
        if(index == 0){
            try {
                startRouteSearch(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }
        if(index == 1){
            String routeIdFrom = object.getString("id");
            String routeIdTo = object.getString("routeId");
            Intent intent = new Intent(context, RouteActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("routeIdTo", routeIdTo);
            bundle.putString("routeIdFrom", routeIdFrom);
            intent.putExtras(bundle);
            startActivity(intent);
            return;
        }
        if(index == 3){
            String objectString = object.toString();
            Intent intent = new Intent(context, EditRouteActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("activity", 0);
            bundle.putString("objectString", objectString);
            intent.putExtras(bundle);
            startActivity(intent);
            return;
        }
        displayOptionAction(object, index);
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void startRouteSearch(JSONObject object) throws JSONException {
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        String routeId = object.getString("id");
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

    private void openRoute(String routeId, String id) {
        Intent intent = new Intent(context, RouteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("routeIdFrom", routeId);
        bundle.putString("routeIdTo", id);
        intent.putExtras(bundle);
        startActivity(intent);
        blackFade.setVisibility(View.GONE);
    }

    @SuppressLint("InflateParams")
    private void displayOptionAction(JSONObject object, int index) {
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        String negTxt = getResources().getString(R.string.no),
                posTxt = getResources().getString(R.string.yes),
                alertTxt = getResources().getString(R.string.cancel_route);
        if(index == 2){
            negTxt = getResources().getString(R.string.cancel);
            posTxt = getResources().getString(R.string.exclude);
            alertTxt = getResources().getString(R.string.exclude_from_route);
        }
        LinearLayout alertLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.alert_layout, null);
        TextView textView = alertLayout.findViewById(R.id.textView);
        Button negativeBtn = alertLayout.findViewById(R.id.negative);
        Button positiveBtn = alertLayout.findViewById(R.id.positive);
        textView.setText(alertTxt);
        negativeBtn.setText(negTxt);
        positiveBtn.setText(posTxt);
        negativeBtn.setOnClickListener((v) -> blackFade.setVisibility(View.GONE));
        positiveBtn.setOnClickListener((v) -> {
            try {
                blackFade.setVisibility(View.GONE);
                if(index == 2)
                    excludeFromRide(object);
                else
                    cancelRoute(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        blackFade.addView(alertLayout);
        blackFade.setVisibility(View.VISIBLE);
    }

    private void excludeFromRide(JSONObject object) throws JSONException {
        String id = object.getString("id");
        String routerId = object.getString("routerId");
        String routeId = object.getString("routeId");
        String userTo = object.getString("user");
        String pathKey = object.getString("pathKey");
        RelativeLayout relativeLayout = setLayout;
        LinearLayout loaderLayout = relativeLayout.findViewById(R.id.loaderLayout);
        loaderLayout.setVisibility(View.VISIBLE);
        new android.os.Handler().postDelayed(() -> {
            List<Pair<String, String>> parameters = new ArrayList<>();
            parameters.add(new Pair<>("routerId", routerId));
            parameters.add(new Pair<>("routeId", routeId));
            parameters.add(new Pair<>("userTo", userTo));
            parameters.add(new Pair<>("pathKey", pathKey));
            parameters.add(new Pair<>("action", "exclude"));
            parameters.add(new Pair<>("user", String.valueOf(getMyId())));
            Fuel.INSTANCE.post(Constants.actionsUrl, parameters).responseString(new Handler<String>() {
                @Override
                public void success(String s) {
                    runOnUiThread(() -> {
                        try {
                            loaderLayout.setVisibility(View.GONE);
                            JSONObject object = new JSONObject(s);
                            boolean noError = object.getBoolean("noError");
                            if(noError) {
                                setRouteActive(id, null, null, null, null, false);
                                showMessageError("You have been excluded as a passenger to this ride");
                            } else {
                                String errorMsg = object.getString("dataStr");
                                showMessageError(errorMsg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void failure(@NotNull FuelError fuelError) {
                    runOnUiThread(() -> {
                        loaderLayout.setVisibility(View.GONE);
                    });
                }
            });
        }, 2000);
    }

    @SuppressLint("InflateParams")
    private void showMessageError(String errorMsg) {
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        LinearLayout alertLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.alert_layout, null);
        TextView textView = alertLayout.findViewById(R.id.textView);
        Button negativeBtn = alertLayout.findViewById(R.id.negative);
        Button positiveBtn = alertLayout.findViewById(R.id.positive);
        textView.setText(errorMsg);
        positiveBtn.setText(R.string.ok);
        negativeBtn.setVisibility(View.GONE);
        positiveBtn.setOnClickListener((v) -> blackFade.setVisibility(View.INVISIBLE));
        blackFade.addView(alertLayout);
        blackFade.setVisibility(View.VISIBLE);
    }

    public static void saveEdit(JSONObject object) throws JSONException {
        String id = object.getString("id");
        JSONObject jsonObject = dataObject.getJSONObject(id);
        for (int i = 0; i < object.length(); i++){
            String key = Objects.requireNonNull(object.names()).getString(i);
            String text = object.getString(key);
            jsonObject.put(key, text);
        }
        dataObject.put(id, jsonObject);
        String locationFrom = object.getString("locationFrom");
        String locationTo = object.getString("locationTo");
        String dateStr = object.getString("dateStr");
        String[] dateStrs = dateStr.split(" ");
        String day = dateStrs[0], mon = dateStrs[1], year = dateStrs[2];
        RelativeLayout routeLayout = routesLayout.findViewWithTag(id);
        TextView monthTextView = routeLayout.findViewById(R.id.monthTextView);
        TextView dayTextView = routeLayout.findViewById(R.id.dayTextView);
        TextView yearTextView = routeLayout.findViewById(R.id.yearTextView);
        TextView fromTextView = routeLayout.findViewById(R.id.fromTextView);
        TextView toTextView = routeLayout.findViewById(R.id.toTextView);
        monthTextView.setText(mon);
        dayTextView.setText(day);
        yearTextView.setText(year);
        fromTextView.setText(locationFrom);
        toTextView.setText(locationTo);
    }

    private void cancelRoute(JSONObject object) throws JSONException {
        String id = object.getString("id");
        RelativeLayout relativeLayout = setLayout;
        LinearLayout loaderLayout = relativeLayout.findViewById(R.id.loaderLayout);
        View statusBar = relativeLayout.findViewById(R.id.statusBar);
        loaderLayout.setVisibility(View.VISIBLE);
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", String.valueOf(getMyId()))
                    .addFormDataPart("action", "cancelRoute")
                    .addFormDataPart("id", id)
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.actionsUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                if (response.isSuccessful()) {
                    loaderLayout.setVisibility(View.GONE);
                    String responseString = Objects.requireNonNull(response.body()).string();
                    JSONObject jsonObject = new JSONObject(responseString);
                    boolean noError = jsonObject.getBoolean("noError");
                    String dataStr = jsonObject.getString("dataStr");
                    if(noError) {
                        relativeLayout.setVisibility(View.GONE);
                        Toast.makeText(context, "Route Cancelled", Toast.LENGTH_LONG).show();
                    } else
                        Toast.makeText(context, dataStr, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1000);
    }

    public static void setRouteActive(String routeId, String routerId, String routeIdTo, String userTo, String pathKey, boolean isActive){
        if(dataObject != null) {
            try {
                JSONObject object = dataObject.getJSONObject(routeId);
                object.put("isActive", isActive);
                object.put("routeId", routeIdTo);
                object.put("routerId", routerId);
                object.put("userTo", userTo);
                object.put("pathKey", pathKey);
                dataObject.put(routeId, object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(blackFade.getVisibility() == View.VISIBLE){
            blackFade.setVisibility(View.GONE);
            return;
        }
        finish();
    }
}