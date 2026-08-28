package com.example.advanceassistant;

public class AppUsageSignal {
    private final String packageName;
    private long timeStamp;
    private final long durationMillis;

    public AppUsageSignal(String packageName, long durationMillis, long timeStamp) {
        this.packageName = packageName;
        this.durationMillis = durationMillis;
        this.timeStamp = timeStamp;
    }


    public String getPackageName() {
        return packageName;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getDurationMillis() {
        return durationMillis;
    }
}
