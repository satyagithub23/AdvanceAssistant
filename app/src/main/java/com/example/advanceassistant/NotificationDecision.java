package com.example.advanceassistant;

public class NotificationDecision {
    public String category;
    public String importance;
    public String urgency;
    public String risk;
    public boolean requires_attention;
    public String recommended_delivery;
    public double confidence;
    public String reason;

    @Override
    public String toString() {
        return "NotificationDecision{" +
                "category='" + category + '\'' +
                ", importance='" + importance + '\'' +
                ", urgency='" + urgency + '\'' +
                ", risk='" + risk + '\'' +
                ", requires_attention=" + requires_attention +
                ", recommended_delivery='" + recommended_delivery + '\'' +
                ", confidence=" + confidence +
                ", reason='" + reason + '\'' +
                '}';
    }
}
