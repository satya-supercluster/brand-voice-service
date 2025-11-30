package com.typeface.brandvoice;

import com.typeface.brandvoice.dto.*;
import com.typeface.brandvoice.model.BrandProfile;
import com.typeface.brandvoice.repository.BrandProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BrandVoiceServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BrandProfileRepository brandProfileRepository;

    @BeforeEach
    void setUp() {
        brandProfileRepository.deleteAll();
    }

    @Test
    void testCreateBrandProfile_Success() {
        // Arrange
        CreateBrandProfileRequest request = CreateBrandProfileRequest.builder()
                .customerId("customer-123")
                .brandName("Tech Innovators Inc")
                .sampleContent("We are a professional enterprise technology company " +
                        "focused on delivering innovative solutions. Our strategic " +
                        "approach ensures optimal results for our clients. We believe " +
                        "in excellence, efficiency, and partnership.")
                .build();

        // Act
        ResponseEntity<BrandProfileResponse> response = restTemplate.postForEntity(
                "/api/v1/brand-voice/profiles",
                request,
                BrandProfileResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCustomerId()).isEqualTo("customer-123");
        assertThat(response.getBody().getBrandName()).isEqualTo("Tech Innovators Inc");
        assertThat(response.getBody().getVoiceAttributes()).isNotEmpty();
        assertThat(response.getBody().getStatus()).isEqualTo("active");
    }

    @Test
    void testCreateBrandProfile_DuplicateCustomer_Conflict() {
        // Arrange
        CreateBrandProfileRequest request = CreateBrandProfileRequest.builder()
                .customerId("customer-456")
                .brandName("Test Brand")
                .sampleContent("Sample content for testing purposes. This is a " +
                        "professional brand with innovative solutions.")
                .build();

        // Create first profile
        restTemplate.postForEntity("/api/v1/brand-voice/profiles", request,
                BrandProfileResponse.class);

        // Act - Try to create duplicate
        ResponseEntity<BrandProfileResponse> response = restTemplate.postForEntity(
                "/api/v1/brand-voice/profiles",
                request,
                BrandProfileResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void testGetBrandProfile_Success() {
        // Arrange
        String customerId = "customer-789";
        CreateBrandProfileRequest request = CreateBrandProfileRequest.builder()
                .customerId(customerId)
                .brandName("Get Test Brand")
                .sampleContent("Professional content for testing retrieval functionality.")
                .build();

        restTemplate.postForEntity("/api/v1/brand-voice/profiles", request,
                BrandProfileResponse.class);

        // Act
        ResponseEntity<BrandProfileResponse> response = restTemplate.getForEntity(
                "/api/v1/brand-voice/profiles/" + customerId,
                BrandProfileResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCustomerId()).isEqualTo(customerId);
    }

    @Test
    void testGetBrandProfile_NotFound() {
        // Act
        ResponseEntity<BrandProfileResponse> response = restTemplate.getForEntity(
                "/api/v1/brand-voice/profiles/non-existent",
                BrandProfileResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testValidateContent_OnBrand() {
        // Arrange - Create profile first
        String customerId = "customer-validate-1";
        CreateBrandProfileRequest profileRequest = CreateBrandProfileRequest.builder()
                .customerId(customerId)
                .brandName("Professional Corp")
                .sampleContent("We are a professional enterprise organization " +
                        "delivering innovative solutions with strategic excellence.")
                .build();

        restTemplate.postForEntity("/api/v1/brand-voice/profiles", profileRequest,
                BrandProfileResponse.class);

        // Act - Validate similar content
        ValidateContentRequest validateRequest = ValidateContentRequest.builder()
                .customerId(customerId)
                .content("Our professional team delivers innovative enterprise solutions " +
                        "with strategic focus and excellence.")
                .contentType("email")
                .build();

        ResponseEntity<ContentValidationResponse> response = restTemplate.postForEntity(
                "/api/v1/brand-voice/validate",
                validateRequest,
                ContentValidationResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getConsistencyScore()).isGreaterThan(70.0);
        assertThat(response.getBody().getVerdict()).isIn("on_brand", "minor_issues");
        assertThat(response.getBody().getProcessingTimeMs()).isLessThan(1000L);
    }

    @Test
    void testValidateContent_OffBrand() {
        // Arrange - Create professional profile
        String customerId = "customer-validate-2";
        CreateBrandProfileRequest profileRequest = CreateBrandProfileRequest.builder()
                .customerId(customerId)
                .brandName("Formal Business")
                .sampleContent("We provide professional enterprise solutions with " +
                        "strategic innovation and excellence in service delivery.")
                .build();

        restTemplate.postForEntity("/api/v1/brand-voice/profiles", profileRequest,
                BrandProfileResponse.class);

        // Act - Validate very casual content
        ValidateContentRequest validateRequest = ValidateContentRequest.builder()
                .customerId(customerId)
                .content("Hey folks! Check out our awesome cool stuff! " +
                        "It's gonna be super amazing!")
                .contentType("social")
                .build();

        ResponseEntity<ContentValidationResponse> response = restTemplate.postForEntity(
                "/api/v1/brand-voice/validate",
                validateRequest,
                ContentValidationResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getConsistencyScore()).isLessThan(80.0);
        assertThat(response.getBody().getIssues()).isNotEmpty();
    }

    @Test
    void testDeleteBrandProfile_Success() {
        // Arrange
        String customerId = "customer-delete";
        CreateBrandProfileRequest request = CreateBrandProfileRequest.builder()
                .customerId(customerId)
                .brandName("Delete Test")
                .sampleContent("Content for deletion testing purposes.")
                .build();

        restTemplate.postForEntity("/api/v1/brand-voice/profiles", request,
                BrandProfileResponse.class);

        // Act
        restTemplate.delete("/api/v1/brand-voice/profiles/" + customerId);

        // Assert - Try to get deleted profile
        ResponseEntity<BrandProfileResponse> getResponse = restTemplate.getForEntity(
                "/api/v1/brand-voice/profiles/" + customerId,
                BrandProfileResponse.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testValidateContent_ProfileNotFound() {
        // Act
        ValidateContentRequest request = ValidateContentRequest.builder()
                .customerId("non-existent")
                .content("Some content")
                .build();

        ResponseEntity<ContentValidationResponse> response = restTemplate.postForEntity(
                "/api/v1/brand-voice/validate",
                request,
                ContentValidationResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testCreateProfile_InvalidRequest_BadRequest() {
        // Act - Missing required fields
        CreateBrandProfileRequest request = CreateBrandProfileRequest.builder()
                .customerId("") // Invalid
                .brandName("Test")
                .sampleContent("Short") // Too short
                .build();

        ResponseEntity<BrandProfileResponse> response = restTemplate.postForEntity(
                "/api/v1/brand-voice/profiles",
                request,
                BrandProfileResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}