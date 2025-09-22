package com.commandlinecommandos.campusmarketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.commandlinecommandos.campusmarketplace.model.ListingImage;

@Repository
public interface ListingImageRepository extends JpaRepository<ListingImage, Long> {
}