package com.workruta.android.NotificationUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;

import com.workruta.android.OpenRouteActivity;
import com.workruta.android.PreviousRoutesActivity;
import com.workruta.android.R;
import com.workruta.android.RouteActivity;

import static com.workruta.android.Utils.Functions.stripeToDollar;

public class Notifications {

    String key;
    String dataId;
    String dataType;
    String date;
    String extraId;
    String userFrom;
    int unseen;

    public Notifications() {
    }

    public Notifications(String dataId, String dataType, String date, String extraId, String userFrom, int unseen) {
        this.dataId = dataId;
        this.dataType = dataType;
        this.date = date;
        this.extraId = extraId;
        this.userFrom = userFrom;
        this.unseen = unseen;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setExtraId(String extraId) {
        this.extraId = extraId;
    }

    public void setUserFrom(String userFrom) {
        this.userFrom = userFrom;
    }

    public void setUnseen(int unseen) {
        this.unseen = unseen;
    }

    public String getKey() {
        return key;
    }

    public String getDataId() {
        return dataId;
    }

    public String getDataType() {
        return dataType;
    }

    public String getDate() {
        return date;
    }

    public String getExtraId() {
        return extraId;
    }

    public String getUserFrom() {
        return userFrom;
    }

    public int getUnseen() {
        return unseen;
    }

    public Spanned getText(){
        Spanned htmlText;
        String text = null;
        switch (this.dataType){
            case "followRoute":
                text = "You have a request from a user to follow your ride";
                break;
            case "mergedRoute":
                text = "We have a match for your route";
                break;
            case "editRoute":
                text = "A route You are linked with have been edited. Check if it's okay with You";
                break;
            case "cancelRoute":
                text = "A route You are linked with have been cancelled. You can search for other available routes";
                break;
            case "rejected":
                text = "Your request to follow a ride has been declined. You can search for other available routes";
                break;
            case "accepted":
                text = "Your request to follow a ride has been approved";
                break;
            case "mergedRouteForTmrw":
                text = "<b><span style=\"color:#CC0000;\">Reminder:</span></b> You have a route for tomorrow";
                break;
            case "mergedRouteForToday":
                text = "<b><span style=\"color:#CC0000;\">Reminder:</span></b> You have a route for today";
                break;
            case "unmergedRouteForTmrw":
                text = "<b><span style=\"color:#CC0000;\">You have an unmerged route tomorrow. Please try to search for matching routes</span></b>";
                break;
            case "unmergedRouteForToday":
                text = "<b><span style=\"color:#CC0000;\">You have an unmerged route today. Please try to search for matching routes</span></b>";
                break;
            case "requestForTmrw":
                text = "<b>You have an unattended ride request for your route scheduled for tomorrow</b>";
                break;
            case "requestForToday":
                text = "<b>You have an unattended ride request for your route scheduled for today</b>";
                break;
            case "startRoute":
                text = "A ride you are linked to has been marked as <b><span style=\"color:#00CC00;\">Started</span></b>";
                break;
            case "endRoute":
                text = "A ride you are linked to has been marked as <b><span style=\"color:#CC0000;\">Ended</span></b>";
                break;
            case "rating":
                text = "<b><span style=\"color:#00CC00;\">Kudos:</span></b> You got a " + this.extraId + " rating from a ride user";
                break;
            case "payment":
                String amount = stripeToDollar(this.extraId);
                text = "<b><span style=\"color:#00CC00;\">You have received " + amount + " from " + this.userFrom + "</span></b>";
                break;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            htmlText = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
        else
            htmlText = Html.fromHtml(text);
        return htmlText;
    }

    public int getImage(){
        int imageResource = 0;
        switch (this.dataType){
            case "followRoute":
                imageResource = R.drawable.follow;
                break;
            case "mergedRoute":
                imageResource = R.drawable.match;
                break;
            case "editRoute":
                imageResource = R.drawable.edit_route;
                break;
            case "cancelRoute":
                imageResource = R.drawable.cancel_route;
                break;
            case "rejected":
                imageResource = R.drawable.rejected;
                break;
            case "accepted":
                imageResource = R.drawable.accepted;
                break;
            case "mergedRouteForTmrw":
            case "mergedRouteForToday":
                imageResource = R.drawable.merged;
                break;
            case "unmergedRouteForTmrw":
                imageResource = R.drawable.unmerged_tmrw;
                break;
            case "unmergedRouteForToday":
                imageResource = R.drawable.unmerged;
                break;
            case "requestForTmrw":
                imageResource = R.drawable.req_for_tmrw;
                break;
            case "requestForToday":
                imageResource = R.drawable.req_for_today;
                break;
            case "startRoute":
                imageResource = R.drawable.start_route;
                break;
            case "endRoute":
                imageResource = R.drawable.end_route;
                break;
            case "rating":
                imageResource = R.drawable.rating_star;
                break;
            case "payment":
                imageResource = R.drawable.paid;
                break;
        }
        return imageResource;
    }

    public Intent getIntent(Context context){
        Intent intent = null;
        Bundle bundle = new Bundle();
        switch (this.dataType){
            case "mergedRoute":
            case "editRoute":
            case "accepted":
            case "startRoute":
            case "endRoute":
                bundle.putString("routeIdFrom", this.dataId);
                bundle.putString("routeIdTo", this.extraId);
                intent = new Intent(context, RouteActivity.class);
                intent.putExtras(bundle);
                break;
            case "cancelRoute":
                bundle.putString("routeId", this.extraId);
                intent = new Intent(context, OpenRouteActivity.class);
                intent.putExtras(bundle);
                break;
            case "rating":
            case "payment":
            case "rejected":
            case "requestForTmrw":
            case "requestForToday":
            case "mergedRouteForTmrw":
            case "mergedRouteForToday":
            case "unmergedRouteForTmrw":
            case "unmergedRouteForToday":
                bundle.putString("routeId", this.dataId);
                intent = new Intent(context, OpenRouteActivity.class);
                intent.putExtras(bundle);
                break;
        }
        return intent;
    }
}
