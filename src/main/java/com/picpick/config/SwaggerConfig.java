package com.picpick.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Swagger (OpenAPI 3).
 * This class defines how the API documentation is generated and displayed.
 */
@Configuration // Marks this class as a configuration source for the Spring context
public class SwaggerConfig {

    /**
     * Configures the main OpenAPI object.
     * This include server URLs, components, and general API information.
     *
     * @return The configured OpenAPI object.
     */
    @Bean // Tells Spring to manage the returned OpenAPI object as a bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("/")) // Sets the base URL for testing in Swagger UI
                .components(new Components()) // Placeholder for security schemes, schemas, etc.
                .info(apiInfo()); // Attaches API metadata
    }

    /**
     * Defines metadata for the API such as title, description, and version.
     *
     * @return An Info object containing API metadata.
     */
    private Info apiInfo() {
        return new Info()
                .title("PicPick API") // The title shown in Swagger UI
                .description("Backend API for the PicPick application, integrating AI chat capabilities.") // Short
                                                                                                           // description
                .version("1.0.0"); // Current API version
    }
}