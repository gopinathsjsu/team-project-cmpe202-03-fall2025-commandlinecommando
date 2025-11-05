package com.commandlinecommandos.listingapi.service;

import com.commandlinecommandos.listingapi.exception.ListingException;
import com.commandlinecommandos.listingapi.exception.ListingNotFoundException;
import com.commandlinecommandos.listingapi.model.*;
import com.commandlinecommandos.listingapi.repository.ListingRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @InjectMocks
    private ListingService listingService;

    private Listing testListing;
    private ListingImage testImage;
    private List<ListingImage> testImages;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        testPageable = PageRequest.of(0, 10);
        
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
        testListing.setViewCount(0);

        // Create test image
        testImage = new ListingImage(testListing.getListingId(), "/path/to/image.jpg", "test-image.jpg", 1);
        testImage.setImageId(1L);

        // Create test images list
        testImages = new ArrayList<>();
        testImages.add(testImage);
    }

    // Test createListing methods
    @Test
    void testCreateListing_WithoutImages_Success() {
        // Arrange
        when(listingRepository.save(any(Listing.class))).thenReturn(testListing);

        // Act
        Listing result = listingService.createListing(
            "Test Item",
            "Test Description",
            new BigDecimal("99.99"),
            Category.ELECTRONICS,
            ItemCondition.GOOD,
            "Test Location",
            1L
        );

        // Assert
        assertNotNull(result);
        assertEquals("Test Item", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals(new BigDecimal("99.99"), result.getPrice());
        assertEquals(Category.ELECTRONICS, result.getCategory());
        assertEquals(ItemCondition.GOOD, result.getCondition());
        assertEquals("Test Location", result.getLocation());
        assertEquals(Long.valueOf(1L), result.getSellerId());
        assertEquals(ListingStatus.ACTIVE, result.getStatus());
        assertEquals(0, result.getViewCount());
        assertNotNull(result.getImages());
        assertTrue(result.getImages().isEmpty());

        verify(listingRepository).save(any(Listing.class));
    }

    @Test
    void testCreateListing_WithImages_Success() {
        // Arrange
        testListing.setImages(testImages);
        when(listingRepository.save(any(Listing.class))).thenReturn(testListing);

        // Act
        Listing result = listingService.createListing(
            "Test Item",
            "Test Description",
            new BigDecimal("99.99"),
            Category.ELECTRONICS,
            ItemCondition.GOOD,
            "Test Location",
            1L,
            testImages
        );

        // Assert
        assertNotNull(result);
        assertEquals("Test Item", result.getTitle());
        assertEquals(testImages, result.getImages());
        assertEquals(1, result.getImages().size());

        verify(listingRepository).save(any(Listing.class));
    }

    // Test getListingById method
    @Test
    void testGetListingById_Success() {
        // Arrange
        when(listingRepository.findById(1L)).thenReturn(Optional.of(testListing));

        // Act
        Listing result = listingService.getListingById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testListing, result);
        verify(listingRepository).findById(1L);
    }

    @Test
    void testGetListingById_NotFound() {
        // Arrange
        when(listingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ListingNotFoundException exception = assertThrows(ListingNotFoundException.class, () -> {
            listingService.getListingById(999L);
        });

        // Assert
        assertEquals("Listing with ID 999 not found", exception.getMessage());
        verify(listingRepository).findById(999L);
    }

    // Test getAllListings method
    @Test
    void testGetAllListings_Success() {
        // Arrange
        List<Listing> listings = Arrays.asList(testListing);
        Page<Listing> expectedPage = new PageImpl<>(listings);
        when(listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.ACTIVE, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Listing> result = listingService.getAllListings(testPageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testListing, result.getContent().get(0));
        verify(listingRepository).findByStatusOrderByCreatedAtDesc(ListingStatus.ACTIVE, testPageable);
    }

    @Test
    void testGetAllListings_NotFound() {
        // Arrange
        when(listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.ACTIVE, testPageable))
            .thenReturn(Optional.empty());

        // Act 
        ListingNotFoundException exception = assertThrows(ListingNotFoundException.class, () -> {
            listingService.getAllListings(testPageable);
        });

        // Assert
        assertEquals("No active listings found", exception.getMessage());
        verify(listingRepository).findByStatusOrderByCreatedAtDesc(ListingStatus.ACTIVE, testPageable);
    }

    // Test getListingsBySellerId method
    @Test
    void testGetListingsBySellerId_Success() {
        // Arrange
        List<Listing> listings = Arrays.asList(testListing);
        Page<Listing> expectedPage = new PageImpl<>(listings);
        when(listingRepository.findBySellerId(1L, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Listing> result = listingService.getListingsBySellerId(1L, testPageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testListing, result.getContent().get(0));
        verify(listingRepository).findBySellerId(1L, testPageable);
    }

    @Test
    void testGetListingsBySellerId_NotFound() {
        // Arrange
        when(listingRepository.findBySellerId(999L, testPageable))
            .thenReturn(Optional.empty());

        // Act
        ListingNotFoundException exception = assertThrows(ListingNotFoundException.class, () -> {
            listingService.getListingsBySellerId(999L, testPageable);
        });

        // Assert
        assertEquals("No listings found for seller with id: 999", exception.getMessage());
        verify(listingRepository).findBySellerId(999L, testPageable);
    }

    // Test getListingsBySellerIdAndStatus method
    @Test
    void testGetListingsBySellerIdAndStatus_Success() {
        // Arrange
        List<Listing> listings = Arrays.asList(testListing);
        Page<Listing> expectedPage = new PageImpl<>(listings);
        when(listingRepository.findBySellerIdAndStatus(1L, ListingStatus.ACTIVE, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Listing> result = listingService.getListingsBySellerIdAndStatus(1L, ListingStatus.ACTIVE, testPageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testListing, result.getContent().get(0));
        verify(listingRepository).findBySellerIdAndStatus(1L, ListingStatus.ACTIVE, testPageable);
    }

    @Test
    void testGetListingsBySellerIdAndStatus_NotFound() {
        // Arrange
        when(listingRepository.findBySellerIdAndStatus(999L, ListingStatus.SOLD, testPageable))
            .thenReturn(Optional.empty());

        // Act
        ListingNotFoundException exception = assertThrows(ListingNotFoundException.class, () -> {
            listingService.getListingsBySellerIdAndStatus(999L, ListingStatus.SOLD, testPageable);
        });

        // Assert
        assertEquals("No listings found for seller with id: 999 and status: SOLD", exception.getMessage());
        verify(listingRepository).findBySellerIdAndStatus(999L, ListingStatus.SOLD, testPageable);
    }

    // Test getListingsByCategory method
    @Test
    void testGetListingsByCategory_Success() {
        // Arrange
        List<Listing> listings = Arrays.asList(testListing);
        Page<Listing> expectedPage = new PageImpl<>(listings);
        when(listingRepository.findByCategory(Category.ELECTRONICS, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Listing> result = listingService.getListingsByCategory(Category.ELECTRONICS, testPageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testListing, result.getContent().get(0));
        verify(listingRepository).findByCategory(Category.ELECTRONICS, testPageable);
    }

    @Test
    void testGetListingsByCategory_NotFound() {
        // Arrange
        when(listingRepository.findByCategory(Category.TEXTBOOKS, testPageable))
            .thenReturn(Optional.empty());

        // Act
        ListingNotFoundException exception = assertThrows(ListingNotFoundException.class, () -> {
            listingService.getListingsByCategory(Category.TEXTBOOKS, testPageable);
        });

        // Assert
        assertEquals("No listings found for category: TEXTBOOKS", exception.getMessage());
        verify(listingRepository).findByCategory(Category.TEXTBOOKS, testPageable);
    }

    // Test getListingsByStatus method
    @Test
    void testGetListingsByStatus_Success() {
        // Arrange
        List<Listing> listings = Arrays.asList(testListing);
        Page<Listing> expectedPage = new PageImpl<>(listings);
        when(listingRepository.findByStatus(ListingStatus.ACTIVE, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Listing> result = listingService.getListingsByStatus(ListingStatus.ACTIVE, testPageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testListing, result.getContent().get(0));
        verify(listingRepository).findByStatus(ListingStatus.ACTIVE, testPageable);
    }

    @Test
    void testGetListingsByStatus_NotFound() {
        // Arrange
        when(listingRepository.findByStatus(ListingStatus.CANCELLED, testPageable))
            .thenReturn(Optional.empty());

        // Act
        ListingNotFoundException exception = assertThrows(ListingNotFoundException.class, () -> {
            listingService.getListingsByStatus(ListingStatus.CANCELLED, testPageable);
        });

        // Assert
        assertEquals("No listings found for status: CANCELLED", exception.getMessage());
        verify(listingRepository).findByStatus(ListingStatus.CANCELLED, testPageable);
    }

    // Test searchListings method
    @Test
    void testSearchListings_Success() {
        // Arrange
        String keyword = "test";
        List<Listing> listings = Arrays.asList(testListing);
        Page<Listing> expectedPage = new PageImpl<>(listings);
        when(listingRepository.findByTitleOrDescriptionContaining(keyword, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Listing> result = listingService.searchListings(keyword, testPageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testListing, result.getContent().get(0));
        verify(listingRepository).findByTitleOrDescriptionContaining(keyword, testPageable);
    }

    @Test
    void testSearchListings_NotFound() {
        // Arrange
        String keyword = "nonexistent";
        when(listingRepository.findByTitleOrDescriptionContaining(keyword, testPageable))
            .thenReturn(Optional.empty());

        // Act
        ListingNotFoundException exception = assertThrows(ListingNotFoundException.class, () -> {
            listingService.searchListings(keyword, testPageable);
        });

        // Assert
        assertEquals("No listings found with keyword: nonexistent", exception.getMessage());
        verify(listingRepository).findByTitleOrDescriptionContaining(keyword, testPageable);
    }

    // Test getListingsWithFilters method
    @Test
    void testGetListingsWithFilters_Success() {
        // Arrange
        List<Listing> listings = Arrays.asList(testListing);
        Page<Listing> expectedPage = new PageImpl<>(listings);
        when(listingRepository.findWithFilters(
            ListingStatus.ACTIVE, "test", Category.ELECTRONICS, ItemCondition.GOOD,
            new BigDecimal("50.00"), new BigDecimal("150.00"), "Test Location", testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Listing> result = listingService.getListingsWithFilters(
            ListingStatus.ACTIVE, "test", Category.ELECTRONICS, ItemCondition.GOOD,
            new BigDecimal("50.00"), new BigDecimal("150.00"), "Test Location", testPageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testListing, result.getContent().get(0));
        verify(listingRepository).findWithFilters(
            ListingStatus.ACTIVE, "test", Category.ELECTRONICS, ItemCondition.GOOD,
            new BigDecimal("50.00"), new BigDecimal("150.00"), "Test Location", testPageable);
    }

    @Test
    void testGetListingsWithFilters_NotFound() {
        // Arrange
        when(listingRepository.findWithFilters(
            ListingStatus.ACTIVE, "test", Category.ELECTRONICS, ItemCondition.GOOD,
            new BigDecimal("50.00"), new BigDecimal("150.00"), "Test Location", testPageable))
            .thenReturn(Optional.empty());

        // Act
        ListingNotFoundException exception = assertThrows(ListingNotFoundException.class, () -> {
            listingService.getListingsWithFilters(
                ListingStatus.ACTIVE, "test", Category.ELECTRONICS, ItemCondition.GOOD,
                new BigDecimal("50.00"), new BigDecimal("150.00"), "Test Location", testPageable);
        });

        // Assert
        assertEquals("No listings found with filters", exception.getMessage());
        verify(listingRepository).findWithFilters(
            ListingStatus.ACTIVE, "test", Category.ELECTRONICS, ItemCondition.GOOD,
            new BigDecimal("50.00"), new BigDecimal("150.00"), "Test Location", testPageable);
    }

    // Test updateListing method
    @Test
    void testUpdateListing_Success() {
        // Arrange
        Listing updatedListing = new Listing(
            "Updated Title",
            "Updated Description",
            new BigDecimal("149.99"),
            Category.TEXTBOOKS,
            ItemCondition.LIKE_NEW,
            "Updated Location",
            1L
        );
        updatedListing.setListingId(1L);
        updatedListing.setImages(testImages);
        updatedListing.setUpdatedAt(LocalDateTime.now()); // Set updatedAt explicitly

        when(listingRepository.findById(1L)).thenReturn(Optional.of(testListing));
        when(listingRepository.save(any(Listing.class))).thenReturn(updatedListing);

        // Act
        Listing result = listingService.updateListing(
            1L,
            "Updated Title",
            "Updated Description",
            new BigDecimal("149.99"),
            Category.TEXTBOOKS,
            ItemCondition.LIKE_NEW,
            "Updated Location",
            testImages
        );

        // Assert
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(new BigDecimal("149.99"), result.getPrice());
        assertEquals(Category.TEXTBOOKS, result.getCategory());
        assertEquals(ItemCondition.LIKE_NEW, result.getCondition());
        assertEquals("Updated Location", result.getLocation());
        assertEquals(testImages, result.getImages());

        verify(listingRepository).findById(1L);
        verify(listingRepository).save(any(Listing.class));
    }

    @Test
    void testUpdateListing_NotFound() {
        // Arrange
        when(listingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ListingNotFoundException exception = assertThrows(ListingNotFoundException.class, () -> {
            listingService.updateListing(
                999L,
                "Updated Title",
                "Updated Description",
                new BigDecimal("149.99"),
                Category.TEXTBOOKS,
                ItemCondition.LIKE_NEW,
                "Updated Location",
                testImages
            );
        });

        // Assert
        assertEquals("Listing with ID 999 not found", exception.getMessage());
        verify(listingRepository).findById(999L);
        verify(listingRepository, never()).save(any(Listing.class));
    }

    // Test addImagesToListing method
    @Test
    void testAddImagesToListing_Success() {
        // Arrange
        List<ListingImage> newImages = Arrays.asList(
            new ListingImage(testListing.getListingId(), "/path/to/new-image.jpg", "new-image.jpg", 2)
        );
        
        when(listingRepository.findById(1L)).thenReturn(Optional.of(testListing));
        when(listingRepository.save(any(Listing.class))).thenReturn(testListing);

        // Act
        Listing result = listingService.addImagesToListing(1L, newImages);

        // Assert
        assertNotNull(result);
        verify(listingRepository).findById(1L);
        verify(listingRepository).save(any(Listing.class));
    }

    @Test
    void testAddImagesToListing_NotFound() {
        // Arrange
        List<ListingImage> newImages = Arrays.asList(testImage);
        when(listingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ListingNotFoundException exception = assertThrows(ListingNotFoundException.class, () -> {
            listingService.addImagesToListing(999L, newImages);
        });

        // Assert
        assertEquals("Listing with ID 999 not found", exception.getMessage());
        verify(listingRepository).findById(999L);
        verify(listingRepository, never()).save(any(Listing.class));
    }

    // Test markAsSold method
    @Test
    void testMarkAsSold_Success() {
        // Arrange
        when(listingRepository.findById(1L)).thenReturn(Optional.of(testListing));
        when(listingRepository.save(any(Listing.class))).thenReturn(testListing);

        // Act
        Listing result = listingService.markAsSold(1L);

        // Assert
        assertNotNull(result);
        assertEquals(ListingStatus.SOLD, result.getStatus());
        verify(listingRepository).findById(1L);
        verify(listingRepository).save(any(Listing.class));
    }

    @Test
    void testMarkAsSold_NotFound() {
        // Arrange
        when(listingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ListingNotFoundException exception = assertThrows(ListingNotFoundException.class, () -> {
            listingService.markAsSold(999L);
        });

        // Assert
        assertEquals("Listing with ID 999 not found", exception.getMessage());
        verify(listingRepository).findById(999L);
        verify(listingRepository, never()).save(any(Listing.class));
    }

    // Test cancelListing method
    @Test
    void testCancelListing_Success() {
        // Arrange
        when(listingRepository.findById(1L)).thenReturn(Optional.of(testListing));
        when(listingRepository.save(any(Listing.class))).thenReturn(testListing);

        // Act
        Listing result = listingService.cancelListing(1L);

        // Assert
        assertNotNull(result);
        assertEquals(ListingStatus.CANCELLED, result.getStatus());
        verify(listingRepository).findById(1L);
        verify(listingRepository).save(any(Listing.class));
    }

    @Test
    void testCancelListing_NotFound() {
        // Arrange
        when(listingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ListingNotFoundException exception = assertThrows(ListingNotFoundException.class, () -> {
            listingService.cancelListing(999L);
        });

        // Assert
        assertEquals("Listing with ID 999 not found", exception.getMessage());
        verify(listingRepository).findById(999L);
        verify(listingRepository, never()).save(any(Listing.class));
    }

    // Test deleteListing method
    @Test
    void testDeleteListing_Success() {
        // Arrange
        when(listingRepository.findById(1L)).thenReturn(Optional.of(testListing));
        doNothing().when(listingRepository).delete(testListing);

        // Act
        listingService.deleteListing(1L);

        // Assert
        verify(listingRepository).findById(1L);
        verify(listingRepository).delete(testListing);
    }

    @Test
    void testDeleteListing_NotFound() {
        // Arrange
        when(listingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ListingNotFoundException exception = assertThrows(ListingNotFoundException.class, () -> {
            listingService.deleteListing(999L);
        });

        // Assert
        assertEquals("Listing with ID 999 not found", exception.getMessage());
        verify(listingRepository).findById(999L);
        verify(listingRepository, never()).delete(any(Listing.class));
    }

    // Test countListingsBySellerIdAndStatus method
    @Test
    void testCountListingsBySellerIdAndStatus_Success() {
        // Arrange
        Long expectedCount = 5L;
        when(listingRepository.countBySellerIdAndStatus(1L, ListingStatus.ACTIVE))
            .thenReturn(expectedCount);

        // Act
        Long result = listingService.countListingsBySellerIdAndStatus(1L, ListingStatus.ACTIVE);

        // Assert
        assertEquals(expectedCount, result);
        verify(listingRepository).countBySellerIdAndStatus(1L, ListingStatus.ACTIVE);
    }

    @Test
    void testCountListingsBySellerIdAndStatus_ZeroCount() {
        // Arrange
        when(listingRepository.countBySellerIdAndStatus(999L, ListingStatus.SOLD))
            .thenReturn(0L);

        // Act
        Long result = listingService.countListingsBySellerIdAndStatus(999L, ListingStatus.SOLD);

        // Assert
        assertEquals(0L, result);
        verify(listingRepository).countBySellerIdAndStatus(999L, ListingStatus.SOLD);
    }

    // Test isListingOwner method
    @Test
    void testIsListingOwner_True() {
        // Arrange
        when(listingRepository.findById(1L)).thenReturn(Optional.of(testListing));

        // Act
        boolean result = listingService.isListingOwner(1L, 1L);

        // Assert
        assertTrue(result);
        verify(listingRepository).findById(1L);
    }

    @Test
    void testIsListingOwner_False() {
        // Arrange
        when(listingRepository.findById(1L)).thenReturn(Optional.of(testListing));

        // Act
        boolean result = listingService.isListingOwner(1L, 999L);

        // Assert
        assertFalse(result);
        verify(listingRepository).findById(1L);
    }

    @Test
    void testIsListingOwner_ListingNotFound() {
        // Arrange
        when(listingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ListingNotFoundException exception = assertThrows(ListingNotFoundException.class, () -> {
            listingService.isListingOwner(999L, 1L);
        });

        // Assert
        assertEquals("Listing with ID 999 not found", exception.getMessage());
        verify(listingRepository).findById(999L);
    }

    // Edge cases and additional scenarios
    @Test
    void testCreateListing_WithNullImages() {
        // Arrange
        when(listingRepository.save(any(Listing.class))).thenReturn(testListing);

        // Act
        Listing result = listingService.createListing(
            "Test Item",
            "Test Description",
            new BigDecimal("99.99"),
            Category.ELECTRONICS,
            ItemCondition.GOOD,
            "Test Location",
            1L,
            null
        );

        // Assert
        assertNotNull(result);
        assertEquals("Test Item", result.getTitle());
        verify(listingRepository).save(any(Listing.class));
    }

    @Test
    void testUpdateListing_WithNullImages() {
        // Arrange
        when(listingRepository.findById(1L)).thenReturn(Optional.of(testListing));
        when(listingRepository.save(any(Listing.class))).thenReturn(testListing);

        // Act
        Listing result = listingService.updateListing(
            1L,
            "Updated Title",
            "Updated Description",
            new BigDecimal("149.99"),
            Category.TEXTBOOKS,
            ItemCondition.LIKE_NEW,
            "Updated Location",
            null
        );

        // Assert
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        verify(listingRepository).findById(1L);
        verify(listingRepository).save(any(Listing.class));
    }

    @Test
    void testAddImagesToListing_WithEmptyImages() {
        // Arrange
        List<ListingImage> emptyImages = new ArrayList<>();
        when(listingRepository.findById(1L)).thenReturn(Optional.of(testListing));
        when(listingRepository.save(any(Listing.class))).thenReturn(testListing);

        // Act
        Listing result = listingService.addImagesToListing(1L, emptyImages);

        // Assert
        assertNotNull(result);
        verify(listingRepository).findById(1L);
        verify(listingRepository).save(any(Listing.class));
    }

    @Test
    void testSearchListings_WithEmptyKeyword() {
        // Arrange
        String keyword = "";
        List<Listing> listings = Arrays.asList(testListing);
        Page<Listing> expectedPage = new PageImpl<>(listings);
        when(listingRepository.findByTitleOrDescriptionContaining(keyword, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Listing> result = listingService.searchListings(keyword, testPageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(listingRepository).findByTitleOrDescriptionContaining(keyword, testPageable);
    }

    @Test
    void testGetListingsWithFilters_WithNullParameters() {
        // Arrange
        List<Listing> listings = Arrays.asList(testListing);
        Page<Listing> expectedPage = new PageImpl<>(listings);
        when(listingRepository.findWithFilters(
            ListingStatus.ACTIVE, null, null, null, null, null, null, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Listing> result = listingService.getListingsWithFilters(
            ListingStatus.ACTIVE, null, null, null, null, null, null, testPageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(listingRepository).findWithFilters(
            ListingStatus.ACTIVE, null, null, null, null, null, null, testPageable);
    }

    // Test incrementViewCount method
    @Test
    void testIncrementViewCount_Success() {
        // Arrange
        testListing.setViewCount(5);
        testListing.setStatus(ListingStatus.ACTIVE);
        
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
        updatedListing.setViewCount(6); // Incremented view count
        updatedListing.setStatus(ListingStatus.ACTIVE);

        when(listingRepository.findById(1L)).thenReturn(Optional.of(testListing));
        when(listingRepository.save(any(Listing.class))).thenReturn(updatedListing);

        // Act
        int result = listingService.incrementViewCount(1L);

        // Assert
        assertEquals(6, result);
        verify(listingRepository).findById(1L);
        verify(listingRepository).save(any(Listing.class));
    }

    @Test
    void testIncrementViewCount_FromZero() {
        // Arrange
        testListing.setViewCount(0);
        testListing.setStatus(ListingStatus.ACTIVE);
        
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
        updatedListing.setViewCount(1); // Incremented from 0 to 1
        updatedListing.setStatus(ListingStatus.ACTIVE);

        when(listingRepository.findById(1L)).thenReturn(Optional.of(testListing));
        when(listingRepository.save(any(Listing.class))).thenReturn(updatedListing);

        // Act
        int result = listingService.incrementViewCount(1L);

        // Assert
        assertEquals(1, result);
        verify(listingRepository).findById(1L);
        verify(listingRepository).save(any(Listing.class));
    }

    @Test
    void testIncrementViewCount_ListingNotFound() {
        // Arrange
        when(listingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ListingNotFoundException exception = assertThrows(ListingNotFoundException.class, () -> {
            listingService.incrementViewCount(999L);
        });

        // Assert
        assertEquals("Listing with ID 999 not found", exception.getMessage());
        verify(listingRepository).findById(999L);
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    void testIncrementViewCount_WithInactiveListing() {
        // Arrange
        testListing.setViewCount(5);
        testListing.setStatus(ListingStatus.SOLD); // Inactive listing
        
        // For inactive listings, the view count should not increment according to the Listing model
        // The incrementViewCount method in Listing model checks if status is ACTIVE
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
        updatedListing.setViewCount(5); // View count should remain the same for inactive listings
        updatedListing.setStatus(ListingStatus.SOLD);

        when(listingRepository.findById(1L)).thenReturn(Optional.of(testListing));
        when(listingRepository.save(any(Listing.class))).thenReturn(updatedListing);

        // Act
        int result = listingService.incrementViewCount(1L);

        // Assert
        assertEquals(5, result); // View count should not increment for inactive listings
        verify(listingRepository).findById(1L);
        verify(listingRepository).save(any(Listing.class));
    }

    @Test
    void testIncrementViewCount_WithCancelledListing() {
        // Arrange
        testListing.setViewCount(3);
        testListing.setStatus(ListingStatus.CANCELLED); // Cancelled listing
        
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
        updatedListing.setViewCount(3); // View count should remain the same for cancelled listings
        updatedListing.setStatus(ListingStatus.CANCELLED);

        when(listingRepository.findById(1L)).thenReturn(Optional.of(testListing));
        when(listingRepository.save(any(Listing.class))).thenReturn(updatedListing);

        // Act
        int result = listingService.incrementViewCount(1L);

        // Assert
        assertEquals(3, result); // View count should not increment for cancelled listings
        verify(listingRepository).findById(1L);
        verify(listingRepository).save(any(Listing.class));
    }
}
