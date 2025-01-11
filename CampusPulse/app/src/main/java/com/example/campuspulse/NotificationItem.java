package com.example.campuspulse;

public class NotificationItem {
    public String NotificationId;
    public String eventType;
    public int imageResourceId;
    public EventAdapter.Event event;
    public String clubId;
    public clubdetails.Announcement  announcement;


    public NotificationItem() {}

    public NotificationItem(String eventType, int imageResourceId, String NotificationId) {
        this.eventType = eventType;
        this.NotificationId = NotificationId;
        this.imageResourceId = imageResourceId;
    }

    public String getEventType() {
        return eventType;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }
}
