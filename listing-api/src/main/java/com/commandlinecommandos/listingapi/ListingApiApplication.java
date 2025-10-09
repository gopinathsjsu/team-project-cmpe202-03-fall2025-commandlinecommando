package com.commandlinecommandos.listingapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class ListingApiApplication {

	public static void main(String[] args) {
		log.info("Starting Listing API Application...");
		SpringApplication.run(ListingApiApplication.class, args);
		log.info("Listing API Application started successfully");
	}

}
