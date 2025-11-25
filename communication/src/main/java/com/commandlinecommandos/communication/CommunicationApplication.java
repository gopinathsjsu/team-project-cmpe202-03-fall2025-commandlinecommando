package com.commandlinecommandos.communication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication(exclude = {SessionAutoConfiguration.class})
public class CommunicationApplication {

	private static final Logger logger = LoggerFactory.getLogger(CommunicationApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Communication Service Application...");
		try {
			SpringApplication.run(CommunicationApplication.class, args);
			logger.info("Communication Service Application started successfully");
		} catch (Exception e) {
			logger.error("Failed to start Communication Service Application", e);
			throw e;
		}
	}

}

