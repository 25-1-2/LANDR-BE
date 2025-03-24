package com.landr.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
            .title("LANDR API")
            .version("1.0")
            .description("LANDR API 명세서입니다.")
            .contact(new Contact().email("andantej99@ajou.ac.kr."));

        String jwtScheme = "JWT Token";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtScheme);
        Components components = new Components()
            .addSecuritySchemes(jwtScheme,
            new SecurityScheme()
                .name("Authorization")
                .in(SecurityScheme.In.HEADER)
                .type(SecurityScheme.Type.HTTP)
                .scheme("Bearer")
                .bearerFormat("JWT"));

        return new OpenAPI()
            .addServersItem(new Server().url("http://localhost:8080"))
            .components(components)
            .info(info)
            .addSecurityItem(securityRequirement);
    }

}
