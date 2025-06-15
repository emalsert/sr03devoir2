import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration CORS pour permettre les requêtes cross-origin
 * depuis le frontend vers l'API backend
 */
@Configuration
public class CorsConfig {

    /**
     * URL du frontend récupérée depuis les propriétés de configuration
     */
    @Value("${URL_FRONTEND}")
    private String urlFrontend;

    /**
     * Bean de configuration CORS qui définit les règles de cross-origin
     * @return WebMvcConfigurer configuré pour CORS
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")  // Applique CORS à toutes les routes
                        .allowedOrigins(urlFrontend)  // Autorise uniquement l'origine du frontend
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")  // Méthodes HTTP autorisées
                        .allowCredentials(true);  // Autorise l'envoi de cookies et headers d'authentification
            }
        };
    }
}
