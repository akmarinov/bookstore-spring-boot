package com.example.bookstore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    
    @Value("${app.openapi.dev-url:http://localhost:8080}")
    private String devUrl;
    
    @Value("${app.openapi.prod-url:}")
    private String prodUrl;
    
    @Bean
    public OpenAPI openAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Server URL in Development environment");
        
        Contact contact = new Contact();
        contact.setEmail("support@bookstore.com");
        contact.setName("Bookstore Support");
        contact.setUrl("https://www.bookstore.com");
        
        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");
        
        Info info = new Info()
                .title("Bookstore Management API")
                .version("1.0")
                .contact(contact)
                .description("This API provides endpoints for managing books in an online bookstore. " +
                           "It supports CRUD operations, search functionality, and inventory management.")
                .termsOfService("https://www.bookstore.com/terms")
                .license(mitLicense);
        
        OpenAPI openAPI = new OpenAPI().info(info);
        
        // Add development server
        openAPI.addServersItem(devServer);
        
        // Add production server if configured
        if (prodUrl != null && !prodUrl.isEmpty()) {
            Server prodServer = new Server();
            prodServer.setUrl(prodUrl);
            prodServer.setDescription("Server URL in Production environment");
            openAPI.addServersItem(prodServer);
        }
        
        return openAPI;
    }
}