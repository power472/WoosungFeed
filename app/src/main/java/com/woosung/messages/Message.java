package com.woosung.messages;

public class Message {
    String title;
    String message;
    String datetime;

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public Message(String title, String message, String datetime) {
        this.title = title;
        this.message = message;
        this.datetime = datetime;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
}
