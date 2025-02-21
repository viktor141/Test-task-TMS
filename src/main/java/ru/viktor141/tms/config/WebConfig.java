package ru.viktor141.tms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig configures web-related settings for the application.
 * <p>
 * This class enables CORS globally and defines allowed origins, headers, and methods.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures global CORS settings.
     *
     * @param registry The CorsRegistry to register allowed origins, headers, and methods.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:8081")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization")
                .allowCredentials(true);
    }
}
