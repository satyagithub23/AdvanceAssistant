package com.example.advanceassistant;

public class SignalSnapshot {
    private final AppUsageSignal currentApp;
    private final AppUsageSignal previousApp;
    private final long timeStamp;
    private final int appSwitchCount;


    public SignalSnapshot(AppUsageSignal currentApp, AppUsageSignal previousApp, long timeStamp, int appSwitchCount) {
        this.currentApp = currentApp;
        this.previousApp = previousApp;
        this.timeStamp = timeStamp;
        this.appSwitchCount = appSwitchCount;
    }

    public AppUsageSignal getCurrentApp() {
        return currentApp;
    }

    public AppUsageSignal getPreviousApp() {
        return previousApp;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getAppSwitchCount() {
        return appSwitchCount;
    }
}
