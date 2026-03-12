package com.example.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://127.0.0.1",
                        "http://127.0.0.1:8080",
                        "http://127.0.0.1:5500",
                        "http://127.0.0.1:3000",
                        "http://localhost",
                        "http://localhost:8080",
                        "http://localhost:5500",
                        "http://localhost:3000",
                        "null"
                )
                .allowedMethods("GET", "POST", "OPTIONS", "PUT", "DELETE")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
