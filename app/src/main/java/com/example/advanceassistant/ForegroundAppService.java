package com.example.advanceassistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ForegroundAppService extends Service {

    private static final String TAG = "ForegroundAppService";
    private static final String CHANNEL_IO = "foreground_app_monitor";
    private static final long CHECK_INTERNAL_MS = 1000;
    private static final int NOTIFICATION_IO = 1001;
    private UsageStatsManager usageStatsManager;
    private Handler handler;
    private String lastForegroundPackage = null;
    private final Runnable foregroundChecker = new Runnable() {
        @Override
        public void run() {
            String currentPackage = getForegroundPackage();
            if(currentPackage != null && !currentPackage.equals(lastForegroundPackage)) {
                lastForegroundPackage = currentPackage;
                Log.d(TAG, "Foreground App: " + currentPackage);
            }
            handler.postDelayed(this, CHECK_INTERNAL_MS);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "ForegroundAppService created");
        usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);

        handler = new Handler(Looper.getMainLooper());

        createNotificationChannel();

        startForeground(NOTIFICATION_IO, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Foreground App Service started");

        handler.removeCallbacks(foregroundChecker);

        handler.post(foregroundChecker);

        return START_STICKY;
    }

    private String getForegroundPackage() {
        if(usageStatsManager == null){
            return null;
        }

        long endTIme = System.currentTimeMillis();
        long startTime = endTIme - 10_000;

        UsageEvents events = usageStatsManager.queryEvents(startTime, endTIme);
        if (events == null) {
            return null;
        }

        UsageEvents.Event event =
                new UsageEvents.Event();

        String latestPackage = null;

        long latestTimestamp = 0;

        while (events.hasNextEvent()) {

            events.getNextEvent(event);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                if (event.getEventType()
                        == UsageEvents.Event.ACTIVITY_RESUMED) {

                    if (event.getTimeStamp() > latestTimestamp) {

                        latestTimestamp =
                                event.getTimeStamp();

                        latestPackage =
                                event.getPackageName();
                    }
                }

            } else {

                if (event.getEventType()
                        == UsageEvents.Event.MOVE_TO_FOREGROUND) {

                    if (event.getTimeStamp() > latestTimestamp) {

                        latestTimestamp =
                                event.getTimeStamp();

                        latestPackage =
                                event.getPackageName();
                    }
                }
            }
        }
        return latestPackage;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_IO,
                    "Foreground App Monitor",
                    NotificationManager.IMPORTANCE_LOW
            );

            channel.setDescription(
                    "Monitors the current foreground application"
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_IO)
                .setContentTitle("Advance Assistant")
                .setContentText("Monitoring foreground application")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Foreground App Service destroyed");
        if(handler != null) {
            handler.removeCallbacks(foregroundChecker);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
