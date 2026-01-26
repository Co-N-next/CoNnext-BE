package com.umc.connext.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
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
        return new OpenAPI()
                .info(new Info()
                        .title("Connext API")
                        .description("공연장 관련 서비스")
                        .version("v1.0.0"))
                .addServersItem(new Server()
                        .url("https://api.con-next.xyz")
                        .description("Production"))
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Local"));
//                .addSecurityItem(new SecurityRequirement().addList("JWT"))
//                .components(new Components()
//                        .addSecuritySchemes("JWT", new SecurityScheme()
//                                .type(SecurityScheme.Type.HTTP)
//                                .scheme("bearer")
//                                .bearerFormat("JWT")));
    }
}