package com.commandlinecommandos.listingapi.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;
import com.commandlinecommandos.listingapi.model.Listing;
import com.commandlinecommandos.listingapi.model.ListingImage;
import com.commandlinecommandos.listingapi.service.FileStorageService;
import com.commandlinecommandos.listingapi.service.ListingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ListingService listingService;

    @PostMapping("/upload/{listingId}")
    public ResponseEntity<String> uploadFile(
            @PathVariable Long listingId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("displayOrder") int displayOrder) {

        log.info("Uploading single file for listing ID: {} - filename: {}, size: {} bytes, displayOrder: {}", 
                listingId, file.getOriginalFilename(), file.getSize(), displayOrder);
        
        try {
            Listing listing = listingService.getListingById(listingId);
            
            if (listing == null) {
                log.warn("Listing not found for file upload - listing ID: {}", listingId);
                return ResponseEntity.notFound().build();
            }
            // TODO: retrieve user id
            Long userId = 0L;
            if (!verifyUser(listing, userId)) {
                log.warn("Unauthorized file upload attempt - listing ID: {}, user: {}", listingId, userId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to upload files for this listing");
            }

            fileStorageService.storeFile(file, listing, displayOrder);
            
            log.info("Successfully uploaded file for listing ID: {} - filename: {}", 
                    listingId, file.getOriginalFilename());
            
            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            log.error("Failed to upload file for listing ID: {} - filename: {}, error: {}", 
                    listingId, file.getOriginalFilename(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
        }
    }

    @PostMapping("/upload-multiple/{listingId}")
    public ResponseEntity<String> uploadMultipleFiles(
            @PathVariable Long listingId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("displayOrders") int[] displayOrders) {

        log.info("Uploading {} files for listing ID: {}", files.size(), listingId);
        
        try {
            Listing listing = listingService.getListingById(listingId);
        
            if (files.isEmpty()) {
                log.warn("No files provided for upload to listing ID: {}", listingId);
                return ResponseEntity.badRequest().body("No files provided");
            }
            if (listing == null) {
                log.warn("Listing not found for multiple file upload - listing ID: {}", listingId);
                return ResponseEntity.notFound().build();
            }
            // TODO: retrieve user id
            Long userId = 0L;
            if (!verifyUser(listing, userId)) {
                log.warn("Unauthorized multiple file upload attempt - listing ID: {}, user: {}", listingId, userId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to upload files for this listing");
            }

            fileStorageService.storeFiles(files, listing, displayOrders);
            
            log.info("Successfully uploaded {} files for listing ID: {}", files.size(), listingId);
            
            return ResponseEntity.ok("Files uploaded successfully");
        } catch (Exception e) {
            log.error("Failed to upload multiple files for listing ID: {} - error: {}", 
                    listingId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload files");
        }
    }

    @GetMapping("/listing/{listingId}")
    public ResponseEntity<?> getListingImages(@PathVariable Long listingId) {
        log.info("Getting images for listing ID: {}", listingId);
        
        try {
            Listing listing = listingService.getListingById(listingId);
            if (listing == null) {
                log.warn("Listing not found when retrieving images - listing ID: {}", listingId);
                return ResponseEntity.notFound().build();
            }
            
            var images = fileStorageService.getImagesByListing(listing);
            
            log.info("Successfully retrieved {} images for listing ID: {}", 
                    images.size(), listingId);
            
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            log.error("Failed to get images for listing ID: {} - error: {}", 
                    listingId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get listing images: " + e.getMessage());
        }
    }

    @DeleteMapping("/listing/{listingId}/{imageId}")
    public ResponseEntity<String> deleteListingImage(@PathVariable Long listingId, @PathVariable Long imageId) {
        log.info("Deleting image ID: {} from listing ID: {}", imageId, listingId);
        
        try {
            Listing listing = listingService.getListingById(listingId);

            if (listing == null) {
                log.warn("Listing not found when deleting image - listing ID: {}", listingId);
                return ResponseEntity.notFound().build();
            }

            // TODO: retrieve user id
            Long userId = 0L;
            if (!verifyUser(listing, userId)) {
                log.warn("Unauthorized image deletion attempt - listing ID: {}, image ID: {}, user: {}", 
                        listingId, imageId, userId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to delete listing images");
            }

            fileStorageService.deleteImageByListingImageId(imageId);
            
            log.info("Successfully deleted image ID: {} from listing ID: {}", imageId, listingId);
            
            return ResponseEntity.ok("Listing image deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete image ID: {} from listing ID: {} - error: {}", 
                    imageId, listingId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete listing image: " + e.getMessage());
        }
    }

    // TODO: verify user id
    private boolean verifyUser(Listing listing, Long userId) {
        //return listing.getSellerId().equals(userId);
        return true;
    }
}