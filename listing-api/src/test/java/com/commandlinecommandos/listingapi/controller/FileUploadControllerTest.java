package com.commandlinecommandos.listingapi.controller;

import com.commandlinecommandos.listingapi.model.*;
import com.commandlinecommandos.listingapi.service.FileStorageService;
import com.commandlinecommandos.listingapi.service.ListingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadControllerTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ListingService listingService;

    @InjectMocks
    private FileUploadController fileUploadController;

    private Listing testListing;
    private ListingImage testListingImage;
    private MockMultipartFile testFile;
    private List<MultipartFile> testFiles;

    @BeforeEach
    void setUp() {
        // Create test listing
        testListing = new Listing(
            "Test Item",
            "Test Description",
            new BigDecimal("99.99"),
            Category.ELECTRONICS,
            ItemCondition.NEW,
            "Test Location",
            1L
        );
        testListing.setListingId(1L);

        // Create test listing image
        testListingImage = new ListingImage(
            testListing.getListingId(),
            "/path/to/test-image.jpg",
            "test-image.jpg",
            1
        );
        testListingImage.setImageId(1L);

        // Create test file
        testFile = new MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        // Create test files list
        testFiles = Arrays.asList(
            (MultipartFile) new MockMultipartFile("files", "test-image1.jpg", "image/jpeg", "test image 1 content".getBytes()),
            (MultipartFile) new MockMultipartFile("files", "test-image2.jpg", "image/jpeg", "test image 2 content".getBytes())
        );
    }

    // Tests for uploadFile method
    @Test
    void uploadFile_Success_ReturnsOkResponse() {
        // Arrange
        when(listingService.getListingById(1L)).thenReturn(testListing);
        when(fileStorageService.storeFile(any(MultipartFile.class), any(Listing.class), anyInt()))
            .thenReturn(testListingImage);

        // Act
        ResponseEntity<String> response = fileUploadController.uploadFile(1L, testFile, 1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("File uploaded successfully", response.getBody());
        verify(listingService).getListingById(1L);
        verify(fileStorageService).storeFile(eq(testFile), eq(testListing), eq(1));
    }

    @Test
    void uploadFile_ListingNotFound_ReturnsNotFound() {
        // Arrange
        when(listingService.getListingById(1L)).thenReturn(null);

        // Act
        ResponseEntity<String> response = fileUploadController.uploadFile(1L, testFile, 1);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(listingService).getListingById(1L);
        verify(fileStorageService, never()).storeFile(any(), any(), anyInt());
    }

    @Test
    void uploadFile_ServiceThrowsException_ReturnsInternalServerError() {
        // Arrange
        when(listingService.getListingById(1L)).thenReturn(testListing);
        when(fileStorageService.storeFile(any(MultipartFile.class), any(Listing.class), anyInt()))
            .thenThrow(new RuntimeException("Storage error"));

        // Act
        ResponseEntity<String> response = fileUploadController.uploadFile(1L, testFile, 1);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to upload file", response.getBody());
        verify(listingService).getListingById(1L);
        verify(fileStorageService).storeFile(eq(testFile), eq(testListing), eq(1));
    }

    // Tests for uploadMultipleFiles method
    @Test
    void uploadMultipleFiles_Success_ReturnsOkResponse() {
        // Arrange
        int[] displayOrders = {1, 2};
        List<ListingImage> expectedImages = Arrays.asList(testListingImage, testListingImage);
        
        when(listingService.getListingById(1L)).thenReturn(testListing);
        when(fileStorageService.storeFiles(anyList(), any(Listing.class), any(int[].class)))
            .thenReturn(expectedImages);

        // Act
        ResponseEntity<String> response = fileUploadController.uploadMultipleFiles(1L, testFiles, displayOrders);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Files uploaded successfully", response.getBody());
        verify(listingService).getListingById(1L);
        verify(fileStorageService).storeFiles(eq(testFiles), eq(testListing), eq(displayOrders));
    }

    @Test
    void uploadMultipleFiles_EmptyFiles_ReturnsBadRequest() {
        // Arrange
        List<MultipartFile> emptyFiles = new ArrayList<>();
        int[] displayOrders = {};

        // Act
        ResponseEntity<String> response = fileUploadController.uploadMultipleFiles(1L, emptyFiles, displayOrders);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No files provided", response.getBody());
        // Note: listingService.getListingById() should NOT be called when files list is empty
        verify(listingService, never()).getListingById(any());
        verify(fileStorageService, never()).storeFiles(any(), any(), any());
    }

    @Test
    void uploadMultipleFiles_ListingNotFound_ReturnsNotFound() {
        // Arrange
        int[] displayOrders = {1, 2};
        when(listingService.getListingById(1L)).thenReturn(null);

        // Act
        ResponseEntity<String> response = fileUploadController.uploadMultipleFiles(1L, testFiles, displayOrders);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(listingService).getListingById(1L);
        verify(fileStorageService, never()).storeFiles(any(), any(), any());
    }

    @Test
    void uploadMultipleFiles_ServiceThrowsException_ReturnsInternalServerError() {
        // Arrange
        int[] displayOrders = {1, 2};
        when(listingService.getListingById(1L)).thenReturn(testListing);
        when(fileStorageService.storeFiles(anyList(), any(Listing.class), any(int[].class)))
            .thenThrow(new RuntimeException("Storage error"));

        // Act
        ResponseEntity<String> response = fileUploadController.uploadMultipleFiles(1L, testFiles, displayOrders);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to upload files", response.getBody());
        verify(listingService).getListingById(1L);
        verify(fileStorageService).storeFiles(eq(testFiles), eq(testListing), eq(displayOrders));
    }

    // Tests for getListingImages method
    @Test
    void getListingImages_Success_ReturnsImages() {
        // Arrange
        List<ListingImage> expectedImages = Arrays.asList(testListingImage);
        when(listingService.getListingById(1L)).thenReturn(testListing);
        when(fileStorageService.getImagesByListing(testListing)).thenReturn(expectedImages);

        // Act
        ResponseEntity<?> response = fileUploadController.getListingImages(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedImages, response.getBody());
        verify(listingService).getListingById(1L);
        verify(fileStorageService).getImagesByListing(testListing);
    }

    @Test
    void getListingImages_ListingNotFound_ReturnsNotFound() {
        // Arrange
        when(listingService.getListingById(1L)).thenReturn(null);

        // Act
        ResponseEntity<?> response = fileUploadController.getListingImages(1L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(listingService).getListingById(1L);
        verify(fileStorageService, never()).getImagesByListing(any());
    }

    @Test
    void getListingImages_ServiceThrowsException_ReturnsInternalServerError() {
        // Arrange
        when(listingService.getListingById(1L)).thenReturn(testListing);
        when(fileStorageService.getImagesByListing(testListing))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = fileUploadController.getListingImages(1L);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Failed to get listing images"));
        verify(listingService).getListingById(1L);
        verify(fileStorageService).getImagesByListing(testListing);
    }

    // Tests for deleteListingImage method
    @Test
    void deleteListingImage_Success_ReturnsOkResponse() {
        // Arrange
        when(listingService.getListingById(1L)).thenReturn(testListing);
        doNothing().when(fileStorageService).deleteImageByListingImageId(1L);

        // Act
        ResponseEntity<String> response = fileUploadController.deleteListingImage(1L, 1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Listing image deleted successfully", response.getBody());
        verify(listingService).getListingById(1L);
        verify(fileStorageService).deleteImageByListingImageId(1L);
    }

    @Test
    void deleteListingImage_ListingNotFound_ReturnsNotFound() {
        // Arrange
        when(listingService.getListingById(1L)).thenReturn(null);

        // Act
        ResponseEntity<String> response = fileUploadController.deleteListingImage(1L, 1L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(listingService).getListingById(1L);
        verify(fileStorageService, never()).deleteImageByListingImageId(any());
    }

    @Test
    void deleteListingImage_ServiceThrowsException_ReturnsInternalServerError() {
        // Arrange
        when(listingService.getListingById(1L)).thenReturn(testListing);
        doThrow(new RuntimeException("Delete error")).when(fileStorageService).deleteImageByListingImageId(1L);

        // Act
        ResponseEntity<String> response = fileUploadController.deleteListingImage(1L, 1L);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to delete listing image"));
        verify(listingService).getListingById(1L);
        verify(fileStorageService).deleteImageByListingImageId(1L);
    }

    // Tests for verifyUser method (using reflection to test private method)
    @Test
    void verifyUser_AlwaysReturnsTrue_CurrentImplementation() throws Exception {
        // Arrange
        java.lang.reflect.Method verifyUserMethod = FileUploadController.class.getDeclaredMethod("verifyUser", Listing.class, Long.class);
        verifyUserMethod.setAccessible(true);

        // Act
        boolean result = (boolean) verifyUserMethod.invoke(fileUploadController, testListing, 1L);

        // Assert
        assertTrue(result); // Current implementation always returns true
    }

    // Additional edge case tests
    @Test
    void uploadFile_NullFile_HandlesGracefully() {
        // Act
        ResponseEntity<String> response = fileUploadController.uploadFile(1L, null, 1);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("File cannot be null", response.getBody());
        verifyNoInteractions(listingService);
        verifyNoInteractions(fileStorageService);
    }

    @Test
    void uploadMultipleFiles_NullFiles_HandlesGracefully() {
        // Arrange
        int[] displayOrders = {1};

        // Act
        ResponseEntity<String> response = fileUploadController.uploadMultipleFiles(1L, null, displayOrders);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Files cannot be null", response.getBody());
        verifyNoInteractions(listingService);
        verifyNoInteractions(fileStorageService);
    }

    @Test
    void uploadMultipleFiles_DisplayOrdersMismatch_HandlesGracefully() {
        // Arrange
        int[] displayOrders = {1}; // Only one order for two files
        when(listingService.getListingById(1L)).thenReturn(testListing);
        when(fileStorageService.storeFiles(anyList(), any(Listing.class), any(int[].class)))
            .thenThrow(new RuntimeException("Display orders mismatch"));

        // Act
        ResponseEntity<String> response = fileUploadController.uploadMultipleFiles(1L, testFiles, displayOrders);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to upload files", response.getBody());
    }

    @Test
    void getListingImages_EmptyImagesList_ReturnsEmptyList() {
        // Arrange
        List<ListingImage> emptyImages = new ArrayList<>();
        when(listingService.getListingById(1L)).thenReturn(testListing);
        when(fileStorageService.getImagesByListing(testListing)).thenReturn(emptyImages);

        // Act
        ResponseEntity<?> response = fileUploadController.getListingImages(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emptyImages, response.getBody());
        assertTrue(((List<?>) response.getBody()).isEmpty());
    }

    @Test
    void deleteListingImage_InvalidImageId_HandlesGracefully() {
        // Arrange
        when(listingService.getListingById(1L)).thenReturn(testListing);
        doThrow(new RuntimeException("Image not found")).when(fileStorageService).deleteImageByListingImageId(999L);

        // Act
        ResponseEntity<String> response = fileUploadController.deleteListingImage(1L, 999L);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to delete listing image"));
    }
}
