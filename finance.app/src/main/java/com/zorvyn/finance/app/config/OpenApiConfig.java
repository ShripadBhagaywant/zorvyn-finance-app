package com.zorvyn.finance.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Zorvyn Finance API")
                        .description(
                                "REST API for the Zorvyn personal finance dashboard. " +
                                        "Supports user management, financial record CRUD, " +
                                        "keyword search, filtering, pagination, and dashboard analytics. " +
                                        "All protected endpoints require a valid JWT cookie (zorvyn_at) obtained via POST /api/v1/auth/login."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Zorvyn FinTech")
                                .email("support@zorvyn.com")
                        )
                )
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development")
                ))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("zorvyn_at")
                                        .description(
                                                "HttpOnly JWT cookie set automatically on successful login via POST /api/v1/auth/login. " +
                                                        "Send requests from the same origin and the browser will attach it automatically. " +
                                                        "For API clients (Postman, curl), copy the Set-Cookie value from the login response and include it manually."
                                        )
                        )
                );
    }
}