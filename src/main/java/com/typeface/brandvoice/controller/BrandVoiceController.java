package com.typeface.brandvoice.controller;

import com.typeface.brandvoice.dto.*;
import com.typeface.brandvoice.service.BrandVoiceService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/brand-voice")
@RequiredArgsConstructor
@Slf4j
public class BrandVoiceController {

    private final BrandVoiceService brandVoiceService;

    @PostMapping("/profiles")
    @Timed(value = "api.profile.create", description = "Time to create brand profile")
    public ResponseEntity<BrandProfileResponse> createBrandProfile(
            @Valid @RequestBody CreateBrandProfileRequest request) {

        log.info("Creating brand profile for customer: {}", request.getCustomerId());

        try {
            BrandProfileResponse response = brandVoiceService.createBrandProfile(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating brand profile", e);
            throw e;
        }
    }

    @GetMapping("/profiles/{customerId}")
    @Timed(value = "api.profile.get", description = "Time to get brand profile")
    public ResponseEntity<BrandProfileResponse> getBrandProfile(
            @PathVariable String customerId) {

        log.info("Fetching brand profile for customer: {}", customerId);

        BrandProfileResponse response = brandVoiceService.getBrandProfile(customerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    @Timed(value = "api.validate.content", description = "Time to validate content")
    public ResponseEntity<ContentValidationResponse> validateContent(
            @Valid @RequestBody ValidateContentRequest request) {

        log.info("Validating content for customer: {}", request.getCustomerId());

        long startTime = System.currentTimeMillis();

        ContentValidationResponse response = brandVoiceService.validateContent(request);

        long processingTime = System.currentTimeMillis() - startTime;
        response.setProcessingTimeMs(processingTime);

        log.info("Content validation completed in {}ms with score: {}",
                processingTime, response.getConsistencyScore());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/profiles/{customerId}")
    public ResponseEntity<Void> deleteBrandProfile(@PathVariable String customerId) {
        log.info("Deleting brand profile for customer: {}", customerId);
        brandVoiceService.deleteBrandProfile(customerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Brand Voice Service is healthy");
    }
}