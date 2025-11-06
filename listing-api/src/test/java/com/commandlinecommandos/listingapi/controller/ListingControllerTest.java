package com.commandlinecommandos.listingapi.controller;

import com.commandlinecommandos.listingapi.model.*;
import com.commandlinecommandos.listingapi.service.FileStorageService;
import com.commandlinecommandos.listingapi.service.ListingService;
import com.commandlinecommandos.listingapi.exception.ListingException;
import com.commandlinecommandos.listingapi.security.JwtHelper;
import com.commandlinecommandos.listingapi.dto.CreateListingRequest;
import com.commandlinecommandos.listingapi.dto.UpdateListingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ListingControllerTest {

    @Mock
    private ListingService listingService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private JwtHelper jwtHelper;

    @InjectMocks
    private ListingController listingController;

    private Listing testListing;
    private ListingImage testListingImage;
    private MockMultipartFile testFile;
    private List<MultipartFile> testFiles;
    private Pageable testPageable;
    private Page<Listing> testPage;
    private HttpServletRequest mockRequest;
    private HttpServletRequest mockRequestNoAuth;

    @BeforeEach
    void setUp() {
        // Create mock HTTP requests
        mockRequest = new MockHttpServletRequest();
        ((MockHttpServletRequest) mockRequest).addHeader("Authorization", "Bearer test-jwt-token");
        
        mockRequestNoAuth = new MockHttpServletRequest();
        
        // Mock JWT helper - use lenient() since not all tests use all mocks
        lenient().when(jwtHelper.extractUserIdFromRequest(mockRequest)).thenReturn(1L);
        lenient().when(jwtHelper.extractUserIdFromRequest(mockRequestNoAuth)).thenReturn(null);
        // Create test listing
        testListing = new Listing(
            "Test Item",
            "Test Description",
            new BigDecimal("99.99"),
            Category.ELECTRONICS,
            ItemCondition.GOOD,
            "Test Location",
            1L
        );
        testListing.setListingId(1L);
        testListing.setCreatedAt(LocalDateTime.now());
        testListing.setUpdatedAt(LocalDateTime.now());
        testListing.setStatus(ListingStatus.ACTIVE);
        testListing.setViewCount(5);

        // Create test listing image
        testListingImage = new ListingImage(
            testListing.getListingId(),
            "/path/to/test-image.jpg",
            "test-image.jpg",
            1
        );
        testListingImage.setImageId(1L);

        // Create test files
        testFile = new MockMultipartFile(
            "images",
            "test-image.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        testFiles = Arrays.asList(
            (MultipartFile) new MockMultipartFile("images", "test-image1.jpg", "image/jpeg", "test image 1 content".getBytes()),
            (MultipartFile) new MockMultipartFile("images", "test-image2.jpg", "image/jpeg", "test image 2 content".getBytes())
        );

        // Create test pageable and page
        testPageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        testPage = new PageImpl<>(Arrays.asList(testListing));
    }

    // Tests for getAllListings method
    @Test
    void getAllListings_Success_ReturnsOkResponse() {
        // Arrange
        when(listingService.getAllListings(any(Pageable.class))).thenReturn(testPage);

        // Act
        ResponseEntity<Page<?>> response = listingController.getAllListings(0, 20, "createdAt", "desc");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(listingService).getAllListings(any(Pageable.class));
    }

    @Test
    void getAllListings_WithCustomParameters_ReturnsOkResponse() {
        // Arrange
        when(listingService.getAllListings(any(Pageable.class))).thenReturn(testPage);

        // Act
        ResponseEntity<Page<?>> response = listingController.getAllListings(1, 10, "price", "asc");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(listingService).getAllListings(any(Pageable.class));
    }

    @Test
    void getAllListings_ServiceThrowsException_PropagatesException() {
        // Arrange
        when(listingService.getAllListings(any(Pageable.class)))
            .thenThrow(new ListingException("No listings found"));

        // Act & Assert
        assertThrows(ListingException.class, () -> {
            listingController.getAllListings(0, 20, "createdAt", "desc");
        });
        verify(listingService).getAllListings(any(Pageable.class));
    }

    // Tests for searchListings method
    @Test
    void searchListings_WithAllFilters_ReturnsOkResponse() {
        // Arrange
        when(listingService.getListingsWithFilters(
            any(ListingStatus.class), anyString(), any(Category.class), any(ItemCondition.class),
            any(BigDecimal.class), any(BigDecimal.class), anyString(), any(Pageable.class)))
            .thenReturn(testPage);

        // Act
        ResponseEntity<Page<?>> response = listingController.searchListings(
            ListingStatus.ACTIVE, "test", Category.ELECTRONICS, ItemCondition.GOOD,
            new BigDecimal("50.00"), new BigDecimal("150.00"), "Test Location",
            0, 20, "createdAt", "desc"
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(listingService).getListingsWithFilters(
            eq(ListingStatus.ACTIVE), eq("test"), eq(Category.ELECTRONICS), eq(ItemCondition.GOOD),
            eq(new BigDecimal("50.00")), eq(new BigDecimal("150.00")), eq("Test Location"), any(Pageable.class));
    }

    @Test
    void searchListings_WithNullFilters_ReturnsOkResponse() {
        // Arrange
        when(listingService.getListingsWithFilters(
            isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(testPage);

        // Act
        ResponseEntity<Page<?>> response = listingController.searchListings(
            null, null, null, null, null, null, null,
            0, 20, "createdAt", "desc"
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(listingService).getListingsWithFilters(
            isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void searchListings_ServiceThrowsException_PropagatesException() {
        // Arrange
        when(listingService.getListingsWithFilters(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
            .thenThrow(new ListingException("No listings found with filters"));

        // Act & Assert
        assertThrows(ListingException.class, () -> {
            listingController.searchListings(
                ListingStatus.ACTIVE, "test", Category.ELECTRONICS, ItemCondition.GOOD,
                new BigDecimal("50.00"), new BigDecimal("150.00"), "Test Location",
                0, 20, "createdAt", "desc"
            );
        });
    }

    // Tests for getListingsBySellerId method
    @Test
    void getListingsBySellerId_Success_ReturnsOkResponse() {
        // Arrange
        when(listingService.getListingsBySellerId(eq(1L), any(Pageable.class))).thenReturn(testPage);

        // Act
        ResponseEntity<Page<?>> response = listingController.getListingsBySellerId(1L, 0, 20, "createdAt", "desc");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(listingService).getListingsBySellerId(eq(1L), any(Pageable.class));
    }

    @Test
    void getListingsBySellerId_ServiceThrowsException_PropagatesException() {
        // Arrange
        when(listingService.getListingsBySellerId(eq(999L), any(Pageable.class)))
            .thenThrow(new ListingException("No listings found for seller with id: 999"));

        // Act & Assert
        assertThrows(ListingException.class, () -> {
            listingController.getListingsBySellerId(999L, 0, 20, "createdAt", "desc");
        });
    }

    // Tests for getListingById method
    @Test
    void getListingById_Success_ReturnsOkResponse() {
        // Arrange
        when(listingService.getListingById(1L)).thenReturn(testListing);

        // Act
        ResponseEntity<?> response = listingController.getListingById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testListing, response.getBody());
        verify(listingService).getListingById(1L);
        verify(listingService).incrementViewCount(1L);
    }

    @Test
    void getListingById_ListingNotFound_PropagatesException() {
        // Arrange
        when(listingService.getListingById(999L))
            .thenThrow(new ListingException("Listing not found with id: 999"));

        // Act & Assert
        assertThrows(ListingException.class, () -> {
            listingController.getListingById(999L);
        });
        verify(listingService).getListingById(999L);
        verify(listingService, never()).incrementViewCount(any());
    }

    // Tests for createListing method
    @Test
    void createListing_Success_ReturnsOkResponse() {
        // Arrange
        CreateListingRequest request = new CreateListingRequest();
        request.setTitle("New Item");
        request.setDescription("New Description");
        request.setPrice(new BigDecimal("149.99"));
        request.setCategory(Category.TEXTBOOKS);
        request.setCondition(ItemCondition.NEW);
        request.setLocation("New Location");

        Listing createdListing = new Listing(
            "New Item", "New Description", new BigDecimal("149.99"),
            Category.TEXTBOOKS, ItemCondition.NEW, "New Location", 1L
        );
        createdListing.setListingId(2L);

        when(listingService.createListing(anyString(), anyString(), any(BigDecimal.class), any(Category.class),
            any(ItemCondition.class), anyString(), anyLong())).thenReturn(createdListing);

        // Act
        ResponseEntity<?> response = listingController.createListing(request, mockRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdListing, response.getBody());
        verify(listingService).createListing(
            eq("New Item"), eq("New Description"), eq(new BigDecimal("149.99")),
            eq(Category.TEXTBOOKS), eq(ItemCondition.NEW), eq("New Location"), eq(1L));
    }

    @Test
    void createListing_ServiceThrowsException_PropagatesException() {
        // Arrange
        CreateListingRequest request = new CreateListingRequest();
        request.setTitle("New Item");
        request.setDescription("New Description");
        request.setPrice(new BigDecimal("149.99"));
        request.setCategory(Category.TEXTBOOKS);
        request.setCondition(ItemCondition.NEW);
        request.setLocation("New Location");

        when(listingService.createListing(anyString(), anyString(), any(BigDecimal.class), any(Category.class),
            any(ItemCondition.class), anyString(), anyLong()))
            .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            listingController.createListing(request, mockRequest);
        });
    }

    // Tests for uploadImages method
    @Test
    void uploadImages_Success_ReturnsOkResponse() {
        // Arrange
        int[] displayOrders = {1, 2};
        List<ListingImage> storedImages = Arrays.asList(testListingImage);
        Listing updatedListing = new Listing(
            testListing.getTitle(),
            testListing.getDescription(),
            testListing.getPrice(),
            testListing.getCategory(),
            testListing.getCondition(),
            testListing.getLocation(),
            testListing.getSellerId()
        );
        updatedListing.setListingId(testListing.getListingId());
        updatedListing.setStatus(testListing.getStatus());
        updatedListing.setImages(testListing.getImages());
        updatedListing.addImages(storedImages);

        when(listingService.isListingOwner(1L, 1L)).thenReturn(true);
        when(listingService.getListingById(1L)).thenReturn(testListing);
        when(fileStorageService.storeFiles(eq(testFiles), eq(testListing), eq(displayOrders))).thenReturn(storedImages);
        when(listingService.addImagesToListing(eq(1L), eq(storedImages))).thenReturn(updatedListing);

        // Act
        ResponseEntity<?> response = listingController.uploadImages(1L, testFiles, displayOrders, mockRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(updatedListing, response.getBody());
        verify(listingService).isListingOwner(1L, 1L);
        verify(listingService).getListingById(1L);
        verify(fileStorageService).storeFiles(eq(testFiles), eq(testListing), eq(displayOrders));
        verify(listingService).addImagesToListing(eq(1L), eq(storedImages));
    }

    @Test
    void uploadImages_NotListingOwner_ReturnsForbidden() {
        // Arrange
        int[] displayOrders = {1, 2};
        when(listingService.isListingOwner(1L, 1L)).thenReturn(false);

        // Act
        ResponseEntity<?> response = listingController.uploadImages(1L, testFiles, displayOrders, mockRequest);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You are not allowed to upload images for this listing", response.getBody());
        verify(listingService).isListingOwner(1L, 1L);
        verify(listingService, never()).getListingById(any());
        verify(fileStorageService, never()).storeFiles(any(), any(), any());
        verify(listingService, never()).addImagesToListing(any(), any());
    }

    @Test
    void uploadImages_ServiceThrowsException_PropagatesException() {
        // Arrange
        int[] displayOrders = {1, 2};
        when(listingService.isListingOwner(1L, 1L)).thenReturn(true);
        when(listingService.getListingById(1L)).thenReturn(testListing);
        when(fileStorageService.storeFiles(eq(testFiles), eq(testListing), eq(displayOrders)))
            .thenThrow(new RuntimeException("Storage error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            listingController.uploadImages(1L, testFiles, displayOrders, mockRequest);
        });
    }

    // Tests for updateListing method
    @Test
    void updateListing_Success_ReturnsOkResponse() {
        // Arrange
        UpdateListingRequest request = new UpdateListingRequest();
        request.setTitle("Updated Item");
        request.setDescription("Updated Description");
        request.setPrice(new BigDecimal("199.99"));
        request.setCategory(Category.GADGETS);
        request.setCondition(ItemCondition.LIKE_NEW);
        request.setLocation("Updated Location");
        request.setImages(Arrays.asList(testListingImage));

        Listing updatedListing = new Listing(
            "Updated Item", "Updated Description", new BigDecimal("199.99"),
            Category.GADGETS, ItemCondition.LIKE_NEW, "Updated Location", 1L
        );
        updatedListing.setListingId(1L);

        when(listingService.isListingOwner(1L, 1L)).thenReturn(true);
        when(listingService.updateListing(eq(1L), anyString(), anyString(), any(BigDecimal.class),
            any(Category.class), any(ItemCondition.class), anyString(), anyList())).thenReturn(updatedListing);

        // Act
        ResponseEntity<?> response = listingController.updateListing(1L, request, mockRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(updatedListing, response.getBody());
        verify(listingService).isListingOwner(1L, 1L);
        verify(listingService).updateListing(eq(1L), eq("Updated Item"), eq("Updated Description"),
            eq(new BigDecimal("199.99")), eq(Category.GADGETS), eq(ItemCondition.LIKE_NEW),
            eq("Updated Location"), eq(Arrays.asList(testListingImage)));
    }

    @Test
    void updateListing_NotListingOwner_ReturnsForbidden() {
        // Arrange
        UpdateListingRequest request = new UpdateListingRequest();
        request.setTitle("Updated Item");
        when(listingService.isListingOwner(1L, 1L)).thenReturn(false);

        // Act
        ResponseEntity<?> response = listingController.updateListing(1L, request, mockRequest);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You are not allowed to update this listing", response.getBody());
        verify(listingService).isListingOwner(1L, 1L);
        verify(listingService, never()).updateListing(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void updateListing_ServiceThrowsException_PropagatesException() {
        // Arrange
        UpdateListingRequest request = new UpdateListingRequest();
        request.setTitle("Updated Item");
        when(listingService.isListingOwner(1L, 1L)).thenReturn(true);
        when(listingService.updateListing(eq(1L), anyString(), anyString(), any(BigDecimal.class),
            any(Category.class), any(ItemCondition.class), anyString(), anyList()))
            .thenThrow(new RuntimeException("Update error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            listingController.updateListing(1L, request, mockRequest);
        });
    }

    // Tests for markAsSold method
    @Test
    void markAsSold_Success_ReturnsOkResponse() {
        // Arrange
        Listing soldListing = new Listing(
            testListing.getTitle(),
            testListing.getDescription(),
            testListing.getPrice(),
            testListing.getCategory(),
            testListing.getCondition(),
            testListing.getLocation(),
            testListing.getSellerId()
        );
        soldListing.setListingId(testListing.getListingId());
        soldListing.setStatus(testListing.getStatus());
        soldListing.setImages(testListing.getImages());
        soldListing.markAsSold();

        when(listingService.isListingOwner(1L, 1L)).thenReturn(true);
        when(listingService.markAsSold(1L)).thenReturn(soldListing);

        // Act
        ResponseEntity<?> response = listingController.markAsSold(1L, mockRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(soldListing, response.getBody());
        assertEquals(ListingStatus.SOLD, ((Listing) response.getBody()).getStatus());
        verify(listingService).isListingOwner(1L, 1L);
        verify(listingService).markAsSold(1L);
    }

    @Test
    void markAsSold_NotListingOwner_ReturnsForbidden() {
        // Arrange
        when(listingService.isListingOwner(1L, 1L)).thenReturn(false);

        // Act
        ResponseEntity<?> response = listingController.markAsSold(1L, mockRequest);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You are not allowed to mark this listing as sold", response.getBody());
        verify(listingService).isListingOwner(1L, 1L);
        verify(listingService, never()).markAsSold(any());
    }

    @Test
    void markAsSold_ServiceThrowsException_PropagatesException() {
        // Arrange
        when(listingService.isListingOwner(1L, 1L)).thenReturn(true);
        when(listingService.markAsSold(1L)).thenThrow(new RuntimeException("Mark as sold error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            listingController.markAsSold(1L, mockRequest);
        });
    }

    // Tests for cancelListing method
    @Test
    void cancelListing_Success_ReturnsOkResponse() {
        // Arrange
        Listing cancelledListing = new Listing(
            testListing.getTitle(),
            testListing.getDescription(),
            testListing.getPrice(),
            testListing.getCategory(),
            testListing.getCondition(),
            testListing.getLocation(),
            testListing.getSellerId()
        );
        cancelledListing.setListingId(testListing.getListingId());
        cancelledListing.setStatus(testListing.getStatus());
        cancelledListing.setImages(testListing.getImages());
        cancelledListing.setStatus(ListingStatus.CANCELLED);

        when(listingService.isListingOwner(1L, 1L)).thenReturn(true);
        when(listingService.cancelListing(1L)).thenReturn(cancelledListing);

        // Act
        ResponseEntity<?> response = listingController.cancelListing(1L, mockRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(cancelledListing, response.getBody());
        assertEquals(ListingStatus.CANCELLED, ((Listing) response.getBody()).getStatus());
        verify(listingService).isListingOwner(1L, 1L);
        verify(listingService).cancelListing(1L);
    }

    @Test
    void cancelListing_NotListingOwner_ReturnsForbidden() {
        // Arrange
        when(listingService.isListingOwner(1L, 1L)).thenReturn(false);

        // Act
        ResponseEntity<?> response = listingController.cancelListing(1L, mockRequest);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You are not allowed to cancel this listing", response.getBody());
        verify(listingService).isListingOwner(1L, 1L);
        verify(listingService, never()).cancelListing(any());
    }

    @Test
    void cancelListing_ServiceThrowsException_PropagatesException() {
        // Arrange
        when(listingService.isListingOwner(1L, 1L)).thenReturn(true);
        when(listingService.cancelListing(1L)).thenThrow(new RuntimeException("Cancel error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            listingController.cancelListing(1L, mockRequest);
        });
    }

    // Tests for deleteListing method
    @Test
    void deleteListing_Success_ReturnsOkResponse() {
        // Arrange
        when(listingService.isListingOwner(1L, 1L)).thenReturn(true);
        doNothing().when(listingService).deleteListing(1L);

        // Act
        ResponseEntity<String> response = listingController.deleteListing(1L, mockRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Listing deleted successfully", response.getBody());
        verify(listingService).isListingOwner(1L, 1L);
        verify(listingService).deleteListing(1L);
    }

    @Test
    void deleteListing_NotListingOwner_ReturnsForbidden() {
        // Arrange
        when(listingService.isListingOwner(1L, 1L)).thenReturn(false);

        // Act
        ResponseEntity<String> response = listingController.deleteListing(1L, mockRequest);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You are not allowed to delete this listing", response.getBody());
        verify(listingService).isListingOwner(1L, 1L);
        verify(listingService, never()).deleteListing(any());
    }

    @Test
    void deleteListing_ServiceThrowsException_PropagatesException() {
        // Arrange
        when(listingService.isListingOwner(1L, 1L)).thenReturn(true);
        doThrow(new RuntimeException("Delete error")).when(listingService).deleteListing(1L);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            listingController.deleteListing(1L, mockRequest);
        });
    }

    // Tests for CreateListingRequest DTO
    @Test
    void createListingRequest_GettersAndSetters_WorkCorrectly() {
        // Arrange
        CreateListingRequest request = new CreateListingRequest();

        // Act & Assert
        request.setTitle("Test Title");
        assertEquals("Test Title", request.getTitle());

        request.setDescription("Test Description");
        assertEquals("Test Description", request.getDescription());

        request.setPrice(new BigDecimal("99.99"));
        assertEquals(new BigDecimal("99.99"), request.getPrice());

        request.setCategory(Category.ELECTRONICS);
        assertEquals(Category.ELECTRONICS, request.getCategory());

        request.setCondition(ItemCondition.GOOD);
        assertEquals(ItemCondition.GOOD, request.getCondition());

        request.setLocation("Test Location");
        assertEquals("Test Location", request.getLocation());
    }

    // Tests for UpdateListingRequest DTO
    @Test
    void updateListingRequest_GettersAndSetters_WorkCorrectly() {
        // Arrange
        UpdateListingRequest request = new UpdateListingRequest();

        // Act & Assert
        request.setTitle("Updated Title");
        assertEquals("Updated Title", request.getTitle());

        request.setDescription("Updated Description");
        assertEquals("Updated Description", request.getDescription());

        request.setPrice(new BigDecimal("149.99"));
        assertEquals(new BigDecimal("149.99"), request.getPrice());

        request.setCategory(Category.TEXTBOOKS);
        assertEquals(Category.TEXTBOOKS, request.getCategory());

        request.setCondition(ItemCondition.LIKE_NEW);
        assertEquals(ItemCondition.LIKE_NEW, request.getCondition());

        request.setLocation("Updated Location");
        assertEquals("Updated Location", request.getLocation());

        List<ListingImage> images = Arrays.asList(testListingImage);
        request.setImages(images);
        assertEquals(images, request.getImages());
    }

    // Edge cases and additional scenarios
    @Test
    void searchListings_WithMinPriceOnly_ReturnsOkResponse() {
        // Arrange
        when(listingService.getListingsWithFilters(
            isNull(), isNull(), isNull(), isNull(), any(BigDecimal.class), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(testPage);

        // Act
        ResponseEntity<Page<?>> response = listingController.searchListings(
            null, null, null, null, new BigDecimal("50.00"), null, null,
            0, 20, "createdAt", "desc"
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(listingService).getListingsWithFilters(
            isNull(), isNull(), isNull(), isNull(), eq(new BigDecimal("50.00")), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void searchListings_WithMaxPriceOnly_ReturnsOkResponse() {
        // Arrange
        when(listingService.getListingsWithFilters(
            isNull(), isNull(), isNull(), isNull(), isNull(), any(BigDecimal.class), isNull(), any(Pageable.class)))
            .thenReturn(testPage);

        // Act
        ResponseEntity<Page<?>> response = listingController.searchListings(
            null, null, null, null, null, new BigDecimal("200.00"), null,
            0, 20, "createdAt", "desc"
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(listingService).getListingsWithFilters(
            isNull(), isNull(), isNull(), isNull(), isNull(), eq(new BigDecimal("200.00")), isNull(), any(Pageable.class));
    }

    @Test
    void uploadImages_WithEmptyFilesList_HandlesGracefully() {
        // Arrange
        int[] displayOrders = {1};
        List<MultipartFile> emptyFiles = new ArrayList<>();
        when(listingService.isListingOwner(1L, 1L)).thenReturn(true);
        when(listingService.getListingById(1L)).thenReturn(testListing);
        when(fileStorageService.storeFiles(eq(emptyFiles), eq(testListing), eq(displayOrders))).thenReturn(new ArrayList<>());
        when(listingService.addImagesToListing(eq(1L), any())).thenReturn(testListing);

        // Act
        ResponseEntity<?> response = listingController.uploadImages(1L, emptyFiles, displayOrders, mockRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(listingService).isListingOwner(1L, 1L);
        verify(fileStorageService).storeFiles(eq(emptyFiles), eq(testListing), eq(displayOrders));
    }

    @Test
    void updateListing_WithNullImages_HandlesGracefully() {
        // Arrange
        UpdateListingRequest request = new UpdateListingRequest();
        request.setTitle("Updated Item");
        request.setImages(null);

        Listing updatedListing = new Listing(
            "Updated Item",
            testListing.getDescription(),
            testListing.getPrice(),
            testListing.getCategory(),
            testListing.getCondition(),
            testListing.getLocation(),
            testListing.getSellerId()
        );
        updatedListing.setListingId(testListing.getListingId());
        updatedListing.setStatus(testListing.getStatus());
        updatedListing.setImages(testListing.getImages());

        when(listingService.isListingOwner(1L, 1L)).thenReturn(true);
        when(listingService.updateListing(eq(1L), eq("Updated Item"), any(), any(), any(), any(), any(), isNull()))
            .thenReturn(updatedListing);

        // Act
        ResponseEntity<?> response = listingController.updateListing(1L, request, mockRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(updatedListing, response.getBody());
        verify(listingService).isListingOwner(1L, 1L);
        verify(listingService).updateListing(eq(1L), eq("Updated Item"), any(), any(), any(), any(), any(), isNull());
    }

    @Test
    void createListing_WithNullPrice_HandlesGracefully() {
        // Arrange
        CreateListingRequest request = new CreateListingRequest();
        request.setTitle("New Item");
        request.setDescription("New Description");
        request.setPrice(null);
        request.setCategory(Category.ELECTRONICS);
        request.setCondition(ItemCondition.NEW);
        request.setLocation("New Location");

        when(listingService.createListing(anyString(), anyString(), isNull(), any(Category.class),
            any(ItemCondition.class), anyString(), anyLong()))
            .thenThrow(new IllegalArgumentException("Price cannot be null"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            listingController.createListing(request, mockRequest);
        });
    }

    // Tests for unauthorized access scenarios
    @Test
    void createListing_NoJwtToken_ReturnsUnauthorized() {
        // Arrange
        CreateListingRequest request = new CreateListingRequest();
        request.setTitle("New Item");
        request.setDescription("New Description");
        request.setPrice(new BigDecimal("149.99"));
        request.setCategory(Category.TEXTBOOKS);
        request.setCondition(ItemCondition.NEW);
        request.setLocation("New Location");

        // Act
        ResponseEntity<?> response = listingController.createListing(request, mockRequestNoAuth);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication required", response.getBody());
        verify(listingService, never()).createListing(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void uploadImages_NoJwtToken_ReturnsUnauthorized() {
        // Arrange
        int[] displayOrders = {1, 2};

        // Act
        ResponseEntity<?> response = listingController.uploadImages(1L, testFiles, displayOrders, mockRequestNoAuth);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication required", response.getBody());
        verify(listingService, never()).isListingOwner(any(), any());
    }

    @Test
    void updateListing_NoJwtToken_ReturnsUnauthorized() {
        // Arrange
        UpdateListingRequest request = new UpdateListingRequest();
        request.setTitle("Updated Item");

        // Act
        ResponseEntity<?> response = listingController.updateListing(1L, request, mockRequestNoAuth);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication required", response.getBody());
        verify(listingService, never()).isListingOwner(any(), any());
    }

    @Test
    void markAsSold_NoJwtToken_ReturnsUnauthorized() {
        // Act
        ResponseEntity<?> response = listingController.markAsSold(1L, mockRequestNoAuth);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication required", response.getBody());
        verify(listingService, never()).isListingOwner(any(), any());
    }

    @Test
    void cancelListing_NoJwtToken_ReturnsUnauthorized() {
        // Act
        ResponseEntity<?> response = listingController.cancelListing(1L, mockRequestNoAuth);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication required", response.getBody());
        verify(listingService, never()).isListingOwner(any(), any());
    }

    @Test
    void deleteListing_NoJwtToken_ReturnsUnauthorized() {
        // Act
        ResponseEntity<String> response = listingController.deleteListing(1L, mockRequestNoAuth);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication required", response.getBody());
        verify(listingService, never()).isListingOwner(any(), any());
    }
}
