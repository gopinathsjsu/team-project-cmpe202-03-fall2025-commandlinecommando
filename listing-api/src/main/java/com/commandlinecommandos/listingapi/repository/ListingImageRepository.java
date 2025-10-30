package com.commandlinecommandos.listingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.commandlinecommandos.listingapi.model.ListingImage;
import com.commandlinecommandos.listingapi.model.Listing;
import java.util.List;
import java.util.Optional;

@Repository
public interface ListingImageRepository extends JpaRepository<ListingImage, Long> {

    @Query("SELECT li FROM ListingImage li WHERE li.listingId = :listingId")
    Optional<List<ListingImage>> findByListingId(@Param("listingId") Long listingId);

    @Query("SELECT li FROM ListingImage li WHERE li.listingId = :listingId ORDER BY li.displayOrder ASC")
    Optional<List<ListingImage>> findByListingIdOrdered(@Param("listingId") Long listingId);

    @Query("SELECT li FROM ListingImage li WHERE li.listingId = :listingId AND li.displayOrder = 1")
    Optional<ListingImage> findPrimaryImageByListingId(@Param("listingId") Long listingId);

    void deleteByListingId(Long listingId);

}