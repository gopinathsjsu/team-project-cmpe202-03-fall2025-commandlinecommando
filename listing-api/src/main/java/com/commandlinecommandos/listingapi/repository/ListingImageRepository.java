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

    Optional<List<ListingImage>> findByListing(Listing listing);

    @Query("SELECT li FROM ListingImage li WHERE li.listing = :listing ORDER BY li.displayOrder ASC")
    Optional<List<ListingImage>> findByListingOrderedByDisplayOrder(@Param("listing") Listing listing);

    @Query("SELECT li FROM ListingImage li WHERE li.listing = :listing AND li.displayOrder = 1")
    Optional<ListingImage> findPrimaryImageByListing(@Param("listing") Listing listing);

    void deleteByListing(Listing listing);

}