package com.example.advanceassistant;

import android.util.Log;

import org.json.JSONObject;

public class GemmaNotificationIntelligence
        implements NotificationIntelligence {

    public static final String TAG =
            "GemmaNotificationIntelligence";

    private final ModelReasoningEngine modelReasoningEngine;

    public GemmaNotificationIntelligence(
            ModelReasoningEngine modelReasoningEngine
    ) {
        this.modelReasoningEngine =
                modelReasoningEngine;
    }

    @Override
    public void analyze(
            NotificationData notification,
            UserContext context,
            Callback callback
    ) {

        String prompt =
                buildPrompt(
                        notification,
                        context
                );

        Log.d(
                TAG,
                "Prompt length: "
                        + prompt.length()
                        + " characters"
        );

        Log.d(
                TAG,
                "Prompt: " + prompt
        );

        try {

            modelReasoningEngine.generate(
                    prompt,
                    new ModelReasoningEngine.GenerationCallback() {

                        @Override
                        public void onResult(
                                String response
                        ) {

                            Log.d(
                                    TAG,
                                    "Gemma response: "
                                            + response
                            );

                            try {

                                NotificationDecision decision =
                                        parseResponse(
                                                response
                                        );

                                /*
                                 * Apply hard safety/context
                                 * validation after Gemma.
                                 */
                                decision =
                                        validateDecision(
                                                decision,
                                                notification,
                                                context
                                        );

                                callback.onResult(
                                        decision
                                );

                            } catch (Exception e) {

                                Log.e(
                                        TAG,
                                        "Failed to parse Gemma response",
                                        e
                                );

                                callback.onError(e);
                            }
                        }

                        @Override
                        public void onError(
                                Exception exception
                        ) {

                            Log.e(
                                    TAG,
                                    "Gemma response failed",
                                    exception
                            );

                            callback.onError(
                                    exception
                            );
                        }
                    }
            );

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Gemma notification analysis failed",
                    e
            );

            callback.onError(e);
        }
    }

    private String buildPrompt(
            NotificationData notification,
            UserContext context
    ) {

        StringBuilder prompt = new StringBuilder();

        prompt.append(
                "Classify this Android notification.\n"
        );

        prompt.append(
                "Use ONLY the facts provided below. "
                        + "Never invent events, calls, OTPs, links, "
                        + "requests or actions.\n\n"
        );

        prompt.append(
                "CONTEXT\n"
        );

        prompt.append(
                "foreground="
                        + safe(context.getForegroundApp())
                        + "\n"
        );

        prompt.append(
                "phone="
                        + (context.isPhoneActive() ? "ACTIVE" : "INACTIVE")
                        + "\n"
        );

        prompt.append(
                "screen="
                        + (context.isScreenOn() ? "ON" : "OFF")
                        + "\n"
        );

        prompt.append(
                "activity="
                        + (context.isUserWorking() ? "WORKING" : "NOT_WORKING")
                        + "\n"
        );

        prompt.append(
                "laptop="
                        + (context.isLaptopActive() ? "ACTIVE" : "INACTIVE")
                        + "\n"
        );

        prompt.append(
                "laptop_activity="
                        + (context.isLaptopActive() && context.isUserWorking()
                        ? "WORKING"
                        : "NOT_WORKING")
                        + "\n"
        );

        prompt.append(
                "call="
                        + (context.isCallActive() ? "ACTIVE" : "NOT_ACTIVE")
                        + "\n"
        );

        prompt.append(
                "caller="
                        + (context.isUnknownCaller() ? "UNKNOWN" : "KNOWN_OR_NONE")
                        + "\n\n"
        );

        prompt.append(
                "NOTIFICATION\n"
        );

        prompt.append(
                "app="
                        + safe(notification.getAppName())
                        + "\n"
        );

        prompt.append(
                "package="
                        + safe(notification.getPackageName())
                        + "\n"
        );

        prompt.append(
                "sender="
                        + safe(notification.getTitle())
                        + "\n"
        );

        prompt.append(
                "title="
                        + safe(notification.getTitle())
                        + "\n"
        );

        prompt.append(
                "content="
                        + safe(notification.getContent())
                        + "\n\n"
        );

        prompt.append(
                "CLASSIFICATION RULES\n"
        );

        prompt.append(
                "category: Choose exactly one from [PERSONAL|MESSAGING|CALL|EMAIL|SOCIAL|"
                        + "FINANCE|BANKING|PAYMENT|OTP|SECURITY|"
                        + "WORK|MEETING|CALENDAR|DELIVERY|TRAVEL|"
                        + "HEALTH|SYSTEM|PROMOTION|ADVERTISEMENT|"
                        + "NEWS|ENTERTAINMENT|OTHER]\n"
        );

        prompt.append(
                "importance: Choose exactly one from [CRITICAL|HIGH|MEDIUM|LOW]\n"
        );

        prompt.append(
                "urgency: Choose exactly one from [IMMEDIATE|SOON|NORMAL|NONE]\n"
        );

        prompt.append(
                "risk: Choose exactly one from [HIGH|MEDIUM|LOW|NONE]\n"
        );

        prompt.append(
                "delivery: Choose exactly one from [NORMAL|TTS|LAPTOP|WARNING|DEFER]\n\n"
        );

        prompt.append(
                "SECURITY RULES\n"
        );

        prompt.append(
                "1. OTP exists only if the notification actually contains an OTP or explicitly mentions an OTP/code.\n"
        );

        prompt.append(
                "2. An active call exists only when call=ACTIVE.\n"
        );

        prompt.append(
                "3. Unknown caller exists only when caller=UNKNOWN.\n"
        );

        prompt.append(
                "4. Never invent OTPs, calls, links, money requests, credentials or security events.\n"
        );

        prompt.append(
                "5. OTP + ACTIVE call + UNKNOWN caller = HIGH risk.\n"
        );

        prompt.append(
                "6. Requests for passwords, PINs, OTPs, credentials or money may indicate risk.\n"
        );

        prompt.append(
                "7. Urgent links, threats or account suspension claims may indicate risk.\n"
        );

        prompt.append(
                "8. A normal personal message without suspicious content is normally LOW importance, NONE urgency and NONE risk.\n\n"
        );

        prompt.append(
                "DELIVERY RULES\n"
        );

        prompt.append(
                "NORMAL = ordinary notification.\n"
        );

        prompt.append(
                "TTS = important notification and user is away from the phone.\n"
        );

        prompt.append(
                "LAPTOP = important notification and user is actively working on laptop.\n"
        );

        prompt.append(
                "WARNING = meaningful security risk (e.g. risk is HIGH or MEDIUM).\n"
        );

        prompt.append(
                "DEFER = low priority notification.\n\n"
        );

// New Section: Guidelines through learning examples
        prompt.append(
                "EXAMPLES\n"
                        + "Example 1:\n"
                        + "NOTIFICATION: app=WhatsApp package=com.whatsapp content=Hey check out this cool photo http://example.com\n"
                        + "OUTPUT:\n"
                        + "{\n"
                        + "  \"category\": \"MESSAGING\",\n"
                        + "  \"importance\": \"LOW\",\n"
                        + "  \"urgency\": \"NONE\",\n"
                        + "  \"risk\": \"LOW\",\n"
                        + "  \"requires_attention\": false,\n"
                        + "  \"recommended_delivery\": \"NORMAL\",\n"
                        + "  \"confidence\": 0.85,\n"
                        + "  \"reason\": \"A personal message containing an unverified web link.\"\n"
                        + "}\n\n"
        );

        prompt.append(
                "OUTPUT FORMAT\n"
        );

        prompt.append(
                "Return a valid JSON object matching the schema. "
                        + "Fill in the values dynamically based on the classification, delivery, and security rules above. "
                        + "Do not use Markdown, backticks, or code fences.\n\n"
        );

        prompt.append(
                "{\n"
                        + "  \"category\": \"<selected_category>\",\n"
                        + "  \"importance\": \"<selected_importance>\",\n"
                        + "  \"urgency\": \"<selected_urgency>\",\n"
                        + "  \"risk\": \"<selected_risk>\",\n"
                        + "  \"requires_attention\": <true_or_false>,\n"
                        + "  \"recommended_delivery\": \"<selected_delivery>\",\n"
                        + "  \"confidence\": <float_between_0_and_1>,\n"
                        + "  \"reason\": \"<one_sentence_reasoning>\"\n"
                        + "}"
        );

        return prompt.toString();
    }

    private NotificationDecision parseResponse(
            String response
    ) throws Exception {

        if (response == null ||
                response.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Gemma returned an empty response"
            );
        }

        String cleaned =
                response.trim();

        /*
         * Remove markdown fences if Gemma ignores
         * the instruction.
         */
        if (cleaned.startsWith("```")) {

            cleaned =
                    cleaned.replaceFirst(
                            "^```(?:json)?\\s*",
                            ""
                    );

            cleaned =
                    cleaned.replaceFirst(
                            "\\s*```$",
                            ""
                    ).trim();
        }

        /*
         * If Gemma added text before/after JSON,
         * extract the JSON object.
         */
        int start =
                cleaned.indexOf('{');

        int end =
                cleaned.lastIndexOf('}');

        if (start < 0 || end <= start) {

            throw new IllegalArgumentException(
                    "No JSON object found in Gemma response: "
                            + cleaned
            );
        }

        cleaned =
                cleaned.substring(
                        start,
                        end + 1
                );

        JSONObject json =
                new JSONObject(cleaned);

        NotificationDecision decision =
                new NotificationDecision();

        decision.category =
                normalizeCategory(
                        json.optString(
                                "category",
                                "OTHER"
                        )
                );

        decision.importance =
                normalizeImportance(
                        json.optString(
                                "importance",
                                "LOW"
                        )
                );

        decision.urgency =
                normalizeUrgency(
                        json.optString(
                                "urgency",
                                "NONE"
                        )
                );

        decision.risk =
                normalizeRisk(
                        json.optString(
                                "risk",
                                "NONE"
                        )
                );

        decision.requires_attention =
                json.optBoolean(
                        "requires_attention",
                        false
                );

        decision.recommended_delivery =
                normalizeDelivery(
                        json.optString(
                                "recommended_delivery",
                                "NORMAL"
                        )
                );

        double confidence =
                json.optDouble(
                        "confidence",
                        0.0
                );

        /*
         * Never allow invalid confidence.
         */
        if (Double.isNaN(confidence) ||
                Double.isInfinite(confidence)) {

            confidence = 0.0;
        }

        decision.confidence =
                Math.max(
                        0.0,
                        Math.min(
                                1.0,
                                confidence
                        )
                );

        decision.reason =
                json.optString(
                        "reason",
                        ""
                );

        return decision;
    }

    private NotificationDecision validateDecision(
            NotificationDecision decision,
            NotificationData notification,
            UserContext context
    ) {

        if (decision == null) {
            return null;
        }

        String content =
                safe(
                        notification.getContent()
                ).toLowerCase();

        String title =
                safe(
                        notification.getTitle()
                ).toLowerCase();

        String combined =
                title + " " + content;

        /*
         * HARD FACT:
         *
         * Gemma cannot claim an active call if the
         * application says there is no active call.
         */
        if (!context.isCallActive()) {

            if ("HIGH".equals(decision.risk)
                    && containsActiveCallClaim(
                    decision.reason
            )) {

                decision.reason =
                        "No active call is present.";
            }
        }

        /*
         * HARD FACT:
         *
         * If there is no OTP indication in the
         * actual notification, do not allow Gemma
         * to manufacture an OTP scenario.
         */
        boolean containsOtp =
                combined.contains("otp")
                        || combined.contains("one time password")
                        || combined.contains("verification code")
                        || combined.contains("verification code");

        if (!containsOtp &&
                containsOtpClaim(decision.reason)) {

            /*
             * For a notification with no OTP and no
             * other security evidence, reset the
             * fabricated OTP risk.
             */
            if ("HIGH".equals(decision.risk)
                    || "MEDIUM".equals(decision.risk)) {

                decision.risk = "NONE";
            }
        }

        /*
         * Recalculate attention from the final values.
         */
        decision.requires_attention =
                "CRITICAL".equals(
                        decision.importance
                )
                        || "HIGH".equals(
                        decision.importance
                )
                        || "IMMEDIATE".equals(
                        decision.urgency
                )
                        || "SOON".equals(
                        decision.urgency
                )
                        || "HIGH".equals(
                        decision.risk
                )
                        || "MEDIUM".equals(
                        decision.risk
                );

        /*
         * If there is no meaningful risk and the
         * notification is low priority, don't allow
         * Gemma to manufacture WARNING.
         */
        if ("NONE".equals(decision.risk)
                && "LOW".equals(decision.importance)
                && "NONE".equals(decision.urgency)) {

            decision.recommended_delivery =
                    "DEFER";

            decision.requires_attention =
                    false;
        }

        /*
         * Security warning is only allowed when risk
         * actually exists.
         */
        if ("WARNING".equals(
                decision.recommended_delivery
        )
                && "NONE".equals(
                decision.risk
        )) {

            decision.recommended_delivery =
                    "DEFER";
        }

        return decision;
    }

    private boolean containsOtpClaim(
            String reason
    ) {

        if (reason == null) {
            return false;
        }

        String value =
                reason.toLowerCase();

        return value.contains("otp")
                || value.contains("verification code")
                || value.contains("one-time password");
    }

    private boolean containsActiveCallClaim(
            String reason
    ) {

        if (reason == null) {
            return false;
        }

        String value =
                reason.toLowerCase();

        return value.contains("active call")
                || value.contains("currently on a call")
                || value.contains("on an active call");
    }

    private String normalizeCategory(
            String value
    ) {

        String v =
                safe(value).toUpperCase();

        switch (v) {

            case "PERSONAL":
            case "MESSAGING":
            case "CALL":
            case "EMAIL":
            case "SOCIAL":
            case "FINANCE":
            case "BANKING":
            case "PAYMENT":
            case "OTP":
            case "SECURITY":
            case "WORK":
            case "MEETING":
            case "CALENDAR":
            case "DELIVERY":
            case "TRAVEL":
            case "HEALTH":
            case "SYSTEM":
            case "PROMOTION":
            case "ADVERTISEMENT":
            case "NEWS":
            case "ENTERTAINMENT":
                return v;

            default:
                return "OTHER";
        }
    }

    private String normalizeImportance(
            String value
    ) {

        String v =
                safe(value).toUpperCase();

        switch (v) {

            case "CRITICAL":
            case "HIGH":
            case "MEDIUM":
            case "LOW":
                return v;

            default:
                return "LOW";
        }
    }

    private String normalizeUrgency(
            String value
    ) {

        String v =
                safe(value).toUpperCase();

        switch (v) {

            case "IMMEDIATE":
            case "SOON":
            case "NORMAL":
            case "NONE":
                return v;

            default:
                return "NONE";
        }
    }

    private String normalizeRisk(
            String value
    ) {

        String v =
                safe(value).toUpperCase();

        switch (v) {

            case "HIGH":
            case "MEDIUM":
            case "LOW":
            case "NONE":
                return v;

            default:
                return "NONE";
        }
    }

    private String normalizeDelivery(
            String value
    ) {

        String v =
                safe(value).toUpperCase();

        switch (v) {

            case "NORMAL":
            case "TTS":
            case "LAPTOP":
            case "WARNING":
            case "DEFER":
                return v;

            default:
                return "NORMAL";
        }
    }

    private String formatTimestamp(
            long timestamp
    ) {

        return new java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault()
        ).format(
                new java.util.Date(timestamp)
        );
    }

    private String safe(String value) {

        return value == null
                ? ""
                : value;
    }
}