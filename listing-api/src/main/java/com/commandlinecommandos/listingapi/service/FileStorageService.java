package com.commandlinecommandos.listingapi.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import com.commandlinecommandos.listingapi.repository.ListingImageRepository;
import com.commandlinecommandos.listingapi.model.Listing;
import com.commandlinecommandos.listingapi.model.ListingImage;
import com.commandlinecommandos.listingapi.exception.FileStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private ListingImageRepository listingImageRepository;

    public ListingImage storeFile(MultipartFile file, Listing listing, int displayOrder) {
        logger.debug("Storing file - listing ID: {}, filename: '{}', size: {} bytes, displayOrder: {}", 
                   listing.getListingId(), file.getOriginalFilename(), file.getSize(), displayOrder);
        
        try {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(fileName);
            String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
            
            logger.debug("Generated unique filename: '{}' for original file: '{}'", uniqueFileName, fileName);
            
            Path targetLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            logger.debug("Target upload directory: {}", targetLocation);
            
            Files.createDirectories(targetLocation);

            Path filePath = targetLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            logger.debug("File copied to: {}", filePath);

            ListingImage listingImage = new ListingImage(
                listing,
                filePath.toString(),
                fileName,
                displayOrder
            );

            ListingImage savedImage = listingImageRepository.save(listingImage);
            
            logger.info("Successfully stored file '{}' for listing ID: {} with image ID: {} and display order: {}", 
                       fileName, listing.getListingId(), savedImage.getImageId(), displayOrder);
            
            return savedImage;

        } catch (IOException e) {
            logger.error("Failed to store file '{}' for listing ID: {} - error: {}", 
                        file.getOriginalFilename(), listing.getListingId(), e.getMessage(), e);
            throw new FileStorageException("Could not store file " + file.getOriginalFilename() + ". Please try again!", e);
        }
    }

    public List<ListingImage> storeFiles(List<MultipartFile> files, Listing listing, int[] displayOrders) {
        logger.debug("Storing multiple files - listing ID: {}, fileCount: {}, totalSize: {} bytes", 
                   listing.getListingId(), files.size(), files.stream().mapToLong(f -> f.getSize()).sum());
        
        if (files.isEmpty()) {
            logger.warn("No files provided for storage for listing ID: {}", listing.getListingId());
            return new ArrayList<>();
        }
        
        if (displayOrders.length != files.size()) {
            logger.error("Display orders mismatch - expected: {}, provided: {} for listing ID: {}", 
                        files.size(), displayOrders.length, listing.getListingId());
            throw new FileStorageException("Number of display orders does not match number of files");
        }

        try {
            List<ListingImage> storedImages = IntStream.range(0, files.size())
                .mapToObj(i -> storeFile(files.get(i), listing, displayOrders[i]))
                .collect(Collectors.toList());
            
            logger.info("Successfully stored {} files for listing ID: {}", storedImages.size(), listing.getListingId());
            
            return storedImages;
        } catch (Exception e) {
            logger.error("Error storing multiple files for listing ID: {} - error: {}", 
                        listing.getListingId(), e.getMessage(), e);
            throw e;
        }
    }

    public List<ListingImage> getImagesByListing(Listing listing) {
        logger.debug("Retrieving images for listing ID: {}", listing.getListingId());
        
        try {
            List<ListingImage> images = listingImageRepository.findByListingOrderedByDisplayOrder(listing)
                .orElseThrow(() -> new FileStorageException("Could not find images for listing " + listing.getListingId() + ". Please try again!"));
            
            logger.debug("Successfully retrieved {} images for listing ID: {}", images.size(), listing.getListingId());
            
            return images;
        } catch (FileStorageException e) {
            logger.warn("No images found for listing ID: {}", listing.getListingId());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving images for listing ID: {} - error: {}", 
                        listing.getListingId(), e.getMessage(), e);
            throw e;
        }
    }

    public ListingImage getPrimaryImageByListing(Listing listing) {
        ListingImage image = listingImageRepository.findPrimaryImageByListing(listing)
            .orElseThrow(() -> new FileStorageException("Could not find primary image for listing " + listing.getListingId() + ". Please try again!"));
        return image;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(lastDotIndex + 1);
    }

    public void deleteImageByListingImageId(Long id) {
        logger.debug("Deleting image by ID: {}", id);
        
        try {
            ListingImage listingImage = listingImageRepository.findById(id)
                .orElseThrow(() -> new FileStorageException("Could not find file with id " + id + ". Please try again!"));
            
            logger.debug("Found image to delete - ID: {}, filename: '{}', listing ID: {}, file path: '{}'", 
                        id, listingImage.getImageUrl(), listingImage.getListing().getListingId(), listingImage.getImageUrl());
            
            try {
                boolean deleted = Files.deleteIfExists(Paths.get(listingImage.getImageUrl()));
                logger.debug("File deletion result: {} for path: '{}'", deleted, listingImage.getImageUrl());
            } catch (IOException e) {
                logger.error("Failed to delete file from filesystem - path: '{}', error: {}", 
                            listingImage.getImageUrl(), e.getMessage(), e);
                throw new FileStorageException("Could not delete file " + listingImage.getImageUrl() + ". Please try again!", e);
            }
            
            listingImageRepository.delete(listingImage);
            
            logger.info("Successfully deleted image ID: {} ('{}') for listing ID: {}", 
                       id, listingImage.getImageUrl(), listingImage.getListing().getListingId());
            
        } catch (FileStorageException e) {
            logger.warn("Image not found for deletion - ID: {}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting image ID: {} - error: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    public void deleteAllImagesByListing(Listing listing) {
        logger.debug("Deleting all images for listing ID: {}", listing.getListingId());
        
        try {
            List<ListingImage> images = listingImageRepository.findByListing(listing)
                .orElseThrow(() -> new FileStorageException("Could not find images for listing " + listing.getListingId() + ". Please try again!"));

            logger.debug("Found {} images to delete for listing ID: {}", images.size(), listing.getListingId());

            int deletedCount = 0;
            for (ListingImage image : images) {
                try {
                    boolean deleted = Files.deleteIfExists(Paths.get(image.getImageUrl()));
                    if (deleted) {
                        deletedCount++;
                        logger.debug("Deleted file: '{}'", image.getImageUrl());
                    } else {
                        // File doesn't exist or couldn't be deleted
                        logger.error("Could not delete file: '{}' - file may not exist or access denied", image.getImageUrl());
                        throw new FileStorageException("Could not delete file " + image.getImageUrl() + ". Please try again!", new IOException("File does not exist or cannot be deleted"));
                    }
                } catch (IOException e) {
                    logger.error("Failed to delete file: '{}' - error: {}", image.getImageUrl(), e.getMessage(), e);
                    throw new FileStorageException("Could not delete file " + image.getImageUrl() + ". Please try again!", e);
                }
            }

            listingImageRepository.deleteByListing(listing);
            
            logger.info("Successfully deleted all {} images for listing ID: {} ({} files deleted from filesystem)", 
                       images.size(), listing.getListingId(), deletedCount);
            
        } catch (FileStorageException e) {
            logger.warn("No images found for deletion for listing ID: {}", listing.getListingId());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting all images for listing ID: {} - error: {}", 
                        listing.getListingId(), e.getMessage(), e);
            throw e;
        }
    }
}