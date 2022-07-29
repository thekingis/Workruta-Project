package com.workruta.android.ChatUtils;

public class Chats {

    String content;
    String date;
    String id;
    boolean is_read;
    String name;
    String sender_email;
    String userFrom;
    String userTo;

    public Chats(){}

    public Chats(String content, String date, String id, boolean is_read, String name, String sender_email, String userFrom, String userTo) {
        this.content = content;
        this.date = date;
        this.id = id;
        this.is_read = is_read;
        this.name = name;
        this.sender_email = sender_email;
        this.userFrom = userFrom;
        this.userTo = userTo;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    public boolean isIs_read() {
        return is_read;
    }

    public String getName() {
        return name;
    }

    public String getSender_email() {
        return sender_email;
    }

    public String getUserFrom() {
        return userFrom;
    }

    public String getUserTo() {
        return userTo;
    }

}
