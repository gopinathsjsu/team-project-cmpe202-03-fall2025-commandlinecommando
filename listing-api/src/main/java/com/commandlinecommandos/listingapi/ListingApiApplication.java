package com.commandlinecommandos.listingapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication(exclude = {SessionAutoConfiguration.class})
public class ListingApiApplication {

	private static final Logger logger = LoggerFactory.getLogger(ListingApiApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Listing API Application...");
		try {
			SpringApplication.run(ListingApiApplication.class, args);
			logger.info("Listing API Application started successfully");
		} catch (Exception e) {
			logger.error("Failed to start Listing API Application", e);
			throw e;
		}
	}

}
