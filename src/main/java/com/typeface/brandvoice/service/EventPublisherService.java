package com.typeface.brandvoice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.typeface.brandvoice.model.BrandProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for publishing events to GCP Pub/Sub
 * These events enable downstream processing and analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherService {

    private final PubSubTemplate pubSubTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gcp.pubsub.topic.profile-events:brand-profile-events}")
    private String profileEventsTopic;

    @Value("${gcp.pubsub.topic.validation-events:content-validation-events}")
    private String validationEventsTopic;

    @Async
    public CompletableFuture<Void> publishProfileCreated(BrandProfile profile) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "PROFILE_CREATED");
            event.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            event.put("customerId", profile.getCustomerId());
            event.put("brandName", profile.getBrandName());
            event.put("profileId", profile.getId());
            event.put("confidenceScore", profile.getConfidenceScore());

            String message = objectMapper.writeValueAsString(event);

            pubSubTemplate.publish(profileEventsTopic, message);

            log.info("Published PROFILE_CREATED event for customer: {}", profile.getCustomerId());

        } catch (Exception e) {
            log.error("Failed to publish profile created event", e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> publishProfileDeleted(String customerId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "PROFILE_DELETED");
            event.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            event.put("customerId", customerId);

            String message = objectMapper.writeValueAsString(event);

            pubSubTemplate.publish(profileEventsTopic, message);

            log.info("Published PROFILE_DELETED event for customer: {}", customerId);

        } catch (Exception e) {
            log.error("Failed to publish profile deleted event", e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> publishValidationPerformed(
            String customerId,
            double consistencyScore,
            String verdict) {

        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "VALIDATION_PERFORMED");
            event.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            event.put("customerId", customerId);
            event.put("consistencyScore", consistencyScore);
            event.put("verdict", verdict);

            String message = objectMapper.writeValueAsString(event);

            pubSubTemplate.publish(validationEventsTopic, message);

            log.debug("Published VALIDATION_PERFORMED event for customer: {}", customerId);

        } catch (Exception e) {
            log.error("Failed to publish validation event", e);
        }

        return CompletableFuture.completedFuture(null);
    }
}