package com.benefactor.agendaCitas.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Configuración de CORS (Cross-Origin Resource Sharing) para la aplicación
 * Permite el acceso desde diferentes orígenes durante el desarrollo
 *
 * @Configuration Indica que esta clase contiene configuraciones de Spring
 * @WebMvcConfigurer Permite personalizar la configuración de Spring MVC
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Configura los mapeos CORS para Spring MVC
     * Define las reglas de CORS para todas las rutas de la aplicación
     *
     * @param registry Registro de configuraciones CORS para Spring MVC
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a todas las rutas
                .allowedOriginPatterns( // Patrones de origen permitidos
                        "http://localhost:*",    // Localhost con cualquier puerto
                        "http://127.0.0.1:*",    // IPv4 local con cualquier puerto
                        "http://0.0.0.0:*"       // Todas las interfaces con cualquier puerto
                )
                .allowedMethods("*")              // Todos los métodos HTTP permitidos
                .allowedHeaders("*")              // Todos los headers permitidos
                .allowCredentials(true)           // Permite credenciales (cookies, auth)
                .maxAge(3600);                    // Tiempo de cache de pre-flight requests (1 hora)
    }

    /**
     * Configuración de CORS para Spring Security y otros componentes
     * Define una fuente de configuración CORS más detallada
     *
     * @return CorsConfigurationSource configurado con las políticas CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Crear configuración CORS
        CorsConfiguration config = new CorsConfiguration();

        // Definir patrones de origen permitidos
        config.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",    // Localhost con cualquier puerto
                "http://127.0.0.1:*",    // IPv4 local con cualquier puerto
                "http://0.0.0.0:*"       // Todas las interfaces con cualquier puerto
        ));

        // Métodos HTTP permitidos
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Headers permitidos
        config.setAllowedHeaders(Arrays.asList("*"));

        // Permitir credenciales
        config.setAllowCredentials(true);

        // Headers expuestos al cliente
        config.setExposedHeaders(Arrays.asList("Set-Cookie"));

        // Registrar configuración para todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}