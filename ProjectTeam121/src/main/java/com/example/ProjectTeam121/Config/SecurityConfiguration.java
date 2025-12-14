package com.example.ProjectTeam121.Config;

import com.example.ProjectTeam121.Security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    // Inject URL frontend từ application.properties
    @Value("${application.frontend.url:http://localhost:3000}") // Default là 3000 nếu không tìm thấy
    private String frontendUrl;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Cho phép đúng domain của Frontend, không dùng "*"
        configuration.setAllowedOrigins(List.of(frontendUrl, "http://localhost:5173")); // Thêm port 5173 nếu dùng Vite
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "x-no-retry"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight request 1 giờ

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Kích hoạt CORS từ bean trên
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Các API Public
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/v1/auth/**",
                                "/uploads/**"
                        ).permitAll()

                        // API Chat
                        .requestMatchers("/api/v1/chat/**").permitAll()

                        // Quyền Admin
                        .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")

                        // Quyền IoT
                        .requestMatchers(HttpMethod.POST, "/api/v1/iot/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/iot/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/iot/**").authenticated()

                        // Quyền Xem IoT
                        .requestMatchers(HttpMethod.GET, "/api/v1/iot/**").authenticated()

                        // Còn lại bắt buộc đăng nhập
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}