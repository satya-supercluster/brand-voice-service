package com.typeface.brandvoice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationIssue {
    private String type; // "tone", "vocabulary", "formality"
    private String severity; // "low", "medium", "high"
    private String description;
    private String suggestion;
}