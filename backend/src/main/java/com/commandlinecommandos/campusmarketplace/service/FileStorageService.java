package com.commandlinecommandos.campusmarketplace.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.commandlinecommandos.campusmarketplace.repository.ListingImageRepository;

@Service
public class FileStorageService {

    @Autowired
    private ListingImageRepository listingImageRepository;
}