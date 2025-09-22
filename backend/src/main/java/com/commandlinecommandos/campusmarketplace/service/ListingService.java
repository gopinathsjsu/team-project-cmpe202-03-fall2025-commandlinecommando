package com.commandlinecommandos.campusmarketplace.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.commandlinecommandos.campusmarketplace.repository.ListingRepository;

@Service
public class ListingService {

    @Autowired
    private ListingRepository listingRepository;
}