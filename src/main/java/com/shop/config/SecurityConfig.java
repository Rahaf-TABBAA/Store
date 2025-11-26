package com.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;
    
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;
    
    @Value("${app.cors.allowed-methods}")
    private String allowedMethods;
    
    @Value("${app.cors.allowed-headers}")
    private String allowedHeaders;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/products/**").permitAll()
                .requestMatchers("/api/categories/**").permitAll()
                .requestMatchers("/api/users/exists/**").permitAll()
                // Health check endpoints
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                // Admin endpoints
                .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "CUSTOMER")
                .requestMatchers("/api/orders/**").hasAnyRole("ADMIN", "CUSTOMER")
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        
        return http.build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new JwtGrantedAuthoritiesConverter().convert(jwt);
            
            // Create a mutable collection
            Collection<GrantedAuthority> allAuthorities = authorities != null ? 
                authorities.stream().collect(Collectors.toList()) : 
                new java.util.ArrayList<>();
            
            // Extract realm roles from Keycloak token
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");
                
                Collection<GrantedAuthority> realmAuthorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
                
                allAuthorities.addAll(realmAuthorities);
            }
            
            // Extract resource roles from Keycloak token
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                resourceAccess.values().forEach(resource -> {
                    if (resource instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> resourceMap = (Map<String, Object>) resource;
                        if (resourceMap.containsKey("roles")) {
                            @SuppressWarnings("unchecked")
                            List<String> roles = (List<String>) resourceMap.get("roles");
                            
                            Collection<GrantedAuthority> resourceAuthorities = roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                                .collect(Collectors.toList());
                            
                            allAuthorities.addAll(resourceAuthorities);
                        }
                    }
                });
            }
            
            return allAuthorities;
        });
        
        return jwtConverter;
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Parse allowed origins
        String[] origins = allowedOrigins.split(",");
        for (String origin : origins) {
            configuration.addAllowedOrigin(origin.trim());
        }
        
        // Parse allowed methods
        String[] methods = allowedMethods.split(",");
        for (String method : methods) {
            configuration.addAllowedMethod(method.trim());
        }
        
        // Parse allowed headers
        if ("*".equals(allowedHeaders.trim())) {
            configuration.addAllowedHeader("*");
        } else {
            String[] headers = allowedHeaders.split(",");
            for (String header : headers) {
                configuration.addAllowedHeader(header.trim());
            }
        }
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}