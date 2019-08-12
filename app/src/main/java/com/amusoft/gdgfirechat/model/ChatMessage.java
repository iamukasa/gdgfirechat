package com.amusoft.gdgfirechat.model;


public class ChatMessage {
    private String author;
    public String message;

    // Required default constructor for Firebase object mapping
    @SuppressWarnings("unused")
    private ChatMessage() {
    }


    public ChatMessage(String message, String author) {
        super();

        this.message = message;
        this.author = author;
    }
    public String getMessage() {
        return message;
    }

    public String getAuthor() {
        return author;
    }
}