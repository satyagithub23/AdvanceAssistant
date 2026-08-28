package com.example.advanceassistant;

public interface NotificationIntelligence {
    void analyze(
            NotificationData notification,
            UserContext context,
            Callback callback
    );

    interface Callback {
        void onResult(NotificationDecision decision);
        void onError(Exception exception);
    }
}
