package com.bookstores.configuration;

import com.bookstores.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers("/api/auth/**").permitAll()
                                        .requestMatchers(HttpMethod.GET, "/api/categories").permitAll()
                                        .requestMatchers(HttpMethod.GET, "/api/books", "/api/books/*").permitAll()
                                        .requestMatchers(HttpMethod.POST, "/api/books/search-image").permitAll()
                                        .requestMatchers("/api/chat/**").permitAll()
                                        .requestMatchers("/api/recommendations", "/api/recommendations/**").permitAll()
                                        .requestMatchers(HttpMethod.DELETE, "/api/partner/**").hasRole("PARTNER")
                                        .requestMatchers(
                                                "/swagger-ui.html",
                                                "/swagger-ui/**",
                                                "/v3/api-docs",
                                                "/v3/api-docs/**").permitAll()
                                        .anyRequest()
                                        .authenticated())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.addAllowedOriginPattern("*");
        c.addAllowedHeader("*");
        c.addAllowedMethod("*");
        c.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }
}
