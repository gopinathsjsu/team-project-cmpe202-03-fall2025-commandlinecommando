package com.commandlinecommandos.campusmarketplace.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "listing_images")
public class ListingImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;
    
    @Column(nullable = false)
    private String imageUrl;
    
    @Column(nullable = false)
    private String fileName;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}