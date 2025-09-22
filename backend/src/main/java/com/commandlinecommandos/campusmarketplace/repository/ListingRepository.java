package com.commandlinecommandos.campusmarketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.commandlinecommandos.campusmarketplace.model.Listing;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {
}