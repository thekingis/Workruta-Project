package com.workruta.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stripe.android.PaymentConfiguration;
import com.workruta.android.Interface.InfoChangeListener;
import com.workruta.android.Interface.LocationChangeListener;
import com.workruta.android.Utils.Constants;
import com.workruta.android.Utils.Functions;
import com.workruta.android.Utils.ImageLoader;
import com.workruta.android.Utils.SharedPrefMngr;
import com.workruta.android.Utils.Statics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import io.socket.client.IO;
import io.socket.client.Socket;

import static com.workruta.android.Utils.Constants.socketUrl;
import static com.workruta.android.Utils.Functions.closeApp;
import static com.workruta.android.Utils.Statics.ALL_RUNNING_ACTIVITIES;

public class SharedCompatActivity extends AppCompatActivity implements LocationListener {

    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int ACCESS_DENIED = 0;
    private static final int ACCESS_GRANTED = -1;
    private static final int REQUEST_CODE = 1;
    private static final int UPDATE_INTERVAL = 10 * 1000;
    private static final int FASTEST_INTERVAL = 1000;
    String GPS_PROVIDER = LocationManager.GPS_PROVIDER;
    LocationChangeListener onLocationChangeListener;
    InfoChangeListener onInfoChangeListener;
    String myName, locationName, activityName, hour, minute;
    Activity activity;
    Context context;
    LocationManager locationManager;
    LocationRequest locationRequest;
    LocationListener listener;
    boolean LOCATION_GPS_ON;
    PlacesClient placesClient;
    Socket socket;
    private int myId;
    SharedPrefMngr sharedPrefMngr;
    ImageLoader imageLoader;
    Functions functions;
    DatabaseReference dbRef;
    public static Handler UIHandler;
    static {
        UIHandler = new Handler(Looper.getMainLooper());
    }

    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        activity = this;
        listener = this;
        functions = new Functions();
        imageLoader = new ImageLoader(this);
        activityName = getClass().getName();
        sharedPrefMngr = new SharedPrefMngr(this);
        sharedPrefMngr.clearExpiredRoutes();
        myId = sharedPrefMngr.getMyId();
        myName = sharedPrefMngr.getMyName();
        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.firebaseUrl);
        PaymentConfiguration.init(context, Constants.stripePKTestAPIKey);
        ALL_RUNNING_ACTIVITIES.put(activity);
        LOCATION_GPS_ON = new Functions().getLocationOn(context);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        Places.initialize(context, Constants.mapAPIKey);
        placesClient = Places.createClient(context);
        if(myId > 0) {
            try {
                socket = IO.socket(socketUrl);
                socket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> socket.emit("connected", myId)));
                //socket.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("InflateParams")
    public void populateViewContent(LinearLayout linearLayout, int max){
        for(int i = 0; i < max; i++){
            LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.option_text_view, null);
            TextView textView = layout.findViewById(R.id.textView);
            String s = String.valueOf(i);
            if(i < 10)
                s = "0" + s;
            textView.setText(s);
            String finalS = s;
            textView.setOnClickListener((v) -> selectOption(linearLayout, textView, max, finalS));
            linearLayout.addView(layout);
        }
    }

    public void selectOption(LinearLayout linearLayout, TextView textView, int max, String s) {
        if(max == 24)
            hour = s;
        else if(max == 60)
            minute = s;
        for(int i = 0; i <linearLayout.getChildCount(); i++){
            LinearLayout layout = (LinearLayout) linearLayout.getChildAt(i);
            TextView textView1 = layout.findViewById(R.id.textView);
            textView1.setBackgroundResource(R.color.white);
            textView1.setTextColor(ContextCompat.getColor(context, R.color.asher));
        }
        if(!(textView == null)) {
            textView.setBackgroundResource(R.color.black);
            textView.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
    }

    public int getMyId(){
        return myId;
    }

    public PlacesClient getPlacesClient(){
        return placesClient;
    }

    public void analyzeLocationState() {
        LocationSettingsRequest.Builder settingsBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        settingsBuilder.setAlwaysShow(true);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this)
                .checkLocationSettings(settingsBuilder.build());
        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);
            } catch (ApiException ex) {
                switch (ex.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvableApiException = (ResolvableApiException) ex;
                            resolvableApiException.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException ignored) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    public Socket getSocket(){
        return socket;
    }

    public String getLocationName() {
        locationName = null;
        if (LOCATION_GPS_ON){
            Location locations = getLastKnownLocation();
            List<String> providerList = locationManager.getAllProviders();
            if(locations != null && providerList != null && providerList.size() > 0){
                double longitude = locations.getLongitude();
                double latitude = locations.getLatitude();
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if(listAddresses != null && listAddresses.size() > 0){
                        locationName = listAddresses.get(0).getAddressLine(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return locationName;
    }

    private Location getLastKnownLocation() {
        Location bestLocation = null;
        if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
            List<String> providers = locationManager.getProviders(true);
            for (String provider : providers) {
                Location l = locationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = l;
                }
            }
        }
        return bestLocation;
    }

    public void setOnLocationChangeListener(LocationChangeListener onLocationChangeListener) {
        this.onLocationChangeListener = onLocationChangeListener;
    }

    public void setOnInfoChangeListener(InfoChangeListener onInfoChangeListener) {
        this.onInfoChangeListener = onInfoChangeListener;
    }

    public void setActivityName(String name){
        Statics.activityName = name;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == ACCESS_DENIED)
            closeApp();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(!(onLocationChangeListener == null))
            onLocationChangeListener.locationChanged(this);
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {

    }

    @Override
    public void onFlushComplete(int requestCode) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LOCATION_GPS_ON = true;
        if(!(onLocationChangeListener == null))
            onLocationChangeListener.locationChanged(this);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LOCATION_GPS_ON = false;
        if(!(Statics.activityName == null) && activityName.equals(Statics.activityName))
            analyzeLocationState();
    }

    @Override
    protected void onResume () {
        super.onResume() ;
        setActivityName(activityName) ;
        if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED))
            locationManager.requestLocationUpdates(GPS_PROVIDER, 6000, 0, listener);
    }

    @Override
    protected void onPause () {
        clearReferences() ;
        super.onPause() ;
    }

    @Override
    protected void onDestroy () {
        clearReferences() ;
        super.onDestroy() ;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (socket != null) {
            socket.emit("disconnected", getMyId());
        }
    }

    private void clearReferences () {
        if(activityName.equals(Statics.activityName))
            setActivityName(null);
    }

    public void receiveInfoChange(String key, JSONObject object) throws Exception {
        if(!(onInfoChangeListener == null))
            onInfoChangeListener.infoChanged(key, object);
    }
}