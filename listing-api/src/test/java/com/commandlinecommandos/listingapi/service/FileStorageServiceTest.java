package com.commandlinecommandos.listingapi.service;

import com.commandlinecommandos.listingapi.exception.FileStorageException;
import com.commandlinecommandos.listingapi.model.Category;
import com.commandlinecommandos.listingapi.model.ItemCondition;
import com.commandlinecommandos.listingapi.model.Listing;
import com.commandlinecommandos.listingapi.model.ListingImage;
import com.commandlinecommandos.listingapi.model.ListingStatus;
import com.commandlinecommandos.listingapi.repository.ListingImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private ListingImageRepository listingImageRepository;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private MultipartFile multipartFile2;

    @InjectMocks
    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    private Listing testListing;
    private String testUploadDir;

    @BeforeEach
    void setUp() {
        testUploadDir = tempDir.toString();
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", testUploadDir);
        
        // Create a test listing
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
    }

    @Test
    void testStoreFile_Success() throws IOException {
        // Arrange
        String originalFileName = "test-image.jpg";
        String fileContent = "test image content";
        int displayOrder = 1;
        
        when(multipartFile.getOriginalFilename()).thenReturn(originalFileName);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent.getBytes()));
        
        ListingImage savedImage = new ListingImage(testListing, "path/to/file", originalFileName, displayOrder);
        when(listingImageRepository.save(any(ListingImage.class))).thenReturn(savedImage);

        // Act
        ListingImage result = fileStorageService.storeFile(multipartFile, testListing, displayOrder);

        // Assert
        assertNotNull(result);
        assertEquals(testListing, result.getListing());
        assertEquals(originalFileName, result.getAltText());
        assertEquals(displayOrder, result.getDisplayOrder());
        assertTrue(result.getImageUrl().contains(testUploadDir));
        assertTrue(result.getImageUrl().endsWith(".jpg"));
        
        // Verify file was actually created
        Path expectedFilePath = Paths.get(result.getImageUrl());
        assertTrue(Files.exists(expectedFilePath));
        
        verify(listingImageRepository).save(any(ListingImage.class));
    }

    @Test
    void testStoreFile_WithEmptyFileName() throws IOException {
        // Arrange
        String originalFileName = "";
        String fileContent = "test image content";
        int displayOrder = 1;
        
        when(multipartFile.getOriginalFilename()).thenReturn(originalFileName);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent.getBytes()));
        
        ListingImage savedImage = new ListingImage(testListing, "path/to/file", originalFileName, displayOrder);
        when(listingImageRepository.save(any(ListingImage.class))).thenReturn(savedImage);

        // Act
        ListingImage result = fileStorageService.storeFile(multipartFile, testListing, displayOrder);

        // Assert
        assertNotNull(result);
        assertTrue(result.getImageUrl().endsWith(".")); // No extension
        verify(listingImageRepository).save(any(ListingImage.class));
    }

    @Test
    void testStoreFile_WithNullFileName() throws IOException {
        // Arrange
        String fileContent = "test image content";
        int displayOrder = 1;
        
        when(multipartFile.getOriginalFilename()).thenReturn(null);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent.getBytes()));
        
        ListingImage savedImage = new ListingImage(testListing, "path/to/file", null, displayOrder);
        when(listingImageRepository.save(any(ListingImage.class))).thenReturn(savedImage);

        // Act
        ListingImage result = fileStorageService.storeFile(multipartFile, testListing, displayOrder);

        // Assert
        assertNotNull(result);
        assertTrue(result.getImageUrl().endsWith(".")); // No extension
        verify(listingImageRepository).save(any(ListingImage.class));
    }

    @Test
    void testStoreFile_WithMultipleDotsInFileName() throws IOException {
        // Arrange
        String originalFileName = "test.image.file.png";
        String fileContent = "test image content";
        int displayOrder = 1;
        
        when(multipartFile.getOriginalFilename()).thenReturn(originalFileName);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent.getBytes()));
        
        ListingImage savedImage = new ListingImage(testListing, "path/to/file", originalFileName, displayOrder);
        when(listingImageRepository.save(any(ListingImage.class))).thenReturn(savedImage);

        // Act
        ListingImage result = fileStorageService.storeFile(multipartFile, testListing, displayOrder);

        // Assert
        assertNotNull(result);
        assertTrue(result.getImageUrl().endsWith(".png"));
        verify(listingImageRepository).save(any(ListingImage.class));
    }

    @Test
    void testStoreFile_WithNoExtension() throws IOException {
        // Arrange
        String originalFileName = "testfile";
        String fileContent = "test image content";
        int displayOrder = 1;
        
        when(multipartFile.getOriginalFilename()).thenReturn(originalFileName);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent.getBytes()));
        
        ListingImage savedImage = new ListingImage(testListing, "path/to/file", originalFileName, displayOrder);
        when(listingImageRepository.save(any(ListingImage.class))).thenReturn(savedImage);

        // Act
        ListingImage result = fileStorageService.storeFile(multipartFile, testListing, displayOrder);

        // Assert
        assertNotNull(result);
        assertTrue(result.getImageUrl().endsWith(".")); // No extension
        verify(listingImageRepository).save(any(ListingImage.class));
    }

    @Test
    void testStoreFile_IOException() throws IOException {
        // Arrange
        String originalFileName = "test-image.jpg";
        int displayOrder = 1;
        
        when(multipartFile.getOriginalFilename()).thenReturn(originalFileName);
        when(multipartFile.getInputStream()).thenThrow(new IOException("Test IO exception"));

        // Act & Assert
        FileStorageException exception = assertThrows(FileStorageException.class, () -> {
            fileStorageService.storeFile(multipartFile, testListing, displayOrder);
        });
        
        assertTrue(exception.getMessage().contains("Could not store file"));
        assertTrue(exception.getMessage().contains(originalFileName));
        assertTrue(exception.getCause() instanceof IOException);
        
        verify(listingImageRepository, never()).save(any(ListingImage.class));
    }

    @Test
    void testStoreFile_CreatesUploadDirectory() throws IOException {
        // Arrange
        String originalFileName = "test-image.jpg";
        String fileContent = "test image content";
        int displayOrder = 1;
        
        // Use a subdirectory that doesn't exist
        String subDir = testUploadDir + "/subdir";
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", subDir);
        
        when(multipartFile.getOriginalFilename()).thenReturn(originalFileName);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent.getBytes()));
        
        ListingImage savedImage = new ListingImage(testListing, "path/to/file", originalFileName, displayOrder);
        when(listingImageRepository.save(any(ListingImage.class))).thenReturn(savedImage);

        // Act
        fileStorageService.storeFile(multipartFile, testListing, displayOrder);

        // Assert
        assertTrue(Files.exists(Paths.get(subDir)));
        verify(listingImageRepository).save(any(ListingImage.class));
    }

    @Test
    void testStoreFile_GeneratesUniqueFileName() throws IOException {
        // Arrange
        String originalFileName = "test-image.jpg";
        String fileContent = "test image content";
        int displayOrder = 1;
        
        when(multipartFile.getOriginalFilename()).thenReturn(originalFileName);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent.getBytes()));
        
        ListingImage savedImage = new ListingImage(testListing, "path/to/file", originalFileName, displayOrder);
        when(listingImageRepository.save(any(ListingImage.class))).thenReturn(savedImage);

        // Act
        ListingImage result1 = fileStorageService.storeFile(multipartFile, testListing, displayOrder);
        ListingImage result2 = fileStorageService.storeFile(multipartFile, testListing, displayOrder);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1.getImageUrl(), result2.getImageUrl());
        
        // Both should end with .jpg but have different UUIDs
        assertTrue(result1.getImageUrl().endsWith(".jpg"));
        assertTrue(result2.getImageUrl().endsWith(".jpg"));
        
        verify(listingImageRepository, times(2)).save(any(ListingImage.class));
    }

    @Test
    void testStoreFile_WithCleanPath() throws IOException {
        // Arrange
        String originalFileName = "../../../test-image.jpg"; // Path traversal attempt
        String fileContent = "test image content";
        int displayOrder = 1;
        
        when(multipartFile.getOriginalFilename()).thenReturn(originalFileName);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent.getBytes()));
        
        ListingImage savedImage = new ListingImage(testListing, "path/to/file", "test-image.jpg", displayOrder);
        when(listingImageRepository.save(any(ListingImage.class))).thenReturn(savedImage);

        // Act
        ListingImage result = fileStorageService.storeFile(multipartFile, testListing, displayOrder);

        // Assert
        assertNotNull(result);
        assertEquals("test-image.jpg", result.getAltText()); // Should be cleaned
        assertTrue(result.getImageUrl().endsWith(".jpg"));
        verify(listingImageRepository).save(any(ListingImage.class));
    }

    @Test
    void testStoreFiles_Success() throws IOException {
        // Arrange
        String originalFileName1 = "test-image1.jpg";
        String originalFileName2 = "test-image2.png";
        String fileContent = "test image content";
        int[] displayOrders = {1, 2};
        
        List<MultipartFile> files = Arrays.asList(multipartFile, multipartFile2);
        
        when(multipartFile.getOriginalFilename()).thenReturn(originalFileName1);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent.getBytes()));
        when(multipartFile2.getOriginalFilename()).thenReturn(originalFileName2);
        when(multipartFile2.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent.getBytes()));
        
        ListingImage savedImage1 = new ListingImage(testListing, "path/to/file1", originalFileName1, displayOrders[0]);
        ListingImage savedImage2 = new ListingImage(testListing, "path/to/file2", originalFileName2, displayOrders[1]);
        
        when(listingImageRepository.save(any(ListingImage.class)))
            .thenReturn(savedImage1)
            .thenReturn(savedImage2);

        // Act
        List<ListingImage> results = fileStorageService.storeFiles(files, testListing, displayOrders);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(originalFileName1, results.get(0).getAltText());
        assertEquals(originalFileName2, results.get(1).getAltText());
        
        verify(listingImageRepository, times(2)).save(any(ListingImage.class));
    }

    @Test
    void testStoreFiles_EmptyList() {
        // Arrange
        List<MultipartFile> files = new ArrayList<>();
        int[] displayOrders = {1, 2};

        // Act
        List<ListingImage> results = fileStorageService.storeFiles(files, testListing, displayOrders);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(listingImageRepository, never()).save(any(ListingImage.class));
    }

    @Test
    void testGetImagesByListing_Success() {
        // Arrange
        ListingImage image1 = new ListingImage(testListing, "path1", "image1.jpg", 1);
        ListingImage image2 = new ListingImage(testListing, "path2", "image2.jpg", 2);
        List<ListingImage> expectedImages = Arrays.asList(image1, image2);
        
        when(listingImageRepository.findByListingOrderedByDisplayOrder(testListing))
            .thenReturn(Optional.of(expectedImages));

        // Act
        List<ListingImage> results = fileStorageService.getImagesByListing(testListing);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(expectedImages, results);
        verify(listingImageRepository).findByListingOrderedByDisplayOrder(testListing);
    }

    @Test
    void testGetImagesByListing_EmptyResult() {
        // Arrange
        when(listingImageRepository.findByListingOrderedByDisplayOrder(testListing))
            .thenReturn(Optional.empty());

        // Act
        FileStorageException exception = assertThrows(FileStorageException.class, () -> {
            fileStorageService.getImagesByListing(testListing);
        });
        
        // Assert
        assertTrue(exception.getMessage().contains("Could not find images for listing"));
        verify(listingImageRepository).findByListingOrderedByDisplayOrder(testListing);
    }

    @Test
    void testGetPrimaryImageByListing_Success() {
        // Arrange
        ListingImage primaryImage = new ListingImage(testListing, "path", "primary.jpg", 1);
        
        when(listingImageRepository.findPrimaryImageByListing(testListing))
            .thenReturn(Optional.of(primaryImage));

        // Act
        ListingImage result = fileStorageService.getPrimaryImageByListing(testListing);

        // Assert
        assertNotNull(result);
        assertEquals(primaryImage, result);
        verify(listingImageRepository).findPrimaryImageByListing(testListing);
    }

    @Test
    void testGetPrimaryImageByListing_NullResult() {
        // Arrange
        when(listingImageRepository.findPrimaryImageByListing(testListing))
            .thenReturn(Optional.empty());

        // Act 
        FileStorageException exception = assertThrows(FileStorageException.class, () -> {
            fileStorageService.getPrimaryImageByListing(testListing);
        });
        
        // Assert
        assertTrue(exception.getMessage().contains("Could not find primary image for listing"));
        verify(listingImageRepository).findPrimaryImageByListing(testListing);
    }


    @Test
    void testDeleteImageByListingImageId_Success() throws IOException {
        // Arrange
        Long imageId = 1L;
        String filePath = testUploadDir + "/test-file.jpg";
        Path testFile = Paths.get(filePath);
        Files.createFile(testFile); // Create actual file
        
        ListingImage listingImage = new ListingImage(testListing, filePath, "test-file.jpg", 1);
        
        when(listingImageRepository.findById(imageId))
            .thenReturn(Optional.of(listingImage));

        // Act
        fileStorageService.deleteImageByListingImageId(imageId);

        // Assert
        assertFalse(Files.exists(testFile));
        verify(listingImageRepository).findById(imageId);
        verify(listingImageRepository).delete(listingImage);
    }

    @Test
    void testDeleteImageByListingImageId_ImageNotFound() {
        // Arrange
        Long imageId = 999L;
        
        when(listingImageRepository.findById(imageId))
            .thenReturn(Optional.empty());

        // Act
        FileStorageException exception = assertThrows(FileStorageException.class, () -> {
            fileStorageService.deleteImageByListingImageId(imageId);
        });
        
        // Assert
        assertTrue(exception.getMessage().contains("Could not find file with id"));
        assertTrue(exception.getMessage().contains(imageId.toString()));
        
        verify(listingImageRepository).findById(imageId);
        verify(listingImageRepository, never()).delete(any(ListingImage.class));
    }

    @Test
    void testDeleteAllImagesByListing_Success() throws IOException {
        // Arrange
        String filePath1 = testUploadDir + "/test-file1.jpg";
        String filePath2 = testUploadDir + "/test-file2.png";
        Path testFile1 = Paths.get(filePath1);
        Path testFile2 = Paths.get(filePath2);
        Files.createFile(testFile1); // Create actual files
        Files.createFile(testFile2);
        
        ListingImage image1 = new ListingImage(testListing, filePath1, "test-file1.jpg", 1);
        ListingImage image2 = new ListingImage(testListing, filePath2, "test-file2.png", 2);
        List<ListingImage> images = Arrays.asList(image1, image2);
        
        when(listingImageRepository.findByListing(testListing))
            .thenReturn(Optional.of(images));

        // Act
        fileStorageService.deleteAllImagesByListing(testListing);

        // Assert
        assertFalse(Files.exists(testFile1));
        assertFalse(Files.exists(testFile2));
        verify(listingImageRepository).findByListing(testListing);
        verify(listingImageRepository).deleteByListing(testListing);
    }

    @Test
    void testDeleteAllImagesByListing_EmptyList() {
        // Arrange
        when(listingImageRepository.findByListing(testListing))
            .thenReturn(Optional.empty());

        // Act
        FileStorageException exception = assertThrows(FileStorageException.class, () -> {
            fileStorageService.deleteAllImagesByListing(testListing);
        });
        
        // Assert
        assertTrue(exception.getMessage().contains("Could not find images for listing"));
        verify(listingImageRepository).findByListing(testListing);
        verify(listingImageRepository, never()).deleteByListing(any(Listing.class));
    }

    @Test
    void testDeleteAllImagesByListing_FileDeleteFailure() throws IOException {
        // Arrange
        String filePath1 = testUploadDir + "/test-file1.jpg";
        String filePath2 = testUploadDir + "/test-file2.png";
        Path testFile1 = Paths.get(filePath1);
        Files.createFile(testFile1); // Create only one file
        
        ListingImage image1 = new ListingImage(testListing, filePath1, "test-file1.jpg", 1);
        ListingImage image2 = new ListingImage(testListing, filePath2, "test-file2.png", 2);
        List<ListingImage> images = Arrays.asList(image1, image2);
        
        when(listingImageRepository.findByListing(testListing))
            .thenReturn(Optional.of(images));

        // Act
        FileStorageException exception = assertThrows(FileStorageException.class, () -> {
            fileStorageService.deleteAllImagesByListing(testListing);
        });
        
        // Assert
        assertTrue(exception.getMessage().contains("Could not delete file"));
        assertTrue(exception.getMessage().contains("test-file2.png"));
        assertTrue(exception.getCause() instanceof IOException);
        
        verify(listingImageRepository).findByListing(testListing);
        verify(listingImageRepository, never()).deleteByListing(any(Listing.class));
    }
}
