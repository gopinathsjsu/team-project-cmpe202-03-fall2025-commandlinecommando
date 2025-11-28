package com.commandlinecommandos.campusmarketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.commandlinecommandos.campusmarketplace.model.ListingImage;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ListingImage entity
 * Consolidated from listing-api
 */
@Repository
public interface ListingImageRepository extends JpaRepository<ListingImage, Long> {

    @Query("SELECT li FROM ListingImage li WHERE li.listingId = :listingId")
    List<ListingImage> findByListingId(@Param("listingId") Long listingId);

    @Query("SELECT li FROM ListingImage li WHERE li.listingId = :listingId ORDER BY li.displayOrder ASC")
    List<ListingImage> findByListingIdOrdered(@Param("listingId") Long listingId);

    List<ListingImage> findByListingIdOrderByDisplayOrder(Long listingId);

    @Query("SELECT li FROM ListingImage li WHERE li.listingId = :listingId AND li.displayOrder = 1")
    Optional<ListingImage> findPrimaryImageByListingId(@Param("listingId") Long listingId);

    void deleteByListingId(Long listingId);
}
