package com.workruta.android;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.workruta.android.Utils.Constants;
import com.workruta.android.Utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.workruta.android.Utils.Constants.monthsOnly;
import static com.workruta.android.Utils.Util.convertDateToArray;

public class HistoryActivity extends SharedCompatActivity {

    Context context;
    ScrollView scrollView;
    EditText passNumET;
    LinearLayout routesLayout, blackFade, loadingLayout, hourLayout, minLayout, timeViewLayout, hault;
    RelativeLayout mainView, loadingView, setLayout, calendarLayout;
    TextView headText, noText, timeSelectView, timeView, okayBtn, okayBtnW;
    CalendarView calendarView;
    CheckBox freeRideBox;
    String maxId, selectedDate;
    boolean allLoaded, loadingRoutes, firstLoaded;
    DisplayMetrics displayMetrics;
    int width, height;
    int[] menuIcons;
    String[] menuTexts;
    JSONObject routeObject;
    JSONObject requestingObj;

    @SuppressLint("InflateParams")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        context = this;
        allLoaded = false;
        firstLoaded = false;
        loadingRoutes = false;
        maxId = "0";
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
        freeRideBox = findViewById(R.id.freeRideBox);
        loadingLayout = findViewById(R.id.loadingLayout);
        blackFade = findViewById(R.id.blackFade);
        headText = findViewById(R.id.headText);
        noText = findViewById(R.id.noText);
        okayBtnW = findViewById(R.id.okayBtnW);
        hault = findViewById(R.id.hault);
        passNumET = findViewById(R.id.passNum);
        calendarLayout = findViewById(R.id.calendarLayout);
        calendarView = findViewById(R.id.calendarView);
        hourLayout = findViewById(R.id.hourLayout);
        minLayout = findViewById(R.id.minLayout);
        timeViewLayout = findViewById(R.id.timeViewLayout);
        okayBtn = findViewById(R.id.okayBtn);
        mainView = findViewById(R.id.mainView);
        timeView = findViewById(R.id.timeView);
        timeSelectView = findViewById(R.id.timeSelectView);

        populateViewContent(hourLayout, 24);
        populateViewContent(minLayout, 60);
        calendarView.setMinDate(currentDate);
        calendarView.setMaxDate(currentDate + (60 * 60 * 24 * 7 * 1000));
        headText.setOnClickListener((v) -> finish());
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
        okayBtn.setOnClickListener((v) -> setRouteDateTime(true));
        okayBtnW.setOnClickListener((v) -> setRouteDateTime(false));
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            try {
                int rm = month + 1;
                String m = String.valueOf(rm), d = String.valueOf(dayOfMonth);
                if (rm < 10)
                    m = "0" + m;
                if (dayOfMonth < 10)
                    d = "0" + d;
                selectedDate = year + "-" + m + "-" + d;
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                R.drawable.ic_add,
                R.drawable.ic_edit,
                R.drawable.ic_cancel,
                R.drawable.ic_add_dis,
                R.drawable.ic_edit_dis,
                R.drawable.ic_cancel_dis
        };
        menuTexts = new String[]{
                "Recreate Route",
                "Edit Route",
                "Cancel Route"
        };

