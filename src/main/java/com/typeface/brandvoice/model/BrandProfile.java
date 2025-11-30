package com.typeface.brandvoice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "brand_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String customerId;

    @Column(nullable = false)
    private String brandName;

    // Voice characteristics stored as JSON
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> voiceAttributes;

    // Example attributes:
    // {
    //   "tone": "professional",
    //   "formality": 0.8,
    //   "vocabulary_complexity": 0.6,
    //   "sentence_length": "medium",
    //   "key_phrases": ["innovative", "customer-first"],
    //   "avoid_phrases": ["cheap", "discount"]
    // }

    @Column(columnDefinition = "text")
    private String sampleContent;

    @Column
    private Double confidenceScore;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private boolean active;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        active = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}