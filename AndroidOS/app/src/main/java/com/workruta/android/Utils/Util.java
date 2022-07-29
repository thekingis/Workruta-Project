package com.workruta.android.Utils;

import android.annotation.SuppressLint;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class Util {

    public static void copyStream(InputStream is, OutputStream os){
        final int bufferSize = 1024;
        try {
            byte[] bytes = new byte[bufferSize];
            while (true){
                int count = is.read(bytes, 0, bufferSize);
                if(count == -1){
                    break;
                }
                os.write(bytes, 0, count);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static int[] convertDateToArray(String dateStr) throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(dateStr);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Objects.requireNonNull(date));
        return new int[]{
                calendar.get(Calendar.DATE),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.HOUR),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND)
        };
    }

    @SuppressLint("SimpleDateFormat")
    public static long convertDateToLong(String dateStr) throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(dateStr);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Objects.requireNonNull(date));
        return calendar.getTimeInMillis();
    }

    @SuppressLint("SimpleDateFormat")
    public static String convertDateToString(String dateStr, boolean returnTime) throws Exception {
        SimpleDateFormat simpleDateFormat;
        if(dateStr.length() > 10)
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        else
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = simpleDateFormat.parse(dateStr);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Objects.requireNonNull(date));
        int y = calendar.get(Calendar.YEAR);
        int m = calendar.get(Calendar.MONTH);
        int d = calendar.get(Calendar.DATE);
        int h = calendar.get(Calendar.HOUR);
        int i = calendar.get(Calendar.MINUTE);
        String day = new Functions().stringDouble(d);
        String hr = new Functions().stringDouble(h);
        String min = new Functions().stringDouble(i);
        String mon = Constants.monthsOnly[m];
        String s = day + " " + mon + " " + y;
        if(returnTime)
            s += " (" + hr + ":" + min + ")";
        return s;
    }

    public static int[] convertStringToArrayInt(String str, String delimiter) {
        String[] strings = str.split(delimiter);
        int[] ints = new int[strings.length];
        for(int i = 0; i < strings.length; i++){
            ints[i] = Integer.parseInt(strings[i]);
        }
        return ints;
    }

}
