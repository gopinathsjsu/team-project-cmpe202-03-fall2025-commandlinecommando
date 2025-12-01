package com.commandlinecommandos.campusmarketplace.controller;

import com.commandlinecommandos.campusmarketplace.model.Product;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.repository.ProductRepository;
import com.commandlinecommandos.campusmarketplace.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * Controller for handling image uploads to S3.
 * Gracefully handles the case when S3 is not configured.
 */
@RestController
@RequestMapping("/images")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Images", description = "Image upload and management endpoints")
public class ImageUploadController {

    private static final Logger logger = LoggerFactory.getLogger(ImageUploadController.class);

    private final S3Service s3Service;
    private final ProductRepository productRepository;

    @Autowired
    public ImageUploadController(
            @Autowired(required = false) S3Service s3Service,
            ProductRepository productRepository) {
        this.s3Service = s3Service;
        this.productRepository = productRepository;
        if (s3Service == null) {
            logger.warn("S3Service not available - image upload endpoints will return errors");
        }
    }
    
    private boolean isS3Available() {
        return s3Service != null;
    }

    /**
     * Upload images for a listing
     */
    @PostMapping(value = "/upload/{listingId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Upload images for a listing", description = "Upload one or more images for a listing to S3")
    public ResponseEntity<?> uploadImages(
            @PathVariable UUID listingId,
            @RequestParam("files") List<MultipartFile> files,
            Authentication authentication) {
        
        if (!isS3Available()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Image upload service is not configured. Please contact administrator."));
        }
        
        try {
            logger.info("Uploading {} images for listing: {}", files.size(), listingId);

            // Verify the listing exists and user has permission
            Product product = productRepository.findById(listingId)
                    .orElseThrow(() -> new RuntimeException("Listing not found"));

            // Check if user owns this listing (unless admin)
            User user = (User) authentication.getPrincipal();
            if (!product.getSeller().getUserId().equals(user.getUserId()) && !user.isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You don't have permission to upload images for this listing"));
            }

            // Upload images to S3
            List<String> imageUrls = s3Service.uploadImages(files, listingId);

            // Update listing with new image URLs
            List<String> existingImages = product.getImageUrls();
            if (existingImages == null) {
                existingImages = new ArrayList<>();
            }
            existingImages.addAll(imageUrls);
            product.setImageUrls(existingImages);
            
            // Set the first image as primary if none exists
            if (product.getPrimaryImageUrl() == null && !imageUrls.isEmpty()) {
                product.setPrimaryImageUrl(imageUrls.get(0));
            }
            
            productRepository.save(product);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Images uploaded successfully");
            response.put("imageUrls", imageUrls);
            response.put("totalImages", existingImages.size());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid file upload: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error uploading images: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload images: " + e.getMessage()));
        }
    }

    /**
     * Upload images before listing creation (temporary upload)
     * Returns URLs that can be used when creating the listing
     */
    @PostMapping(value = "/upload/temp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Upload temporary images", description = "Upload images before creating a listing")
    public ResponseEntity<?> uploadTempImages(
            @RequestParam("files") List<MultipartFile> files,
            Authentication authentication) {
        
        if (!isS3Available()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Image upload service is not configured. Please contact administrator."));
        }
        
        try {
            logger.info("Uploading {} temporary images", files.size());

            // Generate a temporary listing ID for organizing the uploads
            UUID tempId = UUID.randomUUID();

            // Upload images to S3
            List<String> imageUrls = s3Service.uploadImages(files, tempId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Images uploaded successfully");
            response.put("imageUrls", imageUrls);
            response.put("tempId", tempId.toString());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid file upload: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error uploading images: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload images: " + e.getMessage()));
        }
    }

    /**
     * Delete an image from a listing
     */
    @DeleteMapping("/{listingId}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Delete image from listing", description = "Delete a specific image from a listing")
    public ResponseEntity<?> deleteImage(
            @PathVariable UUID listingId,
            @RequestParam("imageUrl") String imageUrl,
            Authentication authentication) {
        
        if (!isS3Available()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Image upload service is not configured. Please contact administrator."));
        }
        
        try {
            logger.info("Deleting image from listing: {}", listingId);

            // Verify the listing exists
            Product product = productRepository.findById(listingId)
                    .orElseThrow(() -> new RuntimeException("Listing not found"));

            // Check if user owns this listing
            User user = (User) authentication.getPrincipal();
            if (!product.getSeller().getUserId().equals(user.getUserId()) && !user.isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You don't have permission to delete images from this listing"));
            }

            // Delete from S3
            s3Service.deleteImage(imageUrl);

            // Update listing
            List<String> images = product.getImageUrls();
            if (images != null) {
                images.remove(imageUrl);
                product.setImageUrls(images);
                
                // Update primary image if deleted
                if (imageUrl.equals(product.getPrimaryImageUrl())) {
                    product.setPrimaryImageUrl(images.isEmpty() ? null : images.get(0));
                }
                
                productRepository.save(product);
            }

            return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));

        } catch (Exception e) {
            logger.error("Error deleting image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete image: " + e.getMessage()));
        }
    }

    /**
     * Set primary image for a listing
     */
    @PutMapping("/{listingId}/primary")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Set primary image", description = "Set a specific image as the primary image for a listing")
    public ResponseEntity<?> setPrimaryImage(
            @PathVariable UUID listingId,
            @RequestParam("imageUrl") String imageUrl,
            Authentication authentication) {
        
        try {
            Product product = productRepository.findById(listingId)
                    .orElseThrow(() -> new RuntimeException("Listing not found"));

            // Check ownership
            User user = (User) authentication.getPrincipal();
            if (!product.getSeller().getUserId().equals(user.getUserId()) && !user.isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You don't have permission to modify this listing"));
            }

            // Verify the image belongs to this listing
            List<String> images = product.getImageUrls();
            if (images == null || !images.contains(imageUrl)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Image not found in this listing"));
            }

            product.setPrimaryImageUrl(imageUrl);
            productRepository.save(product);

            return ResponseEntity.ok(Map.of(
                    "message", "Primary image updated successfully",
                    "primaryImageUrl", imageUrl
            ));

        } catch (Exception e) {
            logger.error("Error setting primary image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to set primary image: " + e.getMessage()));
        }
    }
}

