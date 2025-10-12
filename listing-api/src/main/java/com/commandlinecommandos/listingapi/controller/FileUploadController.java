package com.commandlinecommandos.listingapi.controller;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ListingService listingService;

    @PostMapping("/upload/{listingId}")
    public ResponseEntity<String> uploadFile(
            @PathVariable Long listingId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("displayOrder") int displayOrder) {

        if (file == null) {
            logger.warn("Null file provided for upload to listing ID: {}", listingId);
            return ResponseEntity.badRequest().body("File cannot be null");
        }

        logger.info("Received file upload request - listing ID: {}, filename: '{}', size: {} bytes, displayOrder: {}", 
                   listingId, file.getOriginalFilename(), file.getSize(), displayOrder);
        
        try {
            Listing listing = listingService.getListingById(listingId);
            
            if (listing == null) {
                logger.warn("Listing not found for file upload - ID: {}", listingId);
                return ResponseEntity.notFound().build();
            }
            
            // TODO: retrieve user id
            Long userId = 0L;
            logger.debug("Using temporary user ID: {} for file upload authorization", userId);
            
            if (!verifyUser(listing, userId)) {
                logger.warn("Unauthorized file upload attempt - listing ID: {}, user ID: {}", listingId, userId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to upload files for this listing");
            }

            fileStorageService.storeFile(file, listing, displayOrder);
            logger.info("Successfully uploaded file '{}' for listing ID: {} with display order: {}", 
                       file.getOriginalFilename(), listingId, displayOrder);
            
            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            logger.error("Error uploading file '{}' for listing ID: {} - error: {}", 
                        file.getOriginalFilename(), listingId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
        }
    }

    @PostMapping("/upload-multiple/{listingId}")
    public ResponseEntity<String> uploadMultipleFiles(
            @PathVariable Long listingId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("displayOrders") int[] displayOrders) {

        if (files == null) {
            logger.warn("Null files list provided for upload to listing ID: {}", listingId);
            return ResponseEntity.badRequest().body("Files cannot be null");
        }

        logger.info("Received multiple file upload request - listing ID: {}, fileCount: {}, totalSize: {} bytes", 
                   listingId, files.size(), files.stream().mapToLong(f -> f.getSize()).sum());
        
        try {
            if (files.isEmpty()) {
                logger.warn("No files provided for upload to listing ID: {}", listingId);
                return ResponseEntity.badRequest().body("No files provided");
            }
            
            Listing listing = listingService.getListingById(listingId);
            
            if (listing == null) {
                logger.warn("Listing not found for multiple file upload - ID: {}", listingId);
                return ResponseEntity.notFound().build();
            }
            
            // TODO: retrieve user id
            Long userId = 0L;
            logger.debug("Using temporary user ID: {} for multiple file upload authorization", userId);
            
            if (!verifyUser(listing, userId)) {
                logger.warn("Unauthorized multiple file upload attempt - listing ID: {}, user ID: {}", listingId, userId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to upload files for this listing");
            }

            fileStorageService.storeFiles(files, listing, displayOrders);
            logger.info("Successfully uploaded {} files for listing ID: {}", files.size(), listingId);
            
            return ResponseEntity.ok("Files uploaded successfully");
        } catch (Exception e) {
            logger.error("Error uploading multiple files for listing ID: {} - error: {}", 
                        listingId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload files");
        }
    }

    @GetMapping("/listing/{listingId}")
    public ResponseEntity<?> getListingImages(@PathVariable Long listingId) {
        logger.info("Received request to get images for listing ID: {}", listingId);
        
        try {
            Listing listing = listingService.getListingById(listingId);
            if (listing == null) {
                logger.warn("Listing not found for image retrieval - ID: {}", listingId);
                return ResponseEntity.notFound().build();
            }
            
            List<ListingImage> images = fileStorageService.getImagesByListing(listing);
            logger.info("Successfully retrieved {} images for listing ID: {}", images.size(), listingId);
            
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            logger.error("Error retrieving images for listing ID: {} - error: {}", 
                        listingId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get listing images: " + e.getMessage());
        }
    }

    @DeleteMapping("/listing/{listingId}/{imageId}")
    public ResponseEntity<String> deleteListingImage(@PathVariable Long listingId, @PathVariable Long imageId) {
        logger.info("Received request to delete image ID: {} for listing ID: {}", imageId, listingId);
        
        try {
            Listing listing = listingService.getListingById(listingId);

            if (listing == null) {
                logger.warn("Listing not found for image deletion - ID: {}", listingId);
                return ResponseEntity.notFound().build();
            }

            // TODO: retrieve user id
            Long userId = 0L;
            logger.debug("Using temporary user ID: {} for image deletion authorization", userId);
            
            if (!verifyUser(listing, userId)) {
                logger.warn("Unauthorized image deletion attempt - listing ID: {}, user ID: {}, image ID: {}", 
                           listingId, userId, imageId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to delete listing images");
            }

            fileStorageService.deleteImageByListingImageId(imageId);
            logger.info("Successfully deleted image ID: {} for listing ID: {}", imageId, listingId);
            
            return ResponseEntity.ok("Listing image deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting image ID: {} for listing ID: {} - error: {}", 
                        imageId, listingId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete listing image: " + e.getMessage());
        }
    }

    // TODO: verify user id
    private boolean verifyUser(Listing listing, Long userId) {
        logger.debug("Verifying user access - listing ID: {}, seller ID: {}, user ID: {}, verified: {}", 
                   listing.getListingId(), listing.getSellerId(), userId, true);
        //return listing.getSellerId().equals(userId);
        return true;
    }
}