package dev.davidpalves.cookbetter.auth.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    private static final String[] PRIVATE_PATHS = {"/auth/verify","/auth/logout"};
    private static final String[] PUT_PRIVATE_PATHS = {"/profile/*"};
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final AuthTokenEncrypter authTokenEncrypter;

    public AuthenticationFilter(AuthTokenEncrypter authTokenEncrypter) {
        this.authTokenEncrypter = authTokenEncrypter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("authToken")) {
                    String token = cookie.getValue();
                    try {
                        AuthToken authToken = authTokenEncrypter.decrypt(token);
                        boolean expired = authToken.isExpired();
                        if (!expired) {
                            request.setAttribute("authToken", authToken);
                            String username = authToken.username();
                            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, List.of());
                            SecurityContextHolder.getContext()
                                    .setAuthentication(authenticationToken);
                            filterChain.doFilter(request,response);
                            return;
                        }
                    } catch (Exception e) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                }
            }
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        if (Objects.equals(request.getMethod(), "PUT") &&
                Arrays.stream(PUT_PRIVATE_PATHS).anyMatch(privatePath -> pathMatcher.match(privatePath, request.getRequestURI()))) {
            return false;
        }
        return Arrays.stream(PRIVATE_PATHS).noneMatch(privatePath -> pathMatcher.match(privatePath, requestPath));
    }

}
