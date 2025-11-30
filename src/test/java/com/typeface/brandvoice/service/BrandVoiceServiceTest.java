package com.typeface.brandvoice.service;

import com.typeface.brandvoice.dto.*;
import com.typeface.brandvoice.model.BrandProfile;
import com.typeface.brandvoice.repository.BrandProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandVoiceServiceTest {

    @Mock
    private BrandProfileRepository repository;

    @Mock
    private NLPAnalyzerService nlpAnalyzerService;

    @Mock
    private EventPublisherService eventPublisher;

    @InjectMocks
    private BrandVoiceService service;

    private CreateBrandProfileRequest createRequest;
    private BrandProfile mockProfile;
    private Map<String, Object> mockVoiceAttributes;

    @BeforeEach
    void setUp() {
        createRequest = CreateBrandProfileRequest.builder()
                .customerId("test-customer")
                .brandName("Test Brand")
                .sampleContent("We are a professional enterprise company.")
                .build();

        mockVoiceAttributes = new HashMap<>();
        mockVoiceAttributes.put("tone", "professional");
        mockVoiceAttributes.put("formality", 0.85);
        mockVoiceAttributes.put("vocabulary_complexity", 0.68);
        mockVoiceAttributes.put("sentence_length", "medium");

        mockProfile = BrandProfile.builder()
                .id("profile-id")
                .customerId("test-customer")
                .brandName("Test Brand")
                .voiceAttributes(mockVoiceAttributes)
                .confidenceScore(0.9)
                .active(true)
                .build();
    }

    @Test
    void createBrandProfile_Success() {
        // Arrange
        when(repository.findByCustomerId("test-customer")).thenReturn(Optional.empty());
        when(nlpAnalyzerService.analyzeVoiceCharacteristics(any())).thenReturn(mockVoiceAttributes);
        when(repository.save(any(BrandProfile.class))).thenReturn(mockProfile);

        // Act
        BrandProfileResponse response = service.createBrandProfile(createRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCustomerId()).isEqualTo("test-customer");
        assertThat(response.getBrandName()).isEqualTo("Test Brand");
        assertThat(response.getVoiceAttributes()).isNotEmpty();

        verify(repository).findByCustomerId("test-customer");
        verify(nlpAnalyzerService).analyzeVoiceCharacteristics(createRequest.getSampleContent());
        verify(repository).save(any(BrandProfile.class));
        verify(eventPublisher).publishProfileCreated(any(BrandProfile.class));
    }

    @Test
    void createBrandProfile_DuplicateCustomer_ThrowsException() {
        // Arrange
        when(repository.findByCustomerId("test-customer")).thenReturn(Optional.of(mockProfile));

        // Act & Assert
        assertThatThrownBy(() -> service.createBrandProfile(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");

        verify(repository).findByCustomerId("test-customer");
        verify(repository, never()).save(any());
    }

    @Test
    void getBrandProfile_Success() {
        // Arrange
        when(repository.findByCustomerId("test-customer")).thenReturn(Optional.of(mockProfile));

        // Act
        BrandProfileResponse response = service.getBrandProfile("test-customer");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCustomerId()).isEqualTo("test-customer");
        verify(repository).findByCustomerId("test-customer");
    }

    @Test
    void getBrandProfile_NotFound_ThrowsException() {
        // Arrange
        when(repository.findByCustomerId("non-existent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.getBrandProfile("non-existent"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("not found");

        verify(repository).findByCustomerId("non-existent");
    }

    @Test
    void validateContent_OnBrand_ReturnsHighScore() {
        // Arrange
        ValidateContentRequest request = ValidateContentRequest.builder()
                .customerId("test-customer")
                .content("Our professional team delivers innovative solutions.")
                .contentType("email")
                .build();

        Map<String, Object> contentAttributes = new HashMap<>();
        contentAttributes.put("tone", "professional");
        contentAttributes.put("formality", 0.82);
        contentAttributes.put("vocabulary_complexity", 0.65);
        contentAttributes.put("sentence_length", "medium");

        when(repository.findByCustomerId("test-customer")).thenReturn(Optional.of(mockProfile));
        when(nlpAnalyzerService.analyzeVoiceCharacteristics(any())).thenReturn(contentAttributes);

        // Act
        ContentValidationResponse response = service.validateContent(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getConsistencyScore()).isGreaterThan(70.0);
        assertThat(response.getVerdict()).isIn("on_brand", "minor_issues");
        verify(eventPublisher).publishValidationPerformed(eq("test-customer"), anyDouble(), anyString());
    }

    @Test
    void validateContent_OffBrand_ReturnsLowScoreWithIssues() {
        // Arrange
        ValidateContentRequest request = ValidateContentRequest.builder()
                .customerId("test-customer")
                .content("Hey folks! Super cool awesome stuff!")
                .contentType("social")
                .build();

        Map<String, Object> contentAttributes = new HashMap<>();
        contentAttributes.put("tone", "casual");
        contentAttributes.put("formality", 0.2);
        contentAttributes.put("vocabulary_complexity", 0.3);
        contentAttributes.put("sentence_length", "short");

        when(repository.findByCustomerId("test-customer")).thenReturn(Optional.of(mockProfile));
        when(nlpAnalyzerService.analyzeVoiceCharacteristics(any())).thenReturn(contentAttributes);

        // Act
        ContentValidationResponse response = service.validateContent(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getConsistencyScore()).isLessThan(80.0);
        assertThat(response.getIssues()).isNotEmpty();
        assertThat(response.getVerdict()).isIn("minor_issues", "off_brand");
    }

    @Test
    void validateContent_ProfileNotFound_ThrowsException() {
        // Arrange
        ValidateContentRequest request = ValidateContentRequest.builder()
                .customerId("non-existent")
                .content("Some content")
                .build();

        when(repository.findByCustomerId("non-existent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.validateContent(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void deleteBrandProfile_Success() {
        // Arrange
        when(repository.findByCustomerId("test-customer")).thenReturn(Optional.of(mockProfile));
        doNothing().when(repository).delete(any(BrandProfile.class));

        // Act
        service.deleteBrandProfile("test-customer");

        // Assert
        verify(repository).findByCustomerId("test-customer");
        verify(repository).delete(mockProfile);
        verify(eventPublisher).publishProfileDeleted("test-customer");
    }

    @Test
    void deleteBrandProfile_NotFound_ThrowsException() {
        // Arrange
        when(repository.findByCustomerId("non-existent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.deleteBrandProfile("non-existent"))
                .isInstanceOf(NoSuchElementException.class);

        verify(repository).findByCustomerId("non-existent");
        verify(repository, never()).delete(any());
    }
}