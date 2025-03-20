package dev.davidpalves.cookbetter.auth.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Arrays;
import java.util.List;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    private static final String[] PRIVATE_PATHS = {"/api/auth/verify","/api/auth/logout","/api/recipes","/api/recipes/user"};
    private static final String[] MODIFICATION_PATHS = {"/api/profile/*"};
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
                            String username = authToken.username();
                            String userId = authToken.userId();
                            request.setAttribute("authToken", authToken);
                            request.setAttribute("userId", userId);
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
        if ((request.getMethod().equals("PUT") || request.getMethod().equals("POST")) &&
                Arrays.stream(MODIFICATION_PATHS).anyMatch(privatePath -> pathMatcher.match(privatePath, request.getRequestURI()))) {
            return false;
        }
        return Arrays.stream(PRIVATE_PATHS).noneMatch(privatePath -> pathMatcher.match(privatePath, requestPath));
    }

}
