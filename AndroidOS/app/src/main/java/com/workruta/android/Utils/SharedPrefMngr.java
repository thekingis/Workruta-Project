package com.workruta.android.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Objects;

public class SharedPrefMngr {

    private final Context mCtx;
    private static final String shrdPrefName = "myShrdPref";
    private static final String myId = "myId";
    private static final String myPht = "myPht";
    private static final String myName = "myName";
    private static final String myEmail = "email";
    private static final String tempFilesPath = "tempFilesPath";
    private static final String phoneNumber = "phoneNumber";
    private static final String time = "time";
    private static final String verified = "verified";
    private static final String verifiedLicence = "verifiedLicence";
    private static final String carDetailed = "carDetailed";
    private static final String bankDetailed = "bankDetailed";
    private static final String fingerEnabled = "fingerEnabled";
    private static final String fingerId = "fingerId";
    private static final String fingerCache = "fingerCache";
    private static final String pendingRoutes = "pendingRoutes";

    public SharedPrefMngr(Context mCtx){
        this.mCtx = mCtx;
    }

    public void storeUserInfo(int id, String photo, String name, String email){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.putInt(myId, id);
        editor.putString(myPht, photo);
        editor.putString(myName, name);
        editor.putString(myEmail, email);
        editor.apply();
    }

    public void saveFingerCache(int id, String photo, String name, String email, String phone){
        try {
            SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = shrdPrf.edit();
            JSONObject object = new JSONObject();
            object.put("id", id);
            object.put("photo", photo);
            object.put("name", name);
            object.put("email", email);
            object.put("phone", phone);
            editor.putString(fingerCache, object.toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getFingerCachedObject() throws JSONException {
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        String objectStr = shrdPrf.getString(fingerCache, null);
        return new JSONObject(objectStr);
    }

    public void clearFingerCache(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.remove(fingerEnabled);
        editor.remove(fingerCache);
        editor.remove(fingerId);
        editor.apply();
    }

    public void savePhoto(String photo){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.putString(myPht, photo);
        editor.apply();
    }

    public void addPendingRoute(String user, String key, JSONObject object) throws JSONException {
        JSONObject jsonObject = getPendingRoutes(user);
        jsonObject.put(key, object);
        JSONObject allObject = getPendingRoutes();
        allObject.put(user, jsonObject);
        String arrayString = allObject.toString();
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.putString(pendingRoutes, arrayString);
        editor.apply();
    }

    public void verifyLicence(boolean verified){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.putBoolean(verifiedLicence, verified);
        editor.apply();
    }

    public void saveFingerPrint(boolean enabled, int id){
        if(!enabled)
            id = 0;
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.putBoolean(fingerEnabled, enabled);
        editor.putInt(fingerId, id);
        editor.apply();
        if(enabled)
            saveFingerCache(id, getMyPht(), getMyName(), getMyEmail(), getPhoneNumber());
    }

    public void detailCar(boolean state){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.putBoolean(carDetailed, state);
        editor.apply();
    }

    public void detailBank(boolean state){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.putBoolean(bankDetailed, state);
        editor.apply();
    }

    public void cachePhoneNumber(String number){
        long sec = System.currentTimeMillis();
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.putString(phoneNumber, number);
        editor.putLong(time, sec);
        editor.apply();
    }

    public void verifyPhoneNumber(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.putBoolean(verified, true);
        editor.apply();
    }

    public boolean checkCachedNumber(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getString(phoneNumber, null) != null;
    }

    public boolean getFingerPrintEnabled(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getBoolean(fingerEnabled, false);
    }

    public String getPhoneNumber(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getString(phoneNumber, null);
    }

    public JSONObject getPendingRoutes(String user){
        JSONObject jsonObject = new JSONObject();
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        String routes = shrdPrf.getString(pendingRoutes, null);
        if(!(routes == null)) {
            try {
                JSONObject object = new JSONObject(routes);
                if (object.has(user))
                    jsonObject = object.getJSONObject(user);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    public JSONObject getPendingRoutes(){
        JSONObject jsonObject = new JSONObject();
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        String routes = shrdPrf.getString(pendingRoutes, null);
        if(!(routes == null)) {
            try {
                jsonObject = new JSONObject(routes);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    public void clearPendingRoutes(String user, String key){
        try {
            JSONObject jsonObject = getPendingRoutes(user);
            if (jsonObject.has(key))
                jsonObject.remove(key);
            JSONObject object = getPendingRoutes();
            object.put(user, jsonObject);
            String objectStr = object.toString();
            SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = shrdPrf.edit();
            editor.putString(pendingRoutes, objectStr);
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void clearPendingRoutes(String user){
        JSONObject jsonObject = getPendingRoutes();
        if(jsonObject.has(user))
            jsonObject.remove(user);
        String objectStr = jsonObject.toString();
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.putString(pendingRoutes, objectStr);
        editor.apply();
    }

    public void clearPendingRoutes(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.putString(pendingRoutes, null);
        editor.apply();
    }

    public void clearExpiredRoutes(){
        try {
            long curTime = getLongTimeOfDay(System.currentTimeMillis());
            JSONObject object = getPendingRoutes();
            if (object.length() > 0) {
                for (int x = 0; x < object.length(); x++) {
                    String userKey = Objects.requireNonNull(object.names()).getString(x);
                    JSONObject jsonObject = object.getJSONObject(userKey);
                    if (jsonObject.length() > 0) {
                        for (int i = 0; i < jsonObject.length(); i++) {
                            String timeKey = Objects.requireNonNull(jsonObject.names()).getString(i);
                            long time = getLongTimeOfDay(Long.parseLong(timeKey));
                            if(curTime > time){
                                clearPendingRoutes(userKey, timeKey);
                            }
                        }
                    }
                }
            }
            SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = shrdPrf.edit();
            editor.putString(pendingRoutes, object.toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public long getLongTimeOfDay(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public long getCachedTime(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getLong(time, 0);
    }

    public boolean loggedIn(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getInt(myId, 0) != 0;
    }

    public boolean getCarDetailed(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getBoolean(carDetailed, false);
    }

    public boolean getBankDetailed(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getBoolean(bankDetailed, false);
    }

    public boolean getVerification(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getBoolean(verified, false);
    }

    public boolean getLicenceVerification(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getBoolean(verifiedLicence, false);
    }

    public boolean getSatisfaction(){
        return getPhoneNumber() != null && getVerification() && getBankDetailed() && getCarDetailed() && getLicenceVerification();
    }

    public void clearCachedNumber(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.remove(phoneNumber);
        editor.remove(time);
        editor.remove(verified);
        editor.apply();
    }

    public void logOut(){
        clearAll();
    }

    public void clearAll(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.remove(myId);
        editor.remove(myPht);
        editor.remove(myName);
        editor.remove(myEmail);
        editor.remove(phoneNumber);
        editor.remove(verifiedLicence);
        editor.remove(verified);
        editor.remove(carDetailed);
        editor.remove(bankDetailed);
        editor.apply();
    }

    public int getMyId(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getInt(myId, 0);
    }

    public int getFingerId(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getInt(fingerId, 0);
    }

    public String getMyPht(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getString(myPht, null);
    }

    public String getMyName(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getString(myName, null);
    }

    public String getMyEmail(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getString(myEmail, null);
    }

    public void stockTempFiles(String filePath){
        try {
            SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = shrdPrf.edit();
            String tempFilesPathStr = getTempFiles();
            JSONArray tempFileArray;
            if(tempFilesPathStr == null)
                tempFileArray = new JSONArray();
            else
                tempFileArray = new JSONArray(tempFilesPathStr);
            tempFileArray.put(filePath);
            tempFilesPathStr = tempFileArray.toString();
            editor.putString(tempFilesPath, tempFilesPathStr);
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getTempFiles(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        return shrdPrf.getString(tempFilesPath, null);
    }

    public void emptyTempFile(){
        SharedPreferences shrdPrf = mCtx.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shrdPrf.edit();
        editor.putString(tempFilesPath, null);
        editor.apply();
    }
}
