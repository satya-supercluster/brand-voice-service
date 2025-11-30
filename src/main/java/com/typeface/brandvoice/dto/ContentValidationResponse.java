package com.typeface.brandvoice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentValidationResponse {
    private String customerId;
    private Double consistencyScore; // 0-100
    private String verdict; // "on_brand", "minor_issues", "off_brand"
    private List<ValidationIssue> issues;
    private Map<String, Double> detailedScores;
    private Long processingTimeMs;
}