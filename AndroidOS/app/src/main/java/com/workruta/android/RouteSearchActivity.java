package com.workruta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.workruta.android.Utils.Constants;
import com.workruta.android.Utils.Functions;
import com.workruta.android.Utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.workruta.android.Utils.Constants.country;
import static com.workruta.android.Utils.Constants.monthsOnly;
import static com.workruta.android.Utils.Constants.www;
import static com.workruta.android.Utils.Functions.formatPhoneNumber;
import static com.workruta.android.Utils.Functions.getTripCost;
import static com.workruta.android.Utils.Util.convertDateToArray;

public class RouteSearchActivity extends SharedCompatActivity {

    Context context;
    EditText fromEditText, toEditText, editText;
    RelativeLayout mainView, calendarLayout;
    ScrollView scrollView;
    LinearLayout routesLayout, blackFade, loadingLayout, hourLayout, minLayout, timeViewLayout;
    TextView createButton, dateButton, distanceTextView, headText, noText, timeSelectView, timeView, okayBtn;
    boolean requesting, locationFrom, toRecreate, searching;
    double[] fromLatLng = new double[2], toLatLng = new double[2];
    View poppedView;
    CalendarView calendarView;
    Functions functions;
    String dateStr, routeDate, selectedDate, rFrom, rTo;
    JSONObject requestingObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_search);
        context = this;
        searching = false;
        requesting = false;
        toRecreate = false;
        requestingObj = new JSONObject();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long currentDate = cal.getTimeInMillis();
        functions = new Functions();

        mainView = findViewById(R.id.mainView);
        headText = findViewById(R.id.headText);
        noText = findViewById(R.id.noText);
        blackFade = findViewById(R.id.blackFade);
        loadingLayout = findViewById(R.id.loadingLayout);
        scrollView = findViewById(R.id.scrollView);
        routesLayout = findViewById(R.id.routesLayout);
        calendarLayout = findViewById(R.id.calendarLayout);
        distanceTextView = findViewById(R.id.distanceTextView);
        dateButton = findViewById(R.id.dateButton);
        createButton = findViewById(R.id.createButton);
        calendarView = findViewById(R.id.calendarView);
        toEditText = findViewById(R.id.toEditText);
        fromEditText = findViewById(R.id.fromEditText);
        hourLayout = findViewById(R.id.hourLayout);
        minLayout = findViewById(R.id.minLayout);
        timeViewLayout = findViewById(R.id.timeViewLayout);
        okayBtn = findViewById(R.id.okayBtn);
        timeView = findViewById(R.id.timeView);
        timeSelectView = findViewById(R.id.timeSelectView);

        populateViewContent(hourLayout, 24);
        populateViewContent(minLayout, 60);
        calendarView.setMinDate(currentDate);
        calendarView.setMaxDate(currentDate + (60 * 60 * 24 * 7 * 1000));
        toEditText.setFocusable(false);
        fromEditText.setFocusable(false);
        headText.setOnClickListener((v) -> onBackPressed());
        toEditText.setOnClickListener((v) -> openAddressAutoComplete(toEditText, false));
        fromEditText.setOnClickListener((v) -> openAddressAutoComplete(fromEditText, true));
        dateButton.setOnClickListener((v) -> {
            if(requesting)
                return;
            poppedView = calendarLayout;
            calendarLayout.setVisibility(View.VISIBLE);
        });
        createButton.setOnClickListener((v) -> createNewRoute());
        blackFade.setOnClickListener((v) -> {
            return;
        });
        calendarLayout.setOnClickListener((v) -> {
            return;
        });
        timeViewLayout.setOnClickListener((v) -> {
            return;
        });
        timeView.setOnClickListener((v) -> timeViewLayout.setVisibility(View.VISIBLE));
        timeSelectView.setOnClickListener((v) -> {
            if(!(hour == null) && !(minute == null)) {
                String timeStr = hour + ":" + minute;
                timeView.setText(timeStr);
                timeViewLayout.setVisibility(View.INVISIBLE);
                return;
            }
            Toast.makeText(context, "Please select a valid time", Toast.LENGTH_LONG).show();
        });
        okayBtn.setOnClickListener((v) -> setRouteDateTime());
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            int rm = month + 1;
            String m = String.valueOf(rm), d = String.valueOf(dayOfMonth);
            if(rm < 10)
                m = "0" + m;
            if(dayOfMonth < 10)
                d = "0" + d;
            selectedDate = year + "-" + m + "-" + d;
            String mon = monthsOnly[month];
            dateStr = d + " " + mon + " " + year;
        });
        checkForPendingRoutes();
        getRoutes();

        setupUI(mainView);
    }

    private void setRouteDateTime() {
        if(dateStr == null || hour == null || minute == null) {
            Toast.makeText(context, "Please select date and time", Toast.LENGTH_LONG).show();
            return;
        }
        poppedView = null;
        calendarLayout.setVisibility(View.GONE);
        String s = dateStr + " (" + hour + ":" + minute + ")";
        dateButton.setText(s);
        routeDate = selectedDate + " " + hour + ":" + minute + ":00";
        if(toRecreate){
            toRecreate = false;
            toEditText.setText(rTo);
            fromEditText.setText(rFrom);
            createNewRoute();
        }
    }

    @SuppressLint("InflateParams")
    private void checkForPendingRoutes() {
        try {
            JSONObject jsonObject = sharedPrefMngr.getPendingRoutes(String.valueOf(getMyId()));
            if (jsonObject.length() > 0) {
                for (int i = 0; i < jsonObject.length(); i++) {
                    String key = Objects.requireNonNull(jsonObject.names()).getString(i);
                    JSONObject object = jsonObject.getJSONObject(key);
                    String dateStrText = object.getString("dateStr"),
                            locationFromText = object.getString("locationFrom"),
                            locationToText = object.getString("locationTo"),
                            type = object.getString("type");
                    String[] dateStrTexts = dateStrText.split(" ");
                    String day = dateStrTexts[0], mon = dateStrTexts[1], year = dateStrTexts[2];
                    RelativeLayout routeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.route_view, null);
                    TextView monthTextView = routeLayout.findViewById(R.id.monthTextView);
                    TextView dayTextView = routeLayout.findViewById(R.id.dayTextView);
                    TextView yearTextView = routeLayout.findViewById(R.id.yearTextView);
                    TextView fromTextView = routeLayout.findViewById(R.id.fromTextView);
                    TextView toTextView = routeLayout.findViewById(R.id.toTextView);
                    TextView typeTextView = routeLayout.findViewById(R.id.typeTextView);
                    LinearLayout loaderLayout = routeLayout.findViewById(R.id.loaderLayout);
                    ImageView loadingImageView = routeLayout.findViewById(R.id.loadingImageView);
                    TextView errorTextView = routeLayout.findViewById(R.id.errorTextView);
                    monthTextView.setText(mon);
                    dayTextView.setText(day);
                    yearTextView.setText(year);
                    fromTextView.setText(locationFromText);
                    toTextView.setText(locationToText);
                    typeTextView.setText(type);
                    loadingImageView.setVisibility(View.GONE);
                    errorTextView.setVisibility(View.VISIBLE);
                    loaderLayout.setVisibility(View.VISIBLE);
                    routesLayout.addView(routeLayout, 0);
                    loaderLayout.setOnClickListener((v) -> showFailedRouteActions(routeLayout, key, object));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("InflateParams")
    private void createNewRoute() {
        if(requesting)
            return;
        noText.setVisibility(View.GONE);
        String locationFromText = fromEditText.getText().toString(),
                locationToText = toEditText.getText().toString();
        if(StringUtils.isEmpty(locationFromText) || StringUtils.isEmpty(locationToText) || StringUtils.isEmpty(routeDate)){
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_LONG).show();
            return;
        }
        requesting = true;
        String[] dateStrs = dateStr.split(" ");
        String day = dateStrs[0], mon = dateStrs[1], year = dateStrs[2];
        RelativeLayout routeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.route_view, null);
        TextView monthTextView = routeLayout.findViewById(R.id.monthTextView);
        TextView dayTextView = routeLayout.findViewById(R.id.dayTextView);
        TextView yearTextView = routeLayout.findViewById(R.id.yearTextView);
        TextView fromTextView = routeLayout.findViewById(R.id.fromTextView);
        TextView toTextView = routeLayout.findViewById(R.id.toTextView);
        TextView typeTextView = routeLayout.findViewById(R.id.typeTextView);
        LinearLayout loaderLayout = routeLayout.findViewById(R.id.loaderLayout);
        ImageView loadingImageView = routeLayout.findViewById(R.id.loadingImageView);
        TextView errorTextView = routeLayout.findViewById(R.id.errorTextView);
        View statusBar = routeLayout.findViewById(R.id.statusBar);
        monthTextView.setText(mon);
        dayTextView.setText(day);
        yearTextView.setText(year);
        fromTextView.setText(locationFromText);
        toTextView.setText(locationToText);
        typeTextView.setText("U");
        loaderLayout.setVisibility(View.VISIBLE);
        routesLayout.addView(routeLayout, 0);
        scrollView.scrollTo(0, 0);
        new android.os.Handler().postDelayed(() -> {
            try {
                double fromLat = fromLatLng[0], fromLng = fromLatLng[1], toLat = toLatLng[0], toLng = toLatLng[1];
                JSONObject object = new JSONObject();
                object.put("action", "createRoute");
                object.put("user", String.valueOf(getMyId()));
                object.put("type", "U");
                object.put("passNum", "0");
                object.put("freeRide", "true");
                object.put("locationFrom", locationFromText);
                object.put("locationTo", locationToText);
                object.put("latitudeFrom", String.valueOf(fromLat));
                object.put("longitudeFrom", String.valueOf(fromLng));
                object.put("latitudeTo", String.valueOf(toLat));
                object.put("longitudeTo", String.valueOf(toLng));
                object.put("routeDate", routeDate);
                object.put("dateStr", dateStr);
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                for (int i = 0; i < object.length(); i++) {
                    String rKey = Objects.requireNonNull(object.names()).getString(i);
                    String value = object.getString(rKey);
                    builder.addFormDataPart(rKey, value);
                }
                RequestBody requestBody = builder.build();
                Request request = new Request.Builder()
                        .url(Constants.actionsUrl)
                        .post(requestBody)
                        .build();

                OkHttpClient okHttpClient = new OkHttpClient();
                Call call = okHttpClient.newCall(request);
                try (Response response = call.execute()) {
                    dateStr = null;
                    routeDate = null;
                    toEditText.setText("");
                    fromEditText.setText("");
                    distanceTextView.setText("");
                    distanceTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    dateButton.setText(getResources().getString(R.string.select_date));
                    requesting = false;
                    if (response.isSuccessful()) {
                        String responseString = Objects.requireNonNull(response.body()).string();
                        JSONObject jsonObject = new JSONObject(responseString);
                        boolean noError = jsonObject.getBoolean("noError");
                        if (noError) {
                            JSONObject dataObj = jsonObject.getJSONObject("dataStr");
                            String id = dataObj.getString("id");
                            statusBar.setBackgroundResource(R.drawable.radius_yellow);
                            routeLayout.setOnClickListener((v) -> startRouteSearch(id));
                            loaderLayout.setVisibility(View.GONE);
                            startRouteSearch(id);
                        } else {
                            String key = String.valueOf(System.currentTimeMillis());
                            sharedPrefMngr.addPendingRoute(String.valueOf(getMyId()), key, object);
                            loadingImageView.setVisibility(View.GONE);
                            errorTextView.setVisibility(View.VISIBLE);
                            loaderLayout.setOnClickListener((v) -> retryCreatingRoute(routeLayout, request, key));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void startRouteSearch(String routeId) {
        searching = true;
        poppedView = blackFade;
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
                searching = false;
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
                            TextView costTextView = routeBox.findViewById(R.id.costTextView);
                            TextView phoneTextView = routeBox.findViewById(R.id.phoneTextView);
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
                            costTextView.setText(cost);
                            phoneTextView.setText(formatPhoneNumber(phone));
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
    private void getRoutes() {
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", String.valueOf(getMyId()))
                    .addFormDataPart("maxId", "0")
                    .addFormDataPart("excluded", "pending")
                    .addFormDataPart("type", "U")
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.getRoutesUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                if (response.isSuccessful()) {
                    loadingLayout.setVisibility(View.GONE);
                    String responseString = Objects.requireNonNull(response.body()).string();
                    JSONObject object = new JSONObject(responseString);
                    JSONArray data = object.getJSONArray("data");
                    if(data.length() > 0){
                        for(int i = 0; i < data.length(); i++){
                            JSONObject dataObj = data.getJSONObject(i);
                            String locationFrom = dataObj.getString("locationFrom");
                            String locationTo = dataObj.getString("locationTo");
                            String type = dataObj.getString("type");
                            double latitudeFrom = dataObj.getDouble("latitudeFrom");
                            double longitudeFrom = dataObj.getDouble("longitudeFrom");
                            double latitudeTo = dataObj.getDouble("latitudeTo");
                            double longitudeTo = dataObj.getDouble("longitudeTo");
                            String routeDate = dataObj.getString("routeDate");
                            String status = dataObj.getString("status");
                            RelativeLayout routeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.route_view, null);
                            TextView monthTextView = routeLayout.findViewById(R.id.monthTextView);
                            TextView dayTextView = routeLayout.findViewById(R.id.dayTextView);
                            TextView yearTextView = routeLayout.findViewById(R.id.yearTextView);
                            TextView fromTextView = routeLayout.findViewById(R.id.fromTextView);
                            TextView toTextView = routeLayout.findViewById(R.id.toTextView);
                            TextView typeTextView = routeLayout.findViewById(R.id.typeTextView);
                            View statusBar = routeLayout.findViewById(R.id.statusBar);
                            int[] dateIntArr = convertDateToArray(routeDate);
                            int d = dateIntArr[0], m = dateIntArr[1], y = dateIntArr[2], statusBarColor;
                            String mon = monthsOnly[m], day = String.valueOf(d), year = String.valueOf(y);
                            if(d < 10)
                                day = "0" + d;
                            if(status.equals("pending"))
                                statusBarColor = R.drawable.radius_yellow;
                            else if(status.equals("cancel"))
                                statusBarColor = R.drawable.radius_red;
                            else
                                statusBarColor = R.drawable.radius_green;
                            monthTextView.setText(mon);
                            dayTextView.setText(day);
                            yearTextView.setText(year);
                            fromTextView.setText(locationFrom);
                            toTextView.setText(locationTo);
                            typeTextView.setText(type);
                            statusBar.setBackgroundResource(statusBarColor);
                            routeLayout.setOnClickListener((v) -> {
                                poppedView = blackFade;
                                if(blackFade.getChildCount() > 0)
                                    blackFade.removeAllViews();
                                String negTxt = getResources().getString(R.string.cancel),
                                        posTxt = getResources().getString(R.string.recreate),
                                        alertTxt = getResources().getString(R.string.recreate_route);
                                LinearLayout alertLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.alert_layout, null);
                                TextView textView = alertLayout.findViewById(R.id.textView);
                                Button negativeBtn = alertLayout.findViewById(R.id.negative);
                                Button positiveBtn = alertLayout.findViewById(R.id.positive);
                                textView.setText(alertTxt);
                                negativeBtn.setText(negTxt);
                                positiveBtn.setText(posTxt);
                                negativeBtn.setOnClickListener((v1) -> {
                                    blackFade.setVisibility(View.GONE);
                                    poppedView = null;
                                });
                                positiveBtn.setOnClickListener((v2) -> {
                                    toRecreate = true;
                                    rFrom = locationFrom;
                                    rTo = locationTo;
                                    fromLatLng[0] = latitudeFrom;
                                    fromLatLng[1] = longitudeFrom;
                                    toLatLng[0] = latitudeTo;
                                    toLatLng[1] = longitudeTo;
                                    blackFade.setVisibility(View.GONE);
                                    calendarLayout.setVisibility(View.VISIBLE);
                                    poppedView = calendarLayout;
                                });
                                blackFade.addView(alertLayout);
                                blackFade.setVisibility(View.VISIBLE);
                            });
                            routesLayout.addView(routeLayout);
                        }
                    } else
                        noText.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    @SuppressLint("InflateParams")
    private void showFailedRouteActions(RelativeLayout routeLayout, String key, JSONObject object) {
        try {
            if(requestingObj.has(key)){
                boolean r = requestingObj.getBoolean(key);
                if(r)
                    return;
            }
            poppedView = blackFade;
            if (blackFade.getChildCount() > 0)
                blackFade.removeAllViews();
            String negTxt = getResources().getString(R.string.deleteRoute),
                    posTxt = getResources().getString(R.string.retry),
                    alertTxt = getResources().getString(R.string.route_action_text);
            LinearLayout alertLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.alert_layout, null);
            TextView textView = alertLayout.findViewById(R.id.textView);
            Button negativeBtn = alertLayout.findViewById(R.id.negative);
            Button positiveBtn = alertLayout.findViewById(R.id.positive);
            textView.setText(Html.fromHtml(alertTxt));
            negativeBtn.setText(negTxt);
            positiveBtn.setText(posTxt);
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            for (int i = 0; i < object.length(); i++) {
                String rKey = Objects.requireNonNull(object.names()).getString(i);
                String value = object.getString(rKey);
                builder.addFormDataPart(rKey, value);
            }
            RequestBody requestBody = builder.build();
            Request request = new Request.Builder()
                    .url(Constants.actionsUrl)
                    .post(requestBody)
                    .build();
            negativeBtn.setOnClickListener((v) -> deleteRoute(routeLayout, key));
            positiveBtn.setOnClickListener((v) -> retryCreatingRoute(routeLayout, request, key));
            blackFade.addView(alertLayout);
            blackFade.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void retryCreatingRoute(RelativeLayout routeLayout, Request request, String key) {
        try {
            requestingObj.put(key, true);
            blackFade.setVisibility(View.GONE);
            poppedView = null;
            LinearLayout loaderLayout = routeLayout.findViewById(R.id.loaderLayout);
            ImageView loadingImageView = routeLayout.findViewById(R.id.loadingImageView);
            TextView errorTextView = routeLayout.findViewById(R.id.errorTextView);
            View statusBar = routeLayout.findViewById(R.id.statusBar);
            errorTextView.setVisibility(View.GONE);
            loadingImageView.setVisibility(View.VISIBLE);
            new android.os.Handler().postDelayed(() -> {
                OkHttpClient okHttpClient = new OkHttpClient();
                Call call = okHttpClient.newCall(request);
                try(Response response = call.execute()) {
                    requesting = false;
                    if (response.isSuccessful()) {
                        String responseString = Objects.requireNonNull(response.body()).string();
                        JSONObject object = new JSONObject(responseString);
                        boolean noError = object.getBoolean("noError");
                        requestingObj.remove(key);
                        if(noError) {
                            loaderLayout.setVisibility(View.GONE);
                            sharedPrefMngr.clearPendingRoutes(String.valueOf(getMyId()), key);
                            statusBar.setBackgroundResource(R.drawable.radius_yellow);
                        } else {
                            loadingImageView.setVisibility(View.GONE);
                            errorTextView.setVisibility(View.VISIBLE);
                            loaderLayout.setOnClickListener((v) -> retryCreatingRoute(routeLayout, request, key));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 2000);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void deleteRoute(RelativeLayout routeLayout, String key){
        blackFade.setVisibility(View.GONE);
        routesLayout.removeView(routeLayout);
        if(routesLayout.getChildCount() == 0)
            noText.setVisibility(View.VISIBLE);
        poppedView = null;
        sharedPrefMngr.clearPendingRoutes(String.valueOf(getMyId()), key);
    }

    private void openAddressAutoComplete(EditText editText, boolean locationFrom) {
        if(requesting)
            return;
        this.editText = editText;
        this.locationFrom = locationFrom;
        List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList)
                .setCountry(country).build(context);
        startActivityIntent.launch(intent);
    }

    private final ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getData() != null && result.getResultCode() == RESULT_OK) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    editText.setText(place.getAddress());
                    if(locationFrom){
                        fromLatLng[0] = Objects.requireNonNull(place.getLatLng()).latitude;
                        fromLatLng[1] = place.getLatLng().longitude;
                    } else {
                        toLatLng[0] = Objects.requireNonNull(place.getLatLng()).latitude;
                        toLatLng[1] = place.getLatLng().longitude;
                    }
                    if(!StringUtils.isEmpty(fromEditText.getText().toString()) && !StringUtils.isEmpty(toEditText.getText().toString())) {
                        double distance = functions.getDistance(fromLatLng[0], fromLatLng[1], toLatLng[0], toLatLng[1], null);
                        String distanceStr = distance + "mi";
                        distanceTextView.setText(distanceStr);
                        distanceTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_car, 0, 0, 0);
                    }
                }
            });

    @SuppressLint("InflateParams")
    public void onBackPressed(){
        if(requesting || searching)
            return;
        if(timeViewLayout.getVisibility() == View.VISIBLE){
            timeViewLayout.setVisibility(View.GONE);
            return;
        }
        if(poppedView != null){
            toRecreate = false;
            poppedView.setVisibility(View.GONE);
            poppedView = null;
            return;
        }
        if((!StringUtils.isEmpty(fromEditText.getText().toString()) || !StringUtils.isEmpty(toEditText.getText().toString())) && !requesting){
            poppedView = blackFade;
            if(blackFade.getChildCount() > 0)
                blackFade.removeAllViews();
            String negTxt = getResources().getString(R.string.cancel),
                    posTxt = getResources().getString(R.string.discard),
                    alertTxt = getResources().getString(R.string.discard_text);
            LinearLayout alertLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.alert_layout, null);
            TextView textView = alertLayout.findViewById(R.id.textView);
            Button negativeBtn = alertLayout.findViewById(R.id.negative);
            Button positiveBtn = alertLayout.findViewById(R.id.positive);
            textView.setText(alertTxt);
            negativeBtn.setText(negTxt);
            positiveBtn.setText(posTxt);
            negativeBtn.setOnClickListener((v) -> {
                blackFade.setVisibility(View.GONE);
                poppedView = null;
            });
            positiveBtn.setOnClickListener((v) -> finish());
            blackFade.addView(alertLayout);
            blackFade.setVisibility(View.VISIBLE);
            return;
        }
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
                fromEditText.clearFocus();
                toEditText.clearFocus();
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