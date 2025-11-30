package com.typeface.brandvoice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Service that interfaces with Python NLP analyzer for text analysis
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NLPAnalyzerService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${nlp.analyzer.url:http://localhost:8001}")
    private String nlpAnalyzerUrl;

    /**
     * Analyzes text to extract voice characteristics
     * Calls Python service for advanced NLP processing
     */
    public Map<String, Object> analyzeVoiceCharacteristics(String text) {

        log.debug("Analyzing voice characteristics for text of length: {}", text.length());

        try {
            Map<String, String> request = new HashMap<>();
            request.put("text", text);

            // Call Python NLP service
            Map<String, Object> response = webClient
                    .post()
                    .uri(nlpAnalyzerUrl + "/analyze")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(java.time.Duration.ofSeconds(5))
                    .onErrorResume(this::handleAnalysisError)
                    .block();

            log.debug("Voice analysis completed successfully");
            return response;

        } catch (Exception e) {
            log.error("Error analyzing voice characteristics", e);
            // Return fallback analysis
            return getFallbackAnalysis(text);
        }
    }

    private Mono<Map<String, Object>> handleAnalysisError(Throwable error) {
        log.warn("NLP analyzer service error, using fallback: {}", error.getMessage());
        return Mono.just(new HashMap<>());
    }

    /**
     * Fallback analysis using basic heuristics when Python service is unavailable
     */
    private Map<String, Object> getFallbackAnalysis(String text) {
        Map<String, Object> analysis = new HashMap<>();

        // Basic tone detection
        String tone = detectBasicTone(text);
        analysis.put("tone", tone);

        // Formality score (0-1)
        double formality = calculateFormality(text);
        analysis.put("formality", formality);

        // Vocabulary complexity (0-1)
        double complexity = calculateVocabularyComplexity(text);
        analysis.put("vocabulary_complexity", complexity);

        // Sentence length category
        String sentenceLength = categorizeSentenceLength(text);
        analysis.put("sentence_length", sentenceLength);

        // Average sentence length
        analysis.put("avg_sentence_length", calculateAvgSentenceLength(text));

        return analysis;
    }

    private String detectBasicTone(String text) {
        String lowerText = text.toLowerCase();

        // Simple keyword-based detection
        if (lowerText.contains("exciting") || lowerText.contains("amazing") ||
                lowerText.contains("wonderful")) {
            return "enthusiastic";
        } else if (lowerText.contains("professional") || lowerText.contains("enterprise")) {
            return "professional";
        } else if (lowerText.contains("friend") || lowerText.contains("hey")) {
            return "casual";
        }

        return "neutral";
    }

    private double calculateFormality(String text) {
        int formalIndicators = 0;
        int informalIndicators = 0;

        String lowerText = text.toLowerCase();

        // Formal indicators
        String[] formalWords = {"therefore", "consequently", "furthermore", "moreover"};
        for (String word : formalWords) {
            if (lowerText.contains(word)) formalIndicators++;
        }

        // Informal indicators
        String[] informalWords = {"hey", "yeah", "cool", "awesome", "gonna"};
        for (String word : informalWords) {
            if (lowerText.contains(word)) informalIndicators++;
        }

        // Check for contractions (informal)
        if (text.contains("'")) informalIndicators++;

        int total = formalIndicators + informalIndicators;
        if (total == 0) return 0.5; // neutral

        return (double) formalIndicators / total;
    }

    private double calculateVocabularyComplexity(String text) {
        String[] words = text.split("\\s+");

        int complexWords = 0;
        for (String word : words) {
            // Words longer than 8 characters considered complex
            if (word.length() > 8) {
                complexWords++;
            }
        }

        return words.length > 0 ? (double) complexWords / words.length : 0.0;
    }

    private String categorizeSentenceLength(String text) {
        double avgLength = calculateAvgSentenceLength(text);

        if (avgLength < 10) return "short";
        if (avgLength < 20) return "medium";
        return "long";
    }

    private double calculateAvgSentenceLength(String text) {
        String[] sentences = text.split("[.!?]+");
        if (sentences.length == 0) return 0;

        int totalWords = 0;
        for (String sentence : sentences) {
            totalWords += sentence.trim().split("\\s+").length;
        }

        return (double) totalWords / sentences.length;
    }
}