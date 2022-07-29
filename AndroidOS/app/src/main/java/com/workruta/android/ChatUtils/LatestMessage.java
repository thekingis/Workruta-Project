package com.workruta.android.ChatUtils;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LatestMessage {

    String date;
    boolean is_read;
    String message;
    String userFrom;
    String userTo;

    public LatestMessage(){}

    public LatestMessage(String date, boolean is_read, String message, String userFrom, String userTo) {
        this.date = date;
        this.is_read = is_read;
        this.message = message;
        this.userFrom = userFrom;
        this.userTo = userTo;
    }

    public String getDate() {
        return date;
    }

    @SuppressLint("SimpleDateFormat")
    public Date convertDate() {
        try {
            String dateStr = getDate();
            return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isIs_read() {
        return is_read;
    }

    public String getMessage() {
        return message;
    }

    public String getUserFrom() {
        return userFrom;
    }

    public String getUserTo() {
        return userTo;
    }
}
