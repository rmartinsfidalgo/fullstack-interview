package com.cup.alertsimulator;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Component
class NotificationSimulator {

    private static final Logger log = LoggerFactory.getLogger(NotificationSimulator.class);

    private static final String QUEUE    = "alert.notifications";
    private static final String DESCRIPTION = "Detected by automated monitoring. Requires operator review.";

    private static final List<NotificationPayload> NOTIFICATIONS = List.of(
            new NotificationPayload("High CPU usage spike detected on eu-west-1",           DESCRIPTION, "HIGH"),
            new NotificationPayload("WebSocket connections dropping on ccsdk-chat-server",  DESCRIPTION, "CRITICAL"),
            new NotificationPayload("S3 bucket access denied - possible misconfiguration",  DESCRIPTION, "MEDIUM"),
            new NotificationPayload("Deployment rollback triggered on recipientmanager",    DESCRIPTION, "HIGH"),
            new NotificationPayload("SQS queue depth crossing critical threshold",          DESCRIPTION, "CRITICAL"),
            new NotificationPayload("Disk I/O saturation on MongoDB replica node",          DESCRIPTION, "HIGH"),
            new NotificationPayload("TLS handshake failures increasing on load balancer",   DESCRIPTION, "MEDIUM"),
            new NotificationPayload("Stale pod not responding to health checks",            DESCRIPTION, "LOW"),
            new NotificationPayload("Connection pool exhausted on recipientmanager-server", DESCRIPTION, "HIGH"),
            new NotificationPayload("Kafka consumer lag growing on notifications topic",    DESCRIPTION, "MEDIUM")
    );

    private final JmsTemplate  jmsTemplate;
    private final ObjectMapper objectMapper;

    NotificationSimulator(JmsTemplate jmsTemplate, ObjectMapper objectMapper) {
        this.jmsTemplate  = jmsTemplate;
        this.objectMapper = objectMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    void start() {
        log.info("Alert Simulator broker listening on tcp://0.0.0.0:61616");
        log.info("Publishing to queue: {}", QUEUE);
        log.info("Firing every 15-45 seconds - Ctrl+C to stop");

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            push(executor); // fire immediately

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextLong(15_000, 45_001));
                    push(executor);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void push(Executor executor) {
        executor.execute(() -> {
            var payload = pick();
            try {
                var json = objectMapper.writeValueAsString(payload);
                jmsTemplate.send(QUEUE, session -> session.createTextMessage(json));
                log.info("→ [{}] {}", payload.severity(), payload.title());
            } catch (JacksonException e) {
                log.error("Failed to serialize payload", e);
            }
        });
    }

    private static NotificationPayload pick() {
        return NOTIFICATIONS.get(ThreadLocalRandom.current().nextInt(NOTIFICATIONS.size()));
    }
}
