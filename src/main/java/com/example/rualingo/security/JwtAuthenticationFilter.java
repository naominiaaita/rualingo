package com.example.rualingo.security;

import com.example.rualingo.model.User;
import com.example.rualingo.repository.UserRepository;
import com.example.rualingo.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtService.isTokenValid(token) || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = jwtService.extractAllClaims(token);
        String subject = claims.getSubject();
        if (subject == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Long userId = Long.parseLong(subject);
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            String role = claims.get("role", String.class);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user.get(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + (role != null ? role : "USER"))));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (NumberFormatException ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