        setupUI(mainView);
        getRoutes();

    }

    private void setRouteDateTime(boolean isProv) {
        try {
            String passNum = passNumET.getText().toString();
            boolean freeRide = freeRideBox.isChecked();
            if (selectedDate == null || hour == null || minute == null) {
                Toast.makeText(context, "Please select date and time", Toast.LENGTH_LONG).show();
                return;
            }
            if (isProv && StringUtils.isEmpty(passNum)) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_LONG).show();
                return;
            }
            calendarLayout.setVisibility(View.GONE);
            String routeDate = selectedDate + " " + hour + ":" + minute + ":00";
            recreateRoute(routeDate, passNum, freeRide);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("InflateParams")
    private void getRoutes() {
        loadingRoutes = true;
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", String.valueOf(getMyId()))
                    .addFormDataPart("maxId", maxId)
                    .addFormDataPart("excluded", "pending")
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
                            String type = dataObj.getString("type");
                            String locationFrom = dataObj.getString("locationFrom");
                            String locationTo = dataObj.getString("locationTo");
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
                            routeLayout.setTag(id);
                            int[] dateIntArr = convertDateToArray(routeDate);
                            int d = dateIntArr[0], m = dateIntArr[1], y = dateIntArr[2], statusBarColor;
                            String mon = monthsOnly[m], day = String.valueOf(d), year = String.valueOf(y);
                            if(d < 10)
                                day = "0" + d;
                            if(status.equals("cancel"))
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
                            routeLayout.setOnLongClickListener((v) -> {
                                try {
                                    setLayout = routeLayout;
                                    displayMenuOptions(dataObj);
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
    private void displayMenuOptions(JSONObject dataObj) throws JSONException {
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        String status = dataObj.getString("status");
        RelativeLayout optionBox = (RelativeLayout) getLayoutInflater().inflate(R.layout.option_box, null);
        LinearLayout box = optionBox.findViewById(R.id.box);
        for(int i = 0; i < 3; i++){
            boolean clickable = true;
            String menuText = menuTexts[i];
            int menuIcon = menuIcons[i], color = ContextCompat.getColor(context, R.color.mainColor);
            LinearLayout optionText = (LinearLayout) getLayoutInflater().inflate(R.layout.option_text, null);
            TextView textView = optionText.findViewById(R.id.textView);
            if(status.equals("pending") && i == 0) {
                clickable = false;
                menuIcon = menuIcons[3];
                color = ContextCompat.getColor(context, R.color.asher);
            } else if(!status.equals("pending") && i > 0) {
                clickable = false;
                menuIcon = menuIcons[i + 3];
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
                executeMenuOptions(dataObj, finalI);
            });
            box.addView(optionText);
        }
        blackFade.addView(optionBox);
        blackFade.setVisibility(View.VISIBLE);
    }

    private void executeMenuOptions(JSONObject object, int index) {
        blackFade.setVisibility(View.GONE);
        String objectString = object.toString();
        if(index == 1){
            Intent intent = new Intent(context, EditRouteActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("objectString", objectString);
            intent.putExtras(bundle);
            startActivity(intent);
            return;
        }
        displayOptionAction(object, index);
    }

    @SuppressLint("InflateParams")
    private void displayOptionAction(JSONObject object, int index) {
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        String negTxt = getResources().getString(R.string.no),
                posTxt = getResources().getString(R.string.yes),
                alertTxt = getResources().getString(R.string.cancel_route);
        if(index == 1){
            negTxt = getResources().getString(R.string.cancel);
            posTxt = getResources().getString(R.string.recreate);
            alertTxt = getResources().getString(R.string.recreate_route);
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
                if(index == 0) {
                    routeObject = object;
                    calendarLayout.setVisibility(View.VISIBLE);
                } else
                    cancelRoute(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        blackFade.addView(alertLayout);
        blackFade.setVisibility(View.VISIBLE);
    }

    @SuppressLint("InflateParams")
    private void recreateRoute(String routeDate, String passNum, boolean freeRide) throws Exception {
        String locationFromText = routeObject.getString("locationFrom");
        String locationToText = routeObject.getString("locationTo");
        String type = routeObject.getString("type");
        double latitudeFrom = routeObject.getDouble("latitudeFrom");
        double longitudeFrom = routeObject.getDouble("longitudeFrom");
        double latitudeTo = routeObject.getDouble("latitudeTo");
        double longitudeTo = routeObject.getDouble("longitudeTo");
        routeObject.put("passNum", passNum);
        routeObject.put("freeRide", String.valueOf(freeRide));
        routeObject.put("routeDate", routeDate);
        routeObject.put("status", "pending");
        routeObject.put("action", "createRoute");
        routeObject.put("user", String.valueOf(getMyId()));
        int[] dateIntArr = convertDateToArray(routeDate);
        int d = dateIntArr[0], m = dateIntArr[1], y = dateIntArr[2], statusBarColor;
        String mon = monthsOnly[m], day = String.valueOf(d), year = String.valueOf(y);
        if(d < 10)
            day = "0" + d;
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
        typeTextView.setText(type);
        loaderLayout.setVisibility(View.VISIBLE);
        routesLayout.addView(routeLayout, 0);
        scrollView.scrollTo(0, 0);
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("action", "createRoute")
                    .addFormDataPart("user", String.valueOf(getMyId()))
                    .addFormDataPart("type", type)
                    .addFormDataPart("passNum", passNum)
                    .addFormDataPart("freeRide", String.valueOf(freeRide))
                    .addFormDataPart("locationFrom", locationFromText)
                    .addFormDataPart("locationTo", locationToText)
                    .addFormDataPart("latitudeFrom", String.valueOf(latitudeFrom))
                    .addFormDataPart("longitudeFrom", String.valueOf(longitudeFrom))
                    .addFormDataPart("latitudeTo", String.valueOf(latitudeTo))
                    .addFormDataPart("longitudeTo", String.valueOf(longitudeTo))
                    .addFormDataPart("routeDate", routeDate)
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.actionsUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try (Response response = call.execute()) {
                if (response.isSuccessful()) {
                    selectedDate = null;
                    hour = null;
                    minute = null;
                    freeRideBox.setChecked(false);
                    passNumET.setText("");
                    String responseString = Objects.requireNonNull(response.body()).string();
                    JSONObject jsonObject = new JSONObject(responseString);
                    boolean noError = jsonObject.getBoolean("noError");
                    JSONObject object = routeObject;
                    if (noError) {
                        JSONObject dataObj = jsonObject.getJSONObject("dataStr");
                        String id = dataObj.getString("id");
                        String date = dataObj.getString("date");
                        routeObject.put("id", id);
                        routeObject.put("date", date);
                        statusBar.setBackgroundResource(R.drawable.radius_yellow);
                        loaderLayout.setVisibility(View.GONE);
                        routeLayout.setTag(id);
                        JSONObject finalObject = routeObject;
                        routeLayout.setOnLongClickListener((v) -> {
                            try {
                                setLayout = routeLayout;
                                displayMenuOptions(finalObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return false;
                        });
                    } else {
                        String key = String.valueOf(System.currentTimeMillis());
                        sharedPrefMngr.addPendingRoute(String.valueOf(getMyId()), key, routeObject);
                        loadingImageView.setVisibility(View.GONE);
                        errorTextView.setVisibility(View.VISIBLE);
                        loaderLayout.setOnClickListener((v) -> retryCreatingRoute(routeLayout, request, key, object));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    private void retryCreatingRoute(RelativeLayout routeLayout, Request request, String key, JSONObject jsonObject) {
        try {
            if(requestingObj.has(key)){
                boolean r = requestingObj.getBoolean(key);
                if(r)
                    return;
            }
            requestingObj.put(key, true);
            blackFade.setVisibility(View.GONE);
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
                    if (response.isSuccessful()) {
                        String responseString = Objects.requireNonNull(response.body()).string();
                        JSONObject object = new JSONObject(responseString);
                        boolean noError = object.getBoolean("noError");
                        requestingObj.remove(key);
                        if(noError) {
                            JSONObject dataObj = jsonObject.getJSONObject("dataStr");
                            String id = dataObj.getString("id");
                            String date = dataObj.getString("date");
                            jsonObject.put("id", id);
                            jsonObject.put("date", date);
                            loaderLayout.setVisibility(View.GONE);
                            sharedPrefMngr.clearPendingRoutes(String.valueOf(getMyId()), key);
                            statusBar.setBackgroundResource(R.drawable.radius_yellow);
                            routeLayout.setTag(id);
                            routeLayout.setOnLongClickListener((v) -> {
                                try {
                                    setLayout = routeLayout;
                                    displayMenuOptions(jsonObject);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                return false;
                            });
                        } else {
                            loadingImageView.setVisibility(View.GONE);
                            errorTextView.setVisibility(View.VISIBLE);
                            loaderLayout.setOnClickListener((v) -> retryCreatingRoute(routeLayout, request, key, jsonObject));
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
                        object.put("status", "cancel");
                        statusBar.setBackgroundResource(R.drawable.radius_red);
                        relativeLayout.setOnLongClickListener((v) -> {
                            try {
                                setLayout = relativeLayout;
                                displayMenuOptions(object);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return false;
                        });
                    } else
                        Toast.makeText(context, dataStr, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1000);
    }

    @Override
    public void onBackPressed() {
        if(timeViewLayout.getVisibility() == View.VISIBLE){
            timeViewLayout.setVisibility(View.GONE);
            return;
        }
        if(blackFade.getVisibility() == View.VISIBLE){
            blackFade.setVisibility(View.GONE);
            return;
        }
        if(calendarLayout.getVisibility() == View.VISIBLE){
            calendarLayout.setVisibility(View.GONE);
            return;
        }
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
                passNumET.clearFocus();
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