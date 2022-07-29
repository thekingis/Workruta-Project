package com.workruta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import static com.workruta.android.PreviousRoutesActivity.saveEdit;
import static com.workruta.android.Utils.Constants.monthsOnly;
import static com.workruta.android.Utils.Util.convertDateToArray;
import static com.workruta.android.Utils.Util.convertDateToLong;

public class EditRouteActivity extends SharedCompatActivity {

    Context context;
    EditText fromEditText, toEditText, passNumET, editText;
    RelativeLayout mainView, calendarLayout;
    CheckBox freeRideBox;
    LinearLayout blackFade, whiteFade, hourLayout, minLayout, timeViewLayout, layout;
    TextView createButton, dateButton, distanceTextView, headText, timeSelectView, timeView, okayBtn;
    boolean locationFrom, freeRide, isProv;
    double[] fromLatLng = new double[2], toLatLng = new double[2];
    View poppedView;
    CalendarView calendarView;
    Functions functions;
    String type, dateStr, routeDate, selectedDate, id, oldFromStr, oldToStr, oldRouteDate, passNum;
    JSONObject object;
    int activityIndex;
    boolean requesting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_route);
        context = this;
        requesting = false;

        Bundle bundle = getIntent().getExtras();
        String objectString = bundle.getString("objectString");
        activityIndex = bundle.getInt("activity");

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long currentDate = cal.getTimeInMillis();
        functions = new Functions();


        mainView = findViewById(R.id.mainView);
        headText = findViewById(R.id.headText);
        blackFade = findViewById(R.id.blackFade);
        whiteFade = findViewById(R.id.whiteFade);
        passNumET = findViewById(R.id.passNumET);
        freeRideBox = findViewById(R.id.freeRideBox);
        calendarLayout = findViewById(R.id.calendarLayout);
        distanceTextView = findViewById(R.id.distanceTextView);
        dateButton = findViewById(R.id.dateButton);
        createButton = findViewById(R.id.createButton);
        calendarView = findViewById(R.id.calendarView);
        toEditText = findViewById(R.id.toEditText);
        fromEditText = findViewById(R.id.fromEditText);
        hourLayout = findViewById(R.id.hourLayout);
        minLayout = findViewById(R.id.minLayout);
        layout = findViewById(R.id.layout);
        timeViewLayout = findViewById(R.id.timeViewLayout);
        okayBtn = findViewById(R.id.okayBtn);
        timeView = findViewById(R.id.timeView);
        timeSelectView = findViewById(R.id.timeSelectView);

        try {
            object = new JSONObject(objectString);
            type = object.getString("type");
            id = object.getString("id");
            oldFromStr = object.getString("locationFrom");
            oldToStr = object.getString("locationTo");
            oldRouteDate = object.getString("routeDate");
            passNum = object.getString("passNum");
            freeRide = object.getBoolean("freeRide");
            double latitudeFrom = object.getDouble("latitudeFrom");
            double longitudeFrom = object.getDouble("longitudeFrom");
            double latitudeTo = object.getDouble("latitudeTo");
            double longitudeTo = object.getDouble("longitudeTo");
            isProv = type.equals("P");
            if(!isProv)
                layout.setVisibility(View.GONE);
            routeDate = oldRouteDate;
            fromLatLng[0] = latitudeFrom;
            fromLatLng[1] = longitudeFrom;
            toLatLng[0] = latitudeTo;
            toLatLng[1] = longitudeTo;
            freeRideBox.setChecked(freeRide);
            passNumET.setText(passNum);
            toEditText.setText(oldToStr);
            fromEditText.setText(oldFromStr);
            long time = convertDateToLong(oldRouteDate);
            calendarView.setDate(time);
            int[] dateIntArr = convertDateToArray(oldRouteDate);
            int d = dateIntArr[0], m = dateIntArr[1], y = dateIntArr[2], hr = dateIntArr[3], min = dateIntArr[3];
            String mon = monthsOnly[m], day = String.valueOf(d), year = String.valueOf(y), h = String.valueOf(hr), mn = String.valueOf(min);
            if(d < 10)
                day = "0" + d;
            if(hr < 10)
                h = "0" + h;
            if(min < 10)
                mn = "0" + mn;
            dateStr = day + " " + mon + " " + year + " (" + h + ":" + mn + ")";
            dateButton.setText(dateStr);
            double distance = functions.getDistance(latitudeFrom, longitudeFrom, latitudeTo, longitudeTo, null);
            String distanceStr = distance + "mi";
            distanceTextView.setText(distanceStr);
            distanceTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_car, 0, 0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            poppedView = calendarLayout;
            calendarLayout.setVisibility(View.VISIBLE);
        });
        createButton.setOnClickListener((v) -> editRoute());
        blackFade.setOnClickListener((v) -> {
            return;
        });
        whiteFade.setOnClickListener((v) -> {
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
    }

    private void editRoute() {
        try {
            requesting = true;
            RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.slide_loader, null);
            whiteFade.addView(layout);
            whiteFade.setVisibility(View.VISIBLE);
            String locationFromText = fromEditText.getText().toString(),
                    locationToText = toEditText.getText().toString(),
                    passNumX = passNumET.getText().toString();
            boolean freeRideX = freeRideBox.isChecked();
            if (oldRouteDate.equals(routeDate) && oldFromStr.equals(locationFromText) && oldToStr.equals(locationToText)
                    && passNum.equals(passNumX) && freeRide == freeRideX) {
                Toast.makeText(context, "No changes made", Toast.LENGTH_LONG).show();
                return;
            }
            if (isProv && StringUtils.isEmpty(passNum)) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_LONG).show();
                return;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", id);
            jsonObject.put("locationFrom", locationFromText);
            jsonObject.put("locationTo", locationToText);
            jsonObject.put("passNum", passNumX);
            jsonObject.put("freeRide", String.valueOf(freeRideX));
            jsonObject.put("latitudeFrom", String.valueOf(fromLatLng[0]));
            jsonObject.put("longitudeFrom", String.valueOf(fromLatLng[1]));
            jsonObject.put("latitudeTo", String.valueOf(toLatLng[0]));
            jsonObject.put("longitudeTo", String.valueOf(toLatLng[1]));
            jsonObject.put("routeDate", routeDate);
            jsonObject.put("dateStr", dateStr);
            new android.os.Handler().postDelayed(() -> {
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("user", String.valueOf(getMyId()))
                        .addFormDataPart("action", "editRoute")
                        .addFormDataPart("id", id)
                        .addFormDataPart("type", type)
                        .addFormDataPart("freeRide", String.valueOf(freeRideX))
                        .addFormDataPart("passNum", passNumX)
                        .addFormDataPart("locationFrom", locationFromText)
                        .addFormDataPart("locationTo", locationToText)
                        .addFormDataPart("latitudeFrom", String.valueOf(fromLatLng[0]))
                        .addFormDataPart("longitudeFrom", String.valueOf(fromLatLng[1]))
                        .addFormDataPart("latitudeTo", String.valueOf(toLatLng[0]))
                        .addFormDataPart("longitudeTo", String.valueOf(toLatLng[1]))
                        .addFormDataPart("routeDate", routeDate)
                        .build();
                Request request = new Request.Builder()
                        .url(Constants.actionsUrl)
                        .post(requestBody)
                        .build();

                OkHttpClient okHttpClient = new OkHttpClient();
                Call call = okHttpClient.newCall(request);
                try(Response response = call.execute()) {
                    if (response.isSuccessful()) {
                        String responseString = Objects.requireNonNull(response.body()).string();
                        JSONObject object1 = new JSONObject(responseString);
                        String dataStr = object1.getString("dataStr");
                        Toast.makeText(context, dataStr, Toast.LENGTH_LONG).show();
                        if(activityIndex == 0)
                            saveEdit(jsonObject);
                        else if(activityIndex == 1)
                            OpenRouteActivity.hasBeenEdited = true;
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 1000);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void openAddressAutoComplete(EditText editText, boolean locationFrom) {
        this.editText = editText;
        this.locationFrom = locationFrom;
        List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList)
                .setCountry(Constants.country).build(context);
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
        if(requesting)
            return;
        if(timeViewLayout.getVisibility() == View.VISIBLE){
            timeViewLayout.setVisibility(View.GONE);
            return;
        }
        if(poppedView != null){
            poppedView.setVisibility(View.GONE);
            poppedView = null;
            return;
        }
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