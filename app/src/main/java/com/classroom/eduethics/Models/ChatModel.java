package com.classroom.eduethics.Models;

public class ChatModel implements Comparable<ChatModel> {

    String id;
    String message;
    String time;
    String attachment;
    String name;

    public ChatModel() {

    }


    public ChatModel(String id, String message, String time, String attachment, String name) {
        this.id = id;
        this.message = message;
        this.time = time;
        this.attachment = attachment;
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }


    @Override
    public int compareTo(ChatModel o) {
        return this.getTime().compareTo(o.getTime());
    }
}
