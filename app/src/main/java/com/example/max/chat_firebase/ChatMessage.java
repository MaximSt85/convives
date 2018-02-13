package com.example.max.chat_firebase;

import java.io.Serializable;

/**
 * Created by Max on 14.11.2017.
 */

public class ChatMessage implements Serializable {

    private String messageText;
    private String messageUser;
    private String receiverId;
    private String senderId;
    private long messageTime;
    private int isDelieveredToFirebase;
    private int isDelieveredToUser;

    public ChatMessage(String messageText, String messageUser, String receiverId, String senderId, long time,
                       int isDelieveredToFirebase, int isDelieveredToUser) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.receiverId = receiverId;
        this.senderId = senderId;
        // Initialize to current time
        //messageTime = new Date().getTime();
        this.messageTime = time;
        this.isDelieveredToFirebase = isDelieveredToFirebase;
        this.isDelieveredToUser = isDelieveredToUser;
    }

    public ChatMessage(){}

    public String getMessageText() {
        return messageText;
    }
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }
    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }
    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getReceiverId() {return receiverId;}
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderId() {return senderId;}
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public int getIsDelieveredToFirebase() {return isDelieveredToFirebase;}
    public void setIsDelieveredToFirebase(int isDelieveredToFirebase) {this.isDelieveredToFirebase = isDelieveredToFirebase;}

    public int getIsDelieveredToUser() {return isDelieveredToUser;}
    public void setIsDelieveredToUser(int isDelieveredToUser) {this.isDelieveredToUser = isDelieveredToUser;}
}