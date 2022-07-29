package com.workruta.android.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.text.format.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.workruta.android.Utils.Statics.ALL_RUNNING_ACTIVITIES;

public class Functions {

    public boolean isDateInYesterday(Date date){
        return DateUtils.isToday(date.getTime() + DateUtils.DAY_IN_MILLIS);
    }

    @SuppressLint("SimpleDateFormat")
    public String minify(String date) {
        String newDate = "";
        try {
            Date fromDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
            Date now = new Date();
            long numOfSecs = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - Objects.requireNonNull(fromDate).getTime());
            if (numOfSecs < 60) {
                newDate = "Just now";
            } else{
                long min = numOfSecs / 60;
                if (min == 1) {
                    newDate = "1 minute ago";
                } else{
                    if (min < 60) {
                        newDate = String.valueOf(min) + " minutes ago";
                    } else{
                        long hr = min / 60;
                        if (hr == 1) {
                            newDate = "1 hour ago";
                        } else{
                            if (hr < 24) {
                                newDate = String.valueOf(hr) + " hours ago";
                            } else{
                                boolean isYstrdy = isDateInYesterday(fromDate);
                                if (isYstrdy) {
                                    newDate = "Yesterday";
                                } else{
                                    Calendar calendar = Calendar.getInstance();
                                    Calendar calendarNow = Calendar.getInstance();
                                    calendar.setTimeInMillis(fromDate.getTime());
                                    long days = hr / 24;
                                    if (days < 7) {
                                        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
                                        newDate = new DateFormatSymbols().getShortWeekdays()[weekDay];
                                    } else{
                                        int year = calendar.get(Calendar.YEAR);
                                        int month = calendar.get(Calendar.MONTH);
                                        int yearExtra = calendarNow.get(Calendar.YEAR);
                                        String mnth = Constants.months[month];
                                        if (yearExtra > year) {
                                            newDate = year + " ";
                                        }
                                        newDate += mnth;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newDate;
    }

    @SuppressLint("SimpleDateFormat")
    public String miniDate(String date) {
        String newDate = "";
        try {
            Date fromDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
            Date now = new Date();
            long numOfSecs = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - Objects.requireNonNull(fromDate).getTime());
            if (numOfSecs < 60) {
                newDate = "Just now";
            } else{
                long min = numOfSecs / 60;
                if (min == 1) {
                    newDate = "1 minute ago";
                } else{
                    if (min < 60) {
                        newDate = String.valueOf(min) + " minutes ago";
                    } else{
                        long hr = min / 60;
                        if (hr == 1) {
                            newDate = "1 hour ago";
                        } else{
                            if (hr < 24) {
                                newDate = String.valueOf(hr) + " hours ago";
                            } else{
                                Calendar calendar = Calendar.getInstance();
                                Calendar calendarNow = Calendar.getInstance();
                                calendar.setTimeInMillis(fromDate.getTime());
                                boolean isYstrdy = isDateInYesterday(fromDate);
                                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                                int minute = calendar.get(Calendar.MINUTE);
                                String prompt = hour > 12 ? "pm" : "am";
                                prompt = hour == 12 ? "noon" : prompt;
                                int h = hour > 12 ? hour - 12 : hour;
                                String hStr = h < 10 ? "0" + h : String.valueOf(h);
                                String mStr = minute < 10 ? ":0" + minute : ":" + minute;
                                String time = " at " + hStr + mStr + prompt;
                                if (isYstrdy) {
                                    newDate = "Yesterday" + time;
                                } else{
                                    long days = hr / 24;
                                    if (days < 7) {
                                        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
                                        String dayOfWeek = new DateFormatSymbols().getShortWeekdays()[weekDay];
                                        newDate = dayOfWeek + time;
                                    } else{
                                        int year = calendar.get(Calendar.YEAR);
                                        int month = calendar.get(Calendar.MONTH);
                                        int yearExtra = calendarNow.get(Calendar.YEAR);
                                        String mnth = Constants.months[month];
                                        if (yearExtra > year) {
                                            newDate = year + " ";
                                        }
                                        newDate += mnth + time;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newDate;
    }

    public String safeEmail(String email){
        String safeEmail = email.replaceAll("@", "-");
        safeEmail = safeEmail.replaceAll("\\.", "-");
        return safeEmail;
    }

    public String getStripeCost(double distance){
        double ratio = 0.02;
        double cents = distance / ratio;
        double dollar = roundUpDoubles((cents / 100), 2);
        int stripeCost = (int) (dollar * 100);
        stripeCost = Math.max(stripeCost, 50);
        return String.valueOf(stripeCost);
    }

    public String safeUrl(String url){
        String safeUrl = url.replaceAll("/", "~");
        safeUrl = safeUrl.replaceAll("\\.", "_");
        return safeUrl;
    }

    public String rawUrl(String safeUrl){
        String rawUrl = safeUrl.replaceAll("~", "/");
        rawUrl = rawUrl.replaceAll("_", ".");
        return rawUrl;
    }

    public static Bitmap decodeFiles(String path, boolean resize) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if(resize)
            return decodeBitmap(bitmap);
        else
            return bitmap;
    }

    public static Bitmap decodeBitmap(Bitmap bitmap) {
        int btmpW, btmpH, cropSize, startX = 0, startY = 0;
        btmpW = bitmap.getWidth();
        btmpH = bitmap.getHeight();
        cropSize = btmpH;
        if(btmpW >= btmpH){
            startX = (btmpW / 2) - (btmpH / 2);
        } else {
            startY = (btmpH / 2) - (btmpW / 2);
            cropSize = btmpW;
        }
        return Bitmap.createBitmap(bitmap, startX, startY, cropSize, cropSize);
    }

    public boolean getLocationOn(Context context){
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public double getDistance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if(!(unit == null)) {
            if(unit.equals("K"))
                dist = dist * 1.609344;
            else if(unit.equals("N"))
                dist = dist * 0.8684;
        }
        return roundUpDoubles(dist, 2);
    }

    public String getRouteTime(double lat1, double lon1, double lat2, double lon2){
        double distance = getDistance(lat1, lon1, lat2, lon2, "K");
        distance *= 60 * 60;
        double speed = 50.00;
        int fraction = (int) ((int) distance / speed);
        int min = fraction / 60;
        int hr = min / 60;
        min %= 60;
        String h = stringDouble(hr),
                m = stringDouble(min);
        return h + "hr(s) and " + m + "min(s)";
    }

    public String stringDouble(int number){
        String num = String.valueOf(number);
        if(number < 10)
            num = "0" + num;
        return num;
    }

    public double[][] jsonToDoubles(JSONArray array){
        double[][] doubles = new double[2][2];
        try {
            for(int i = 0; i < 2; i++)
                for(int x = 0; x < 2; x++)
                    doubles[i][x] = array.getJSONArray(i).getDouble(x);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return doubles;
    }

    public static double roundUpDoubles(double value, int places){
        if(places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public static String getTripCost(double distance){
        //0.02mi = 1cent
        double ratio = 0.02;
        double cents = distance / ratio;
        double dollar = roundUpDoubles((cents / 100), 2);
        dollar = Math.max(dollar, 0.5);
        return "$" + dollar;
    }

    public static String stripeToDollar(String amount){
        double cost = Double.parseDouble(amount);
        double dollar = cost / 100;
        return "$" + dollar;
    }

    public static void closeApp(){
        try {
            for (int i = 0; i < ALL_RUNNING_ACTIVITIES.length(); i++) {
                Activity activity = (Activity) ALL_RUNNING_ACTIVITIES.get(i);
                if(!(activity == null))
                    activity.finish();
            }
            System.exit(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String formatPhoneNumber(String phoneNumber){
        String phone = "+1 ";
        phone += phoneNumber.substring(0, 3) + " ";
        phone += phoneNumber.substring(3, 6) + " ";
        phone += phoneNumber.substring(6);
        return phone;
    }

}
