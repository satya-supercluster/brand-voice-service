package com.typeface.brandvoice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandProfileResponse {
    private String profileId;
    private String customerId;
    private String brandName;
    private Map<String, Object> voiceAttributes;
    private Double confidenceScore;
    private String status;
    private String createdAt;
}