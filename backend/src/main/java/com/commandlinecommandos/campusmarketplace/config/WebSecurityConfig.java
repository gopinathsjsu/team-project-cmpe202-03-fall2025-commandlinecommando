package com.commandlinecommandos.campusmarketplace.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.commandlinecommandos.campusmarketplace.security.JwtAuthenticationFilter;
import com.commandlinecommandos.campusmarketplace.security.JwtAuthenticationEntryPoint;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@EnableAspectJAutoProxy
public class WebSecurityConfig {
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public auth endpoints (no authentication required)
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/auth/register").permitAll()
                .requestMatchers("/auth/refresh").permitAll()
                .requestMatchers("/auth/logout").permitAll()
                .requestMatchers("/auth/validate").permitAll()
                .requestMatchers("/auth/forgot-password").permitAll()
                .requestMatchers("/auth/reset-password").permitAll()

                // Protected auth endpoints (authentication required)
                .requestMatchers("/auth/me").authenticated()
                .requestMatchers("/auth/logout-all").authenticated()

                // Other public endpoints
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                
                // Admin only endpoints (context path /api is already applied)
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Debug endpoints (admin only, development use)
                .requestMatchers("/debug/**").hasRole("ADMIN")
                
                // Listings endpoints (context path /api is already applied)
                // Public read access for GET requests
                .requestMatchers("/listings", "/listings/{id}", "/listings/seller/{sellerId}").permitAll()
                // Protected write operations require SELLER role (students have both BUYER and SELLER by default)
                .requestMatchers("/listings/**").hasAnyRole("SELLER", "ADMIN")

                // User endpoints - BUYER and SELLER can access user features
                .requestMatchers("/user/**").hasAnyRole("BUYER", "SELLER", "ADMIN")

                .requestMatchers("/actuator/prometheus").permitAll()
                .requestMatchers("/actuator/metrics").permitAll()
                
                // Swagger/OpenAPI endpoints
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                
                
                // User profile endpoints (authenticated users - context path /api is already applied)
                .requestMatchers("/users/profile").authenticated()
                .requestMatchers("/users/change-password").authenticated()
                .requestMatchers("/users/deactivate").authenticated()
                .requestMatchers("/users/{userId}").authenticated()
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            );
        
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        // For H2 Console (development only)
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.deny()));
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins instead of all origins
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",           // React development server
            "http://localhost:3001",           // Alternative React port
            "http://localhost:3002",           // Alternative React port
            "http://localhost:5000",           // Vite development server
            "http://localhost:5001",           // Alternative Vite port
            "http://localhost:5002",           // Alternative Vite port
            "http://localhost:5173",           // Vite default port
            "http://127.0.0.1:3000",          // Localhost alternative
            "http://127.0.0.1:3001",          // Localhost alternative
            "http://127.0.0.1:3002",          // Localhost alternative
            "http://127.0.0.1:5000",          // Vite localhost alternative
            "http://127.0.0.1:5001",          // Vite localhost alternative
            "http://127.0.0.1:5002",          // Vite localhost alternative
            "http://127.0.0.1:5173",          // Vite default localhost alternative
            "https://campus-marketplace.sjsu.edu",  // Production domain
            "https://*.sjsu.edu"              // SJSU subdomains
        ));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With", 
            "Accept", 
            "Origin", 
            "Access-Control-Request-Method", 
            "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", 
            "Access-Control-Allow-Credentials"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}