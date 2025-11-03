package io.github.mgluizbrito.PdfSorgu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String DOWNLOAD_DIR = "./uploads";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path downloadPath = Paths.get(DOWNLOAD_DIR);
        String absolutePath = downloadPath.toFile().getAbsolutePath();

        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }
}