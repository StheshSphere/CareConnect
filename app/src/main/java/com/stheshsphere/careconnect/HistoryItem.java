package com.stheshsphere.careconnect;

public class HistoryItem {
    private String title;
    private String date;
    private String status;
    private String type;
    private int iconResId;

    public HistoryItem(String title, String date, String status, String type, int iconResId) {
        this.title = title;
        this.date = date;
        this.status = status;
        this.type = type;
        this.iconResId = iconResId;
    }

    // Getters and setters
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public String getType() { return type; }
    public int getIconResId() { return iconResId; }
}