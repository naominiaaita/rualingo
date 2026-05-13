package com.app.rualingoapplication.database;

public class Message {

    private String text;
    private boolean isUser; // true if user sent it, false if Rua sent it

    public Message(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
    }

    public String getText() { return text; }
    public boolean isUser() { return isUser; }
}
