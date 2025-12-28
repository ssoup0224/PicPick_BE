package com.picpick;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main entry point for the PicPick Spring Boot application.
 * This class initializes the Spring context and starts the embedded web server.
 */
@SpringBootApplication // Enables auto-configuration, component scanning, and extra configuration on
                       // the class
public class PicpickApplication {

    /**
     * The main method that serves as the entry point when the application is
     * launched.
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        // Run the Spring Boot application
        SpringApplication.run(PicpickApplication.class, args);
    }

}
