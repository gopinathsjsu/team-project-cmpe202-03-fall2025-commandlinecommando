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
import com.commandlinecommandos.listingapi.exception.UnauthorizedAccessException;
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File cannot be null");
        }

        logger.info("Received file upload request - listing ID: {}, filename: '{}', size: {} bytes, displayOrder: {}", 
                   listingId, file.getOriginalFilename(), file.getSize(), displayOrder);
        
        try {
            Listing listing = listingService.getListingById(listingId);
            
            if (listing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Listing not found");
            }
            
            // TODO: retrieve user id
            Long userId = 0L;
            logger.debug("Using temporary user ID: {} for file upload authorization", userId);
            
            if (!verifyUser(listing, userId)) {
                throw new UnauthorizedAccessException("listing", listingId, userId);
            }

            fileStorageService.storeFile(file, listing, displayOrder);
            logger.info("Successfully uploaded file '{}' for listing ID: {} with display order: {}", 
                       file.getOriginalFilename(), listingId, displayOrder);
            
            return ResponseEntity.ok("File uploaded successfully");
        } catch (RuntimeException e) {
            logger.error("Error uploading file for listing ID: {}", listingId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
        }
    }

    @PostMapping("/upload-multiple/{listingId}")
    public ResponseEntity<String> uploadMultipleFiles(
            @PathVariable Long listingId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("displayOrders") int[] displayOrders) {

        if (files == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Files cannot be null");
        }

        if (files.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No files provided");
        }

        logger.info("Received multiple file upload request - listing ID: {}, fileCount: {}, totalSize: {} bytes", 
                   listingId, files.size(), files.stream().mapToLong(f -> f.getSize()).sum());
        
        try {
            Listing listing = listingService.getListingById(listingId);
            
            if (listing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Listing not found");
            }
            
            // TODO: retrieve user id
            Long userId = 0L;
            logger.debug("Using temporary user ID: {} for multiple file upload authorization", userId);
            
            if (!verifyUser(listing, userId)) {
                throw new UnauthorizedAccessException("listing", listingId, userId);
            }

            fileStorageService.storeFiles(files, listing, displayOrders);
            logger.info("Successfully uploaded {} files for listing ID: {}", files.size(), listingId);
            
            return ResponseEntity.ok("Files uploaded successfully");
        } catch (RuntimeException e) {
            logger.error("Error uploading multiple files for listing ID: {}", listingId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload files");
        }
    }

    @GetMapping("/listing/{listingId}")
    public ResponseEntity<?> getListingImages(@PathVariable Long listingId) {
        logger.info("Received request to get images for listing ID: {}", listingId);
        
        try {
            Listing listing = listingService.getListingById(listingId);
            
            if (listing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Listing not found");
            }
            
            List<ListingImage> images = fileStorageService.getImagesByListing(listing);
            logger.info("Successfully retrieved {} images for listing ID: {}", images.size(), listingId);
            
            return ResponseEntity.ok(images);
        } catch (RuntimeException e) {
            logger.error("Error retrieving images for listing ID: {}", listingId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get listing images");
        }
    }

    @DeleteMapping("/listing/{listingId}/{imageId}")
    public ResponseEntity<String> deleteListingImage(@PathVariable Long listingId, @PathVariable Long imageId) {
        logger.info("Received request to delete image ID: {} for listing ID: {}", imageId, listingId);
        
        try {
            Listing listing = listingService.getListingById(listingId);
            
            if (listing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Listing not found");
            }

            // TODO: retrieve user id
            Long userId = 0L;
            logger.debug("Using temporary user ID: {} for image deletion authorization", userId);
            
            if (!verifyUser(listing, userId)) {
                throw new UnauthorizedAccessException("listing", listingId, userId);
            }

            fileStorageService.deleteImageByListingImageId(imageId);
            logger.info("Successfully deleted image ID: {} for listing ID: {}", imageId, listingId);
            
            return ResponseEntity.ok("Listing image deleted successfully");
        } catch (RuntimeException e) {
            logger.error("Error deleting image ID: {} for listing ID: {}", imageId, listingId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete listing image");
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