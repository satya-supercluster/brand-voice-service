package com.typeface.brandvoice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

// Request to create a brand profile
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBrandProfileRequest {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Brand name is required")
    private String brandName;

    @NotBlank(message = "Sample content is required")
    @Size(min = 100, message = "Sample content should be at least 100 characters")
    private String sampleContent;
}