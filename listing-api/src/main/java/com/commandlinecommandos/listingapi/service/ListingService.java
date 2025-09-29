package com.commandlinecommandos.listingapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.commandlinecommandos.listingapi.model.Listing;
import com.commandlinecommandos.listingapi.repository.ListingRepository;

@Service
public class ListingService {

    @Autowired
    private ListingRepository listingRepository;

    public Listing getListingById(Long listingId) {
        return listingRepository.findById(listingId).orElse(null);
    }
}