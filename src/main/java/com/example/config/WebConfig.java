package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration de la gestion des ressources statiques et des médias
 * Définit les mappings pour les ressources statiques et les médias
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configure les mappings pour les requêtes CORS
     * @param registry le registre des mappings CORS
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true);
    }

    /**
     * Configure les mappings pour les ressources statiques
     * @param registry le registre des mappings des ressources statiques
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .setCachePeriod(3600)
                .resourceChain(true);
        
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(3600)
                .resourceChain(true);
        
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(3600)
                .resourceChain(true);
    }

    /**
     * Configure la négociation de contenu
     * @param configurer le configureur de négociation de contenu
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .mediaType("css", MediaType.valueOf("text/css"))
            .mediaType("js", MediaType.valueOf("application/javascript"))
            .mediaType("html", MediaType.valueOf("text/html"))
            .mediaType("json", MediaType.valueOf("application/json"));
    }
} 