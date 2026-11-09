package com.cup.alertsimulator;

/**
 * Payload posted to the candidate's backend.
 */
public record NotificationPayload(
        String title,
        String description,
        String severity
) {}
