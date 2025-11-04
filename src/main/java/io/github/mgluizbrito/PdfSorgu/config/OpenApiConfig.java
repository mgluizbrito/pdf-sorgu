package io.github.mgluizbrito.PdfSorgu.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "PDFSorgu API", version = "v0.2", description = "Retrieval Augmented Generation (RAG) system for answering questions based on the content of PDF files"))
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT access token, obtained from the /auth/login endpoint."
)
public class OpenApiConfig {
}
