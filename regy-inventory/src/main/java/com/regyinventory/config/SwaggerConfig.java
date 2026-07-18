package com.regyinventory.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI regyInventoryOpenAPI() {

        SecurityScheme securityScheme = new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Ingresa únicamente el token JWT");

        SecurityRequirement securityRequirement =
                new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME);

        return new OpenAPI()
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        securityScheme
                                )
                )
                .addSecurityItem(securityRequirement)
                .info(
                        new Info()
                                .title("REGY Inventory API")
                                .description(
                                        "API REST para la gestión de inventario de REGY"
                                )
                                .version("1.0.0")
                                .contact(
                                        new Contact()
                                                .name("REGY Inventory")
                                                .email("admin@regy.local")
                                )
                                .license(
                                        new License()
                                                .name("Uso interno")
                                )
                );
    }
}