package com.commandlinecommandos.listingapi.service;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private ListingImageRepository listingImageRepository;

    public ListingImage storeFile(MultipartFile file, Listing listing, int displayOrder) {
        log.info("Storing file for listing ID: {} - filename: {}, size: {} bytes, displayOrder: {}", 
                listing.getListingId(), file.getOriginalFilename(), file.getSize(), displayOrder);
        
        try {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(fileName);
            String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
            
            log.debug("Generated unique filename: {} for original file: {}", uniqueFileName, fileName);
            
            Path targetLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(targetLocation);
            
            log.debug("Created upload directory: {}", targetLocation);

            Path filePath = targetLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            log.debug("File copied to: {}", filePath);

            ListingImage listingImage = new ListingImage(
                listing,
                filePath.toString(),
                fileName,
                displayOrder
            );

            ListingImage savedImage = listingImageRepository.save(listingImage);
            
            log.info("Successfully stored file for listing ID: {} - image ID: {}, filename: {}", 
                    listing.getListingId(), savedImage.getImageId(), fileName);
            
            return savedImage;

        } catch (IOException e) {
            log.error("Failed to store file for listing ID: {} - filename: {}, error: {}", 
                    listing.getListingId(), file.getOriginalFilename(), e.getMessage(), e);
            throw new FileStorageException("Could not store file " + file.getOriginalFilename() + ". Please try again!", e);
        }
    }

    public List<ListingImage> storeFiles(List<MultipartFile> files, Listing listing, int[] displayOrders) {
        log.info("Storing {} files for listing ID: {}", files.size(), listing.getListingId());
        
        if (files.isEmpty()) {
            log.warn("No files provided for storage for listing ID: {}", listing.getListingId());
            return new ArrayList<>();
        }
        
        if (displayOrders.length != files.size()) {
            log.error("Display orders count ({}) does not match files count ({}) for listing ID: {}", 
                    displayOrders.length, files.size(), listing.getListingId());
            throw new FileStorageException("Number of display orders does not match number of files");
        }

        List<ListingImage> storedImages = IntStream.range(0, files.size())
            .mapToObj(i -> storeFile(files.get(i), listing, displayOrders[i]))
            .collect(Collectors.toList());
            
        log.info("Successfully stored {} files for listing ID: {}", storedImages.size(), listing.getListingId());
        
        return storedImages;
    }

    public List<ListingImage> getImagesByListing(Listing listing) {
        log.debug("Retrieving images for listing ID: {}", listing.getListingId());
        
        List<ListingImage> images = listingImageRepository.findByListingOrderedByDisplayOrder(listing)
            .orElseThrow(() -> {
                log.warn("No images found for listing ID: {}", listing.getListingId());
                return new FileStorageException("Could not find images for listing " + listing.getListingId() + ". Please try again!");
            });
            
        log.debug("Retrieved {} images for listing ID: {}", images.size(), listing.getListingId());
        
        return images;
    }

    public ListingImage getPrimaryImageByListing(Listing listing) {
        log.debug("Retrieving primary image for listing ID: {}", listing.getListingId());
        
        ListingImage image = listingImageRepository.findPrimaryImageByListing(listing)
            .orElseThrow(() -> {
                log.warn("No primary image found for listing ID: {}", listing.getListingId());
                return new FileStorageException("Could not find primary image for listing " + listing.getListingId() + ". Please try again!");
            });
            
        log.debug("Retrieved primary image ID: {} for listing ID: {}", image.getImageId(), listing.getListingId());
        
        return image;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            log.debug("No file extension found for null or empty filename");
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            log.debug("No valid file extension found for filename: {}", fileName);
            return "";
        }

        String extension = fileName.substring(lastDotIndex + 1);
        log.debug("Extracted file extension: {} from filename: {}", extension, fileName);
        
        return extension;
    }

    public void deleteImageByListingImageId(Long id) {
        log.info("Deleting image with ID: {}", id);
        
        ListingImage listingImage = listingImageRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Image not found for deletion - ID: {}", id);
                return new FileStorageException("Could not find file with id " + id + ". Please try again!");
            });
        
        log.debug("Deleting file from filesystem: {}", listingImage.getImageUrl());
        
        try {
            boolean deleted = Files.deleteIfExists(Paths.get(listingImage.getImageUrl()));
            if (deleted) {
                log.debug("Successfully deleted file from filesystem: {}", listingImage.getImageUrl());
            } else {
                log.warn("File not found on filesystem: {}", listingImage.getImageUrl());
            }
        } catch (IOException e) {
            log.error("Failed to delete file from filesystem: {} - error: {}", 
                    listingImage.getImageUrl(), e.getMessage(), e);
            throw new FileStorageException("Could not delete file " + listingImage.getImageUrl() + ". Please try again!", e);
        }
        
        listingImageRepository.delete(listingImage);
        
        log.info("Successfully deleted image ID: {} from listing ID: {}", 
                id, listingImage.getListing().getListingId());
    }

    public void deleteAllImagesByListing(Listing listing) {
        log.info("Deleting all images for listing ID: {}", listing.getListingId());
        
        List<ListingImage> images = listingImageRepository.findByListing(listing)
            .orElseThrow(() -> {
                log.warn("No images found for listing ID: {} during bulk deletion", listing.getListingId());
                return new FileStorageException("Could not find images for listing " + listing.getListingId() + ". Please try again!");
            });

        log.debug("Found {} images to delete for listing ID: {}", images.size(), listing.getListingId());

        for (ListingImage image : images) {
            try {
                log.debug("Deleting file from filesystem: {}", image.getImageUrl());
                Files.delete(Paths.get(image.getImageUrl()));
                log.debug("Successfully deleted file: {}", image.getImageUrl());
            } catch (IOException e) {
                log.error("Failed to delete file: {} - error: {}", image.getImageUrl(), e.getMessage(), e);
                throw new FileStorageException("Could not delete file " + image.getImageUrl() + ". Please try again!", e);
            }
        }

        listingImageRepository.deleteByListing(listing);
        
        log.info("Successfully deleted all {} images for listing ID: {}", images.size(), listing.getListingId());
    }
}