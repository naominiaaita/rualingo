package com.example.rualingo.config;

import com.example.rualingo.repository.UserRepository;
import com.example.rualingo.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, ex) -> writeJsonError(
                                response,
                                HttpServletResponse.SC_UNAUTHORIZED,
                                "Unauthorized",
                                ex.getMessage(),
                                request.getRequestURI()))
                        .accessDeniedHandler((request, response, ex) -> writeJsonError(
                                response,
                                HttpServletResponse.SC_FORBIDDEN,
                                "Forbidden",
                                ex.getMessage(),
                                request.getRequestURI())))
                .authorizeHttpRequests(auth -> auth
                        // Allow internal error/forward dispatches (prevents `/error` from being secured).
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                        .requestMatchers("/error").permitAll()
                        // Allow CORS preflight requests.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/chat/**").permitAll() // Add this line
                        // Used by the Android app landing screen before login.
                        .requestMatchers(HttpMethod.GET, "/api/exercises/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/courses/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/lessons/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/vocabulary/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/debug/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private static void writeJsonError(
            HttpServletResponse response,
            int status,
            String error,
            String message,
            String path) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);

        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            var user = userRepository.findByEmail(username).orElse(null);
            if (user == null) {
                user = userRepository.findByUsername(username).orElse(null);
            }
            if (user == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }
            return User.withUsername(user.getEmail())
                    .password(user.getPassword() != null ? user.getPassword() : "")
                    .roles(user.getRole() != null ? user.getRole().getName() : "USER")
                    .disabled(!user.isActive())
                    .build();
        };
    }
}
