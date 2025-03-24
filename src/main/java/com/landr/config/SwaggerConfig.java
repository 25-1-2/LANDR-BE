package com.landr.config;

import com.landr.properties.SwaggerProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private final SwaggerProperties swaggerProperties;

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

        OpenAPI openAPI = new OpenAPI()
            .components(components)
            .info(info)
            .addSecurityItem(securityRequirement);

        for (String url: swaggerProperties.getServers()) {
            openAPI.addServersItem(new Server().url(url));
        }

        return openAPI;
    }

}
