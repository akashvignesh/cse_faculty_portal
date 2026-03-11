package edu.buffalo.cse.facultyportal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI facultyPortalOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Faculty Portal Backend API")
                        .description("REST APIs for faculty listing, profile photo retrieval, and faculty document updates")
                        .version("v1")
                        .contact(new Contact()
                                .name("Faculty Portal Team")
                                .email("support@faculty-portal.local"))
                        .license(new License()
                                .name("Internal Use")
                                .url("https://www.buffalo.edu/")));
    }
}
