package com.example.advanceassistant;

public class NotificationData {
    private final String packageName;
    private final String appName;
    private final String title;
    private final String content;
    private final long timestamp;

    public NotificationData(String packageName, String appName, String title, String content, long timestamp) {
        this.packageName = packageName;
        this.appName = appName;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAppName() {
        return appName;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
