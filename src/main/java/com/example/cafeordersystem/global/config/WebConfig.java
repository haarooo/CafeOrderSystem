package com.example.cafeordersystem.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.qr.save-dir:uploads/qrcodes}")
    private String qrSaveDir;

    @Value("${app.qr.public-prefix:/qrcodes}")
    private String qrPublicPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String qrPath = Paths.get(qrSaveDir)
                .toAbsolutePath()
                .toString()
                .replace("\\", "/");

        String publicPattern = qrPublicPrefix.endsWith("/**")
                ? qrPublicPrefix
                : qrPublicPrefix + "/**";

        registry.addResourceHandler(publicPattern)
                .addResourceLocations("file:" + qrPath + "/");
    }
}