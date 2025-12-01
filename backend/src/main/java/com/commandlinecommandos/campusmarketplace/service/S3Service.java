package com.commandlinecommandos.campusmarketplace.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for handling S3 image uploads.
 * Only enabled when S3Client bean is available (i.e., AWS credentials are configured)
 */
@Service
@ConditionalOnBean(S3Client.class)
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name:webapp-s3-bucket-2025}")
    private String bucketName;

    @Value("${aws.s3.region:us-west-1}")
    private String region;

    // Allowed image content types
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    // Max file size: 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
        logger.info("S3Service initialized - image upload is available");
    }

    /**
     * Upload a single image to S3
     * 
     * @param file The image file to upload
     * @param listingId The listing ID (used for organizing files)
     * @return The public URL of the uploaded image
     */
    public String uploadImage(MultipartFile file, UUID listingId) throws IOException {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String key = generateS3Key(listingId, extension);

        // Note: Public access is handled by the S3 bucket policy, not ACLs
        // Bucket policy grants public read access to objects in listings/* prefix
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

        String imageUrl = getPublicUrl(key);
        logger.info("Image uploaded successfully: {}", imageUrl);

        return imageUrl;
    }

    /**
     * Upload multiple images to S3
     * 
     * @param files List of image files to upload
     * @param listingId The listing ID
     * @return List of public URLs for uploaded images
     */
    public List<String> uploadImages(List<MultipartFile> files, UUID listingId) throws IOException {
        List<String> urls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String url = uploadImage(file, listingId);
                urls.add(url);
            }
        }
        
        return urls;
    }

    /**
     * Delete an image from S3
     * 
     * @param imageUrl The URL of the image to delete
     */
    public void deleteImage(String imageUrl) {
        String key = extractKeyFromUrl(imageUrl);
        
        if (key != null) {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            logger.info("Image deleted successfully: {}", imageUrl);
        }
    }

    /**
     * Delete multiple images from S3
     * 
     * @param imageUrls List of image URLs to delete
     */
    public void deleteImages(List<String> imageUrls) {
        for (String url : imageUrls) {
            try {
                deleteImage(url);
            } catch (Exception e) {
                logger.error("Failed to delete image: {}", url, e);
            }
        }
    }

    /**
     * Validate the uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: JPEG, PNG, GIF, WebP");
        }
    }

    /**
     * Generate a unique S3 key for the image
     */
    private String generateS3Key(UUID listingId, String extension) {
        String uniqueId = UUID.randomUUID().toString();
        return String.format("listings/%s/%s.%s", listingId, uniqueId, extension);
    }

    /**
     * Get the file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Get the public URL for an S3 object
     */
    private String getPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    /**
     * Extract the S3 key from a public URL
     */
    private String extractKeyFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        
        String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
        if (imageUrl.startsWith(prefix)) {
            return imageUrl.substring(prefix.length());
        }
        
        return null;
    }
}

