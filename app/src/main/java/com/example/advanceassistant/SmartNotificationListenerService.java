package com.example.advanceassistant;

import android.app.Notification;
import android.content.pm.PackageManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class SmartNotificationListenerService
        extends NotificationListenerService {

    private static final String TAG =
            "SmartNotificationListenerService";

    private ForegroundAppMonitor foregroundAppMonitor;

    private NotificationIntelligence notificationIntelligence;

    private ModelReasoningEngine modelReasoningEngine;

    @Override
    public void onCreate() {

        super.onCreate();

        Log.d(
                TAG,
                "Notification Listener Service created"
        );

        foregroundAppMonitor =
                new ForegroundAppMonitor(this);

        modelReasoningEngine =
                new ModelReasoningEngine(this);

        notificationIntelligence =
                new GemmaNotificationIntelligence(
                        modelReasoningEngine
                );

        modelReasoningEngine.initialize(
                new ModelReasoningEngine.InitializationCallback() {

                    @Override
                    public void onInitialized() {

                        Log.d(
                                TAG,
                                "Gemma notification intelligence ready"
                        );
                    }

                    @Override
                    public void onError(
                            Exception exception
                    ) {

                        Log.e(
                                TAG,
                                "Failed to initialize Gemma",
                                exception
                        );
                    }
                }
        );
    }

    @Override
    public void onNotificationPosted(
            StatusBarNotification sbn
    ) {

        super.onNotificationPosted(sbn);

        if (sbn == null) {
            return;
        }

        Notification notification =
                sbn.getNotification();

        if (notification == null) {
            return;
        }

        String packageName =
                sbn.getPackageName();

        CharSequence title =
                notification.extras.getCharSequence(
                        Notification.EXTRA_TITLE
                );

        CharSequence text =
                notification.extras.getCharSequence(
                        Notification.EXTRA_TEXT
                );

        String titleString =
                title != null
                        ? title.toString()
                        : "";

        String textString =
                text != null
                        ? text.toString()
                        : "";

        Log.d(
                TAG,
                "Notification Received: Package: "
                        + packageName
                        + " Title: "
                        + titleString
                        + " Text: "
                        + textString
        );

        /*
         * Foreground app.
         */
        String foregroundApp =
                foregroundAppMonitor
                        .getForegroundApp();

        Log.d(
                TAG,
                "Foreground app at notification time: "
                        + foregroundApp
        );

        /*
         * Notification data.
         */
        NotificationData notificationData =
                new NotificationData(
                        packageName,
                        getApplicationName(packageName),
                        titleString,
                        textString,
                        sbn.getPostTime()
                );

        /*
         * Current context.
         *
         * These are still temporary values exactly
         * as in your current implementation.
         */
        UserContext context =
                new UserContext(
                        foregroundApp,
                        true,   // phone active
                        true,   // screen on
                        false,  // laptop active
                        true,   // user working
                        false,  // call active
                        false   // unknown caller
                );

        /*
         * Make sure intelligence exists.
         */
        if (notificationIntelligence == null) {

            Log.w(
                    TAG,
                    "Notification intelligence is not initialized"
            );

            return;
        }

        notificationIntelligence.analyze(
                notificationData,
                context,
                new NotificationIntelligence.Callback() {

                    @Override
                    public void onResult(
                            NotificationDecision decision
                    ) {

                        Log.d(
                                TAG,
                                "Gemma Decision: "
                                        + decision
                        );

                        handleDecision(
                                notificationData,
                                context,
                                decision
                        );
                    }

                    @Override
                    public void onError(
                            Exception exception
                    ) {

                        Log.e(
                                TAG,
                                "Notification Intelligence error",
                                exception
                        );
                    }
                }
        );
    }

    @Override
    public void onListenerConnected() {

        super.onListenerConnected();

        Log.d(
                TAG,
                "Notification Listener CONNECTED"
        );

        /*
         * DO NOT initialize Gemma here.
         *
         * onCreate() already did it.
         */
    }

    @Override
    public void onListenerDisconnected() {

        super.onListenerDisconnected();

        Log.d(
                TAG,
                "Notification Listener DISCONNECTED"
        );
    }

    private void handleDecision(
            NotificationData notificationData,
            UserContext userContext,
            NotificationDecision decision
    ) {

        if (decision == null) {

            Log.w(
                    TAG,
                    "Received null notification decision"
            );

            return;
        }

        if (decision.recommended_delivery == null) {

            Log.w(
                    TAG,
                    "Gemma returned null delivery action"
            );

            return;
        }

        switch (
                decision.recommended_delivery
        ) {

            case "NORMAL":

                Log.d(
                        TAG,
                        "ACTION: NORMAL notification"
                );

                break;

            case "TTS":

                Log.d(
                        TAG,
                        "ACTION: TTS"
                );

                break;

            case "LAPTOP":

                Log.d(
                        TAG,
                        "ACTION: Forward to laptop"
                );

                break;

            case "WARNING":

                Log.d(
                        TAG,
                        "ACTION: Security warning"
                );

                break;

            case "DEFER":

                Log.d(
                        TAG,
                        "ACTION: Defer notification"
                );

                break;

            default:

                Log.w(
                        TAG,
                        "Unknown delivery action: "
                                + decision.recommended_delivery
                );
        }
    }

    private String getApplicationName(
            String packageName
    ) {

        try {

            PackageManager pm =
                    getPackageManager();

            return pm
                    .getApplicationLabel(
                            pm.getApplicationInfo(
                                    packageName,
                                    0
                            )
                    )
                    .toString();

        } catch (Exception e) {

            return packageName;
        }
    }

    @Override
    public void onDestroy() {

        Log.d(
                TAG,
                "Notification Listener Service destroyed"
        );

        if (modelReasoningEngine != null) {

            modelReasoningEngine.close();

            modelReasoningEngine = null;
        }

        notificationIntelligence = null;
        foregroundAppMonitor = null;

        super.onDestroy();
    }
}