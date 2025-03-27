package dev.davidpalves.cookbetter.auth.api;

import dev.davidpalves.cookbetter.auth.configuration.AuthToken;
import dev.davidpalves.cookbetter.auth.configuration.AuthTokenEncrypter;
import dev.davidpalves.cookbetter.auth.dto.AuthenticationDTO;
import dev.davidpalves.cookbetter.auth.service.AuthenticationService;
import dev.davidpalves.cookbetter.models.ServiceResult;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthenticationController {
    private static final String LOG_TITLE = "[AuthenticationController] -";
    public static final int AUTH_COOKIE_EXPIRY = 60 * 60 * 24 * 30;
    public static final int AUTH_COOKIE_REFRESH = 60 * 15;
    private final AuthenticationService authenticationService;
    private final AuthTokenEncrypter authTokenEncrypter;

    public AuthenticationController(AuthenticationService authenticationService, AuthTokenEncrypter authTokenEncrypter) {
        this.authenticationService = authenticationService;
        this.authTokenEncrypter = authTokenEncrypter;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signupUser(@RequestBody AuthenticationDTO authDTO, HttpServletResponse response) {
        log.info("{} Register request received", LOG_TITLE);
        boolean validated = AuthenticationRequestValidator.validateRegisterDTO(authDTO);
        if (validated) {
            log.debug("{} Request is valid, proceeding to registering the user", LOG_TITLE);
            ServiceResult<String> serviceResult = authenticationService.signupUser(authDTO);
            if (serviceResult.isSuccess()) {
                try {
                    log.info("{} User registered successfully", LOG_TITLE);
                    Cookie authCookie = createAuthCookie(authDTO.getUsername(),serviceResult.getData());
                    response.addCookie(authCookie);
                    return new ResponseEntity<>(HttpStatus.CREATED);
                } catch (Exception e) {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            else {
                log.info("{} Registration failed due to {}.", LOG_TITLE,serviceResult.getErrorMessage());
                if (serviceResult.getErrorCode() == 2) {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                return new ResponseEntity<>("<p>Registration failed. " + serviceResult.getErrorMessage() + "</p>", HttpStatus.CONFLICT);
            }
        }
        return createBadRequestResponse();
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody AuthenticationDTO authDTO, HttpServletResponse response) {
        log.info("{} Login request received", LOG_TITLE);
        boolean validated = AuthenticationRequestValidator.validateLoginDTO(authDTO);
        if (validated) {
            log.debug("{} Request is valid, proceeding to logging in the user", LOG_TITLE);
            ServiceResult<Map.Entry<String,String>> serviceResult = authenticationService.loginUser(authDTO);
            if (serviceResult.isSuccess()) {
                try {
                    Map.Entry<String, String> usernameIdPair = serviceResult.getData();
                    Cookie authCookie = createAuthCookie(usernameIdPair.getKey(), usernameIdPair.getValue());
                    response.addCookie(authCookie);
                    log.info("{} Login successfully", LOG_TITLE);
                    return new ResponseEntity<>(HttpStatus.OK);
                } catch (Exception e) {
                    log.warn("{} INTERNAL ERROR. Failed to create auth token. ", LOG_TITLE);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            else {
                log.info("{} Unauthorized access. Login failed due to {}", LOG_TITLE,serviceResult.getErrorMessage());
                if (serviceResult.getErrorCode() == 2) {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                return new ResponseEntity<>("<p>Login failed. " + serviceResult.getErrorMessage() + "</p>", HttpStatus.UNAUTHORIZED);
            }
        }
        return createBadRequestResponse();
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String,String>> checkAuthentication(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("{} Check authentication request received", LOG_TITLE);
        AuthToken authToken = (AuthToken) request.getAttribute("authToken");
        assert authToken != null;
        if (authToken.needsRefresh()) {
            Cookie authCookie = createAuthCookie(authToken.username(), authToken.userId());
            response.addCookie(authCookie);
        }
        return new ResponseEntity<>(Map.of("userId",authToken.userId(),"username",authToken.username()),HttpStatus.OK);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletResponse response) {
        log.info("{} Logout request received", LOG_TITLE);
        removeAuthCookie(response);
        return new ResponseEntity<>("User logged out successfully!", HttpStatus.OK);
    }


    private ResponseEntity<String> createBadRequestResponse() {
        log.info("{} Request was not valid.", LOG_TITLE);
        return new ResponseEntity<>("Request Malformed", HttpStatus.BAD_REQUEST);
    }

    private Cookie createAuthCookie(String username, String userId) throws Exception {
        AuthToken authToken = new AuthToken(username, userId, LocalDateTime.now().plusSeconds(AUTH_COOKIE_REFRESH), LocalDateTime.now().plusSeconds(AUTH_COOKIE_EXPIRY));
        Cookie authCookie = new Cookie("authToken", authTokenEncrypter.encrypt(authToken));
        authCookie.setHttpOnly(true);
        //TODO: HANDLE ONLY HTTPS TRAFFIC
//                authCookie.setSecure(true);
        authCookie.setPath("/");
        authCookie.setMaxAge(AUTH_COOKIE_EXPIRY);
        authCookie.setAttribute("SameSite","Lax");
        return authCookie;
    }

    private void removeAuthCookie(HttpServletResponse response) {
        Cookie authCookie = new Cookie("authToken",null);
        authCookie.setHttpOnly(true);
        //TODO: HANDLE ONLY HTTPS TRAFFIC
//                authCookie.setSecure(true);
        authCookie.setPath("/");
        authCookie.setMaxAge(0);
        authCookie.setAttribute("SameSite","Lax");
        response.addCookie(authCookie);
    }
}
