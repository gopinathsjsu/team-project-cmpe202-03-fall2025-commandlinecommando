package com.commandlinecommandos.campusmarketplace.service;

import com.commandlinecommandos.campusmarketplace.exception.FileStorageException;
import com.commandlinecommandos.campusmarketplace.exception.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.Arrays;
import java.util.List;

/**
 * Service for handling file uploads and storage
 * Manages image files for listings
 * Consolidated from listing-api
 */
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif",
        "image/webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public FileStorageService(@Value("${file.upload-dir:./uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create upload directory: " + uploadDir, ex);
        }
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize file storage", e);
        }
    }

    /**
     * Store an uploaded file
     * @param file the multipart file to store
     * @return the stored filename
     */
    public String storeFile(MultipartFile file) {
        // Validate file
        validateFile(file);

        // Generate unique filename
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + fileExtension;

        try {
            // Check for invalid characters
            if (filename.contains("..")) {
                throw new FileUploadException("Invalid path sequence in filename: " + filename);
            }

            // Copy file to target location
            Path targetLocation = this.fileStorageLocation.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return filename;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + filename, ex);
        }
    }

    /**
     * Store multiple files
     * @param files array of files to store
     * @return list of stored filenames
     */
    public List<String> storeFiles(MultipartFile[] files) {
        return Arrays.stream(files)
                .map(this::storeFile)
                .toList();
    }

    /**
     * Delete a file by filename
     * @param filename the filename to delete
     */
    public void deleteFile(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file " + filename, ex);
        }
    }

    /**
     * Delete multiple files
     * @param filenames list of filenames to delete
     */
    public void deleteFiles(List<String> filenames) {
        filenames.forEach(this::deleteFile);
    }

    /**
     * Get the path to a stored file
     * @param filename the filename
     * @return the path to the file
     */
    public Path getFilePath(String filename) {
        return this.fileStorageLocation.resolve(filename).normalize();
    }

    /**
     * Get the URL path for a stored file
     * @param filename the filename
     * @return the URL path (e.g., "/uploads/abc-123.jpg")
     */
    public String getFileUrl(String filename) {
        return "/uploads/" + filename;
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File is empty");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException(
                "File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB"
            );
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new FileUploadException(
                "Invalid file type. Allowed types: JPEG, PNG, GIF, WEBP"
            );
        }

        // Check filename
        String filename = file.getOriginalFilename();
        if (filename == null || filename.contains("..")) {
            throw new FileUploadException("Invalid filename");
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }

        return filename.substring(lastDotIndex);
    }

    /**
     * Check if a file exists
     * @param filename the filename to check
     * @return true if file exists, false otherwise
     */
    public boolean fileExists(String filename) {
        Path filePath = this.fileStorageLocation.resolve(filename).normalize();
        return Files.exists(filePath);
    }

    /**
     * Get the file storage location path
     * @return the storage location path
     */
    public Path getFileStorageLocation() {
        return fileStorageLocation;
    }
}
