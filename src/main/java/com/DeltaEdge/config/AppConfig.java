package com.DeltaEdge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class AppConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // TEMPORARY & FEATURE ENDPOINTS
                        .requestMatchers("/api/orders/pay").permitAll()
                        .requestMatchers("/api/graph/**").permitAll()

                        // STANDARD RULES
                        .requestMatchers("/auth/**", "/api/coins/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                // CRITICAL: Disable CSRF and Frame Options for H2 Console testing
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**").disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .addFilterBefore(new JwtTokenValidator(), BasicAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Frontend origins (React/Vite setup)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:5173", "https://your-vercel-app-url.vercel.app"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);

        // Required headers for the Idempotency Layer and Auth to function
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Cache-Control",
                "Content-Type",
                "X-Idempotency-Key"
        ));

        // Expose headers so the frontend Axios client can read the responses
        configuration.setExposedHeaders(Arrays.asList("Authorization", "X-Idempotency-Key"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}