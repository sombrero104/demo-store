package com.store.auth;

import com.store.dto.MessageResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthorityService authorityService;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response
            , @NonNull FilterChain filterChain) throws ServletException, IOException, AuthenticationException {

        String token = getTokenFromRequest(request);

        try {
            if (jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmail(token);
                var authorities = authorityService.loadAuthorities(email);

                UserDetails userDetails = new User(email, "", authorities);
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, token, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.warn("JWT authentication failed. method={}, path={}, reason={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    e.getClass().getSimpleName());
            handleJwtException(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /*@Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String path = request.getServletPath();
        return path.equals("/api/account/login")
                || path.equals("/api/account/refresh")
                || path.equals("/api/account/logout")
                || path.equals("/api/account/join");
    }*/

    private void handleJwtException(HttpServletResponse response) throws IOException {
        String responseBody = objectMapper.writeValueAsString(
                MessageResponseDto.builder().message("Invalid JWT token").build());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(responseBody);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (isBearerToken(authorizationHeader)) {
            return extractToken(authorizationHeader);
        }
        return null;
    }

    private boolean isBearerToken(String header) {
        return header != null && header.startsWith(BEARER_PREFIX);
    }

    private String extractToken(String header) {
        return header.substring(BEARER_PREFIX.length());
    }

}
