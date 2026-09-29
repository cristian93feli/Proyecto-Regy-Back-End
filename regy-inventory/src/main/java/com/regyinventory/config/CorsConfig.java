package com.regyinventory.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;

import java.util.*;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:4200,  https://regy-frontend-azii.vercel.app}")
    private String origins;

    @Override
    public void addCorsMappings(CorsRegistry r) {
        r.addMapping("/**").allowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toArray(String[]::new)).allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS").allowedHeaders("*").allowCredentials(true);
    }
}
