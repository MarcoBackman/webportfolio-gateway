package com.marco.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //Todo: read from environment or local file
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:63342", "https://marcobackman.github.io/web-portfolio")
                .allowedMethods("GET", "POST")
                .allowedHeaders("Content-Type")
                .maxAge(3600);
    }
}