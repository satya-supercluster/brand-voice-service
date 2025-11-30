package com.typeface.brandvoice.service;

import com.typeface.brandvoice.dto.*;
import com.typeface.brandvoice.model.BrandProfile;
import com.typeface.brandvoice.repository.BrandProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandVoiceService {

    private final BrandProfileRepository brandProfileRepository;
    private final NLPAnalyzerService nlpAnalyzerService;
    private final EventPublisherService eventPublisher;

    @Transactional
    public BrandProfileResponse createBrandProfile(CreateBrandProfileRequest request) {

        // Check if profile already exists
        Optional<BrandProfile> existing = brandProfileRepository
                .findByCustomerId(request.getCustomerId());

        if (existing.isPresent()) {
            throw new IllegalStateException(
                    "Brand profile already exists for customer: " + request.getCustomerId()
            );
        }

        // Analyze sample content to extract voice attributes
        Map<String, Object> voiceAttributes = nlpAnalyzerService
                .analyzeVoiceCharacteristics(request.getSampleContent());

        // Create and save profile
        BrandProfile profile = BrandProfile.builder()
                .customerId(request.getCustomerId())
                .brandName(request.getBrandName())
                .sampleContent(request.getSampleContent())
                .voiceAttributes(voiceAttributes)
                .confidenceScore(calculateConfidenceScore(voiceAttributes))
                .build();

        profile = brandProfileRepository.save(profile);

        // Publish event for downstream systems
        eventPublisher.publishProfileCreated(profile);

        log.info("Brand profile created successfully for customer: {}",
                request.getCustomerId());

        return mapToResponse(profile);
    }

    @Cacheable(value = "brandProfiles", key = "#customerId")
    public BrandProfileResponse getBrandProfile(String customerId) {
        BrandProfile profile = brandProfileRepository
                .findByCustomerId(customerId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Brand profile not found for customer: " + customerId
                ));

        return mapToResponse(profile);
    }

    public ContentValidationResponse validateContent(ValidateContentRequest request) {

        // Get brand profile (from cache if available)
        BrandProfile profile = brandProfileRepository
                .findByCustomerId(request.getCustomerId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Brand profile not found for customer: " + request.getCustomerId()
                ));

        // Analyze the new content
        Map<String, Object> contentAttributes = nlpAnalyzerService
                .analyzeVoiceCharacteristics(request.getContent());

        // Compare with brand profile
        Map<String, Double> detailedScores = compareAttributes(
                profile.getVoiceAttributes(),
                contentAttributes
        );

        // Calculate overall consistency score
        double consistencyScore = calculateConsistencyScore(detailedScores);

        // Generate issues and suggestions
        List<ValidationIssue> issues = generateIssues(detailedScores, profile.getVoiceAttributes());

        // Determine verdict
        String verdict = determineVerdict(consistencyScore);

        // Publish validation event for analytics
        eventPublisher.publishValidationPerformed(
                request.getCustomerId(),
                consistencyScore,
                verdict
        );

        return ContentValidationResponse.builder()
                .customerId(request.getCustomerId())
                .consistencyScore(consistencyScore)
                .verdict(verdict)
                .issues(issues)
                .detailedScores(detailedScores)
                .build();
    }

    @Transactional
    public void deleteBrandProfile(String customerId) {
        BrandProfile profile = brandProfileRepository
                .findByCustomerId(customerId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Brand profile not found for customer: " + customerId
                ));

        brandProfileRepository.delete(profile);
        eventPublisher.publishProfileDeleted(customerId);

        log.info("Brand profile deleted for customer: {}", customerId);
    }

    private Map<String, Double> compareAttributes(
            Map<String, Object> brandAttributes,
            Map<String, Object> contentAttributes) {

        Map<String, Double> scores = new HashMap<>();

        // Compare tone
        scores.put("tone", compareTone(brandAttributes, contentAttributes));

        // Compare formality
        scores.put("formality", compareFormality(brandAttributes, contentAttributes));

        // Compare vocabulary complexity
        scores.put("vocabulary", compareVocabulary(brandAttributes, contentAttributes));

        // Compare sentence structure
        scores.put("sentence_structure", compareSentenceStructure(brandAttributes, contentAttributes));

        return scores;
    }

    private double compareTone(Map<String, Object> brand, Map<String, Object> content) {
        String brandTone = (String) brand.getOrDefault("tone", "neutral");
        String contentTone = (String) content.getOrDefault("tone", "neutral");
        return brandTone.equals(contentTone) ? 100.0 : 60.0;
    }

    private double compareFormality(Map<String, Object> brand, Map<String, Object> content) {
        double brandFormality = ((Number) brand.getOrDefault("formality", 0.5)).doubleValue();
        double contentFormality = ((Number) content.getOrDefault("formality", 0.5)).doubleValue();
        double diff = Math.abs(brandFormality - contentFormality);
        return Math.max(0, 100.0 - (diff * 200));
    }

    private double compareVocabulary(Map<String, Object> brand, Map<String, Object> content) {
        double brandVocab = ((Number) brand.getOrDefault("vocabulary_complexity", 0.5)).doubleValue();
        double contentVocab = ((Number) content.getOrDefault("vocabulary_complexity", 0.5)).doubleValue();
        double diff = Math.abs(brandVocab - contentVocab);
        return Math.max(0, 100.0 - (diff * 150));
    }

    private double compareSentenceStructure(Map<String, Object> brand, Map<String, Object> content) {
        String brandStructure = (String) brand.getOrDefault("sentence_length", "medium");
        String contentStructure = (String) content.getOrDefault("sentence_length", "medium");
        return brandStructure.equals(contentStructure) ? 100.0 : 70.0;
    }

    private double calculateConsistencyScore(Map<String, Double> detailedScores) {
        return detailedScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private List<ValidationIssue> generateIssues(
            Map<String, Double> scores,
            Map<String, Object> brandAttributes) {

        List<ValidationIssue> issues = new ArrayList<>();

        scores.forEach((attribute, score) -> {
            if (score < 70) {
                issues.add(ValidationIssue.builder()
                        .type(attribute)
                        .severity(score < 50 ? "high" : "medium")
                        .description(String.format("The %s doesn't match your brand voice", attribute))
                        .suggestion(getSuggestion(attribute, brandAttributes))
                        .build());
            }
        });

        return issues;
    }

    private String getSuggestion(String attribute, Map<String, Object> brandAttributes) {
        return switch (attribute) {
            case "tone" -> "Try using a more " + brandAttributes.get("tone") + " tone";
            case "formality" -> "Adjust the formality level to match your brand";
            case "vocabulary" -> "Use vocabulary that aligns with your brand complexity";
            case "sentence_structure" -> "Adjust sentence length to match your brand style";
            default -> "Review this aspect against your brand guidelines";
        };
    }

    private String determineVerdict(double score) {
        if (score >= 80) return "on_brand";
        if (score >= 60) return "minor_issues";
        return "off_brand";
    }

    private double calculateConfidenceScore(Map<String, Object> attributes) {
        // Simple confidence based on completeness of attributes
        return attributes.size() >= 4 ? 0.9 : 0.7;
    }

    private BrandProfileResponse mapToResponse(BrandProfile profile) {
        return BrandProfileResponse.builder()
                .profileId(profile.getId())
                .customerId(profile.getCustomerId())
                .brandName(profile.getBrandName())
                .voiceAttributes(profile.getVoiceAttributes())
                .confidenceScore(profile.getConfidenceScore())
                .status(profile.isActive() ? "active" : "inactive")
                .createdAt(profile.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }
}