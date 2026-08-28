package com.example.advanceassistant;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;

public class ForegroundAppMonitor {
    private final Context context;
    private final UsageStatsManager usageStatsManager;

    public ForegroundAppMonitor(Context context) {
        this.context = context;
        this.usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
    }

    public String getForegroundApp() {

        if (usageStatsManager == null) {
            return null;
        }

        long endTime = System.currentTimeMillis();
        long startTime = endTime - 10_000; // Last minute

        UsageEvents usageEvents = usageStatsManager.queryEvents(startTime, endTime);

        UsageEvents.Event event = new UsageEvents.Event();

        String foreGroundPackage = null;
        long latestTimeStamp = 0;

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                    if (event.getTimeStamp() > latestTimeStamp) {
                        latestTimeStamp = event.getTimeStamp();
                        foreGroundPackage = event.getPackageName();
                    }
                }
            } else {
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    if (event.getTimeStamp() > latestTimeStamp) {
                        latestTimeStamp = event.getTimeStamp();
                        foreGroundPackage = event.getPackageName();
                    }
                }
            }
        }
        return foreGroundPackage;
    }
}
