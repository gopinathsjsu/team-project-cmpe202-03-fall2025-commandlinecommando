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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

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
            @RequestParam("displayOrder") int displayOrder,
            Authentication authentication) {

        try {
            Listing listing = listingService.getListingById(listingId);
            
            if (listing == null) {
                return ResponseEntity.notFound().build();
            }
            // TODO: retrieve user id
            Long userId = 0L;
            if (!verifyUser(listing, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to upload files for this listing");
            }

            fileStorageService.storeFile(file, listing, displayOrder);
            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
        }
    }

    @PostMapping("/upload-multiple/{listingId}")
    public ResponseEntity<String> uploadMultipleFiles(
            @PathVariable Long listingId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("displayOrders") int[] displayOrders,
            Authentication authentication) {

        try {
            Listing listing = listingService.getListingById(listingId);
        
            if (files.isEmpty()) {
                return ResponseEntity.badRequest().body("No files provided");
            }
            if (listing == null) {
                return ResponseEntity.notFound().build();
            }
            // TODO: retrieve user id
            Long userId = 0L;
            if (!verifyUser(listing, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to upload files for this listing");
            }

            fileStorageService.storeFiles(files, listing, displayOrders);
            return ResponseEntity.ok("Files uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload files");
        }
    }

    @GetMapping("/listing/{listingId}")
    public ResponseEntity<?> getListingImages(@PathVariable Long listingId) {
        try {
            Listing listing = listingService.getListingById(listingId);
            if (listing == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(fileStorageService.getImagesByListing(listing));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get listing images: " + e.getMessage());
        }
    }

    @DeleteMapping("/listing/{listingId}/{imageId}")
    public ResponseEntity<String> deleteListingImage(@PathVariable Long listingId, @PathVariable Long imageId) {
        try {
            Listing listing = listingService.getListingById(listingId);

            if (listing == null) {
                return ResponseEntity.notFound().build();
            }

            // TODO: retrieve user id
            Long userId = 0L;
            if (!verifyUser(listing, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to delete listing images");
            }

            fileStorageService.deleteImageByListingImageId(imageId);
            return ResponseEntity.ok("Listing image deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete listing image: " + e.getMessage());
        }
    }

    // TODO: verify user id
    private boolean verifyUser(Listing listing, Long userId) {
        //return listing.getSellerId().equals(userId);
        return true;
    }
}