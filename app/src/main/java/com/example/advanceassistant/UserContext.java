package com.example.advanceassistant;

public class UserContext {
    private final String foregroundApp;
    private final boolean phoneActive;
    private final boolean screenOn;
    private final boolean laptopActive;
    private final boolean userWorking;
    private final boolean callActive;
    private final boolean unknownCaller;

    public UserContext(String foregroundApp, boolean phoneActive, boolean screenOn, boolean laptopActive, boolean userWorking, boolean callActive, boolean unknownCaller) {
        this.foregroundApp = foregroundApp;
        this.phoneActive = phoneActive;
        this.screenOn = screenOn;
        this.laptopActive = laptopActive;
        this.userWorking = userWorking;
        this.callActive = callActive;
        this.unknownCaller = unknownCaller;
    }

    public String getForegroundApp() {
        return foregroundApp;
    }

    public boolean isPhoneActive() {
        return phoneActive;
    }

    public boolean isScreenOn() {
        return screenOn;
    }

    public boolean isLaptopActive() {
        return laptopActive;
    }

    public boolean isUserWorking() {
        return userWorking;
    }

    public boolean isCallActive() {
        return callActive;
    }

    public boolean isUnknownCaller() {
        return unknownCaller;
    }
}
