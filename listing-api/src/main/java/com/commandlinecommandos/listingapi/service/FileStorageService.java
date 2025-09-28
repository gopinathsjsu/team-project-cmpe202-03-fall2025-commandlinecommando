package com.commandlinecommandos.listingapi.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
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

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private ListingImageRepository listingImageRepository;

    public ListingImage storeFile(MultipartFile file, Listing listing, int displayOrder) {
        try {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(fileName);
            String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
            
            Path targetLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(targetLocation);

            Path filePath = targetLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            ListingImage listingImage = new ListingImage(
                listing,
                filePath.toString(),
                fileName,
                displayOrder
            );

            return listingImageRepository.save(listingImage);

        } catch (IOException e) {
            throw new FileStorageException("Could not store file " + file.getOriginalFilename() + ". Please try again!", e);
        }
    }

    public List<ListingImage> storeFiles(List<MultipartFile> files, Listing listing, int displayOrder) {
        return files.stream()
            .map(file -> storeFile(file, listing, displayOrder))
            .collect(Collectors.toList());
    }

    public List<ListingImage> getImagesByListing(Listing listing) {
        List<ListingImage> images =listingImageRepository.findByListingOrderedByDisplayOrder(listing)
            .orElseThrow(() -> new FileStorageException("Could not find images for listing " + listing.getListingId() + ". Please try again!"));
        return images;
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
        ListingImage listingImage = listingImageRepository.findById(id)
            .orElseThrow(() -> new FileStorageException("Could not find file with id " + id + ". Please try again!"));
        
        try {
            Files.deleteIfExists(Paths.get(listingImage.getImageUrl()));
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file " + listingImage.getImageUrl(), e);
        }
        
        listingImageRepository.delete(listingImage);
    }

    public void deleteAllImagesByListing(Listing listing) {
        List<ListingImage> images = listingImageRepository.findByListing(listing)
            .orElseThrow(() -> new FileStorageException("Could not find images for listing " + listing.getListingId() + ". Please try again!"));

        for (ListingImage image : images) {
            try {
                Files.delete(Paths.get(image.getImageUrl()));
            } catch (IOException e) {
                throw new FileStorageException("Could not delete file " + image.getImageUrl() + ". Please try again!", e);
            }
        }

        listingImageRepository.deleteByListing(listing);
    }
}