package com.workruta.android.ChatUtils;

public class Conversations {

    String id;
    LatestMessage latest_message;
    String name;
    String other_user_email;
    String userFrom;
    String userTo;
    int unseen;
    String photoUrl;

    public Conversations(){}

    public Conversations(String id, LatestMessage latest_message, String name, String other_user_email, String userFrom, String userTo, int unseen) {
        this.id = id;
        this.latest_message = latest_message;
        this.name = name;
        this.other_user_email = other_user_email;
        this.userFrom = userFrom;
        this.userTo = userTo;
        this.unseen = unseen;
    }

    public Conversations(String id, LatestMessage latest_message, String name, String other_user_email, String userFrom, String userTo, int unseen, String photoUrl) {
        this.id = id;
        this.latest_message = latest_message;
        this.name = name;
        this.other_user_email = other_user_email;
        this.userFrom = userFrom;
        this.userTo = userTo;
        this.unseen = unseen;
        this.photoUrl = photoUrl;
    }

    public String getId() {
        return id;
    }

    public LatestMessage getLatest_message() {
        return latest_message;
    }

    public String getName() {
        return name;
    }

    public String getOther_user_email() {
        return other_user_email;
    }

    public String getUserFrom() {
        return userFrom;
    }

    public String getUserTo() {
        return userTo;
    }

    public void setLatest_message(LatestMessage latest_message) {
        this.latest_message = latest_message;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOther_user_email(String other_user_email) {
        this.other_user_email = other_user_email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getUnseen() {
        return unseen;
    }

    public void setUnseen(int unseen) {
        this.unseen = unseen;
    }
}
