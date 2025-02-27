package dev.davidpalves.cookbetter.auth.api;

import dev.davidpalves.cookbetter.auth.dto.AuthenticationDTO;
import dev.davidpalves.cookbetter.auth.dto.AuthenticationResponse;
import dev.davidpalves.cookbetter.auth.dto.AuthenticationResult;
import dev.davidpalves.cookbetter.auth.service.AuthenticationService;
import dev.davidpalves.cookbetter.models.ServiceResult;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping
@Slf4j
public class AuthenticationController {
    private static final String LOG_TITLE = "[AuthenticationController] -";
    public static final int AUTH_COOKIE_EXPIRY = 60 * 60 * 24 * 30;
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthenticationResponse> registerUser(@RequestBody AuthenticationDTO authDTO) throws NoSuchAlgorithmException {
        log.info("{} Register request received", LOG_TITLE);
        boolean validated = AuthenticationRequestValidator.validateRegisterDTO(authDTO);
        if (validated) {
            log.debug("{} Request is valid, proceeding to registering the user", LOG_TITLE);
            ServiceResult<AuthenticationResult> serviceResult = authenticationService.registerUser(authDTO);
            if (serviceResult.isSuccess()) {
                log.info("{} User registered successfully", LOG_TITLE);
                AuthenticationResult authenticationResult = serviceResult.getData();
                return new ResponseEntity<>(new AuthenticationResponse(authenticationResult.getMessage(), authenticationResult.getUser()), HttpStatus.CREATED);
            }
            if (serviceResult.getErrorCode() == 1) {
                log.info("{} Registration failed because email was already taken.", LOG_TITLE);
                return new ResponseEntity<>(new AuthenticationResponse(serviceResult.getErrorMessage()), HttpStatus.CONFLICT);
            }
            if (serviceResult.getErrorCode() == 2) {
                log.info("{} Registration failed because username was already taken.", LOG_TITLE);
                return new ResponseEntity<>(new AuthenticationResponse(serviceResult.getErrorMessage()), HttpStatus.CONFLICT);
            }
        }
        return createBadRequestResponse();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> loginUser(@RequestBody AuthenticationDTO authDTO, HttpServletResponse response) {
        log.info("{} Login request received", LOG_TITLE);
        boolean validated = AuthenticationRequestValidator.validateLoginDTO(authDTO);
        if (validated) {
            log.debug("{} Request is valid, proceeding to logging in the user", LOG_TITLE);
            ServiceResult<AuthenticationResult> serviceResult = authenticationService.loginUser(authDTO);
            if (serviceResult.isSuccess()) {
                Cookie authCookie = createAuthCookie(serviceResult);
                response.addCookie(authCookie);
                log.info("{} Login successfully", LOG_TITLE);
                AuthenticationResult authenticationResult = serviceResult.getData();
                return new ResponseEntity<>(new AuthenticationResponse(authenticationResult.getMessage(),authenticationResult.getUser()), HttpStatus.OK);
            }
            if (serviceResult.getErrorCode() == 1) {
                log.info("{} Unauthorized access. Login failed", LOG_TITLE);
                return new ResponseEntity<>(new AuthenticationResponse(serviceResult.getErrorMessage()), HttpStatus.UNAUTHORIZED);
            }
            else if (serviceResult.getErrorCode() == 2) {
                log.info("{} Could not found user. Login failed", LOG_TITLE);
                return new ResponseEntity<>(new AuthenticationResponse(serviceResult.getErrorMessage()), HttpStatus.NOT_FOUND);
            }
        }
        return createBadRequestResponse();
    }

    @GetMapping("/verify")
    public ResponseEntity<AuthenticationResponse> checkAuthentication(HttpServletRequest request, HttpServletResponse response) {
        log.info("{} Check authentication request received", LOG_TITLE);
        ServiceResult<AuthenticationResult> serviceResult = authenticationService.checkAuthentication(request.getCookies());
        if (serviceResult.isSuccess()) {
            log.info("{} User is authenticated", LOG_TITLE);
            Cookie authCookie = createAuthCookie(serviceResult);
            response.addCookie(authCookie);
            AuthenticationResult authenticationResult = serviceResult.getData();
            return new ResponseEntity<>(new AuthenticationResponse(authenticationResult.getMessage(),authenticationResult.getUser()), HttpStatus.OK);
        }
        log.info("{} User is not authenticated", LOG_TITLE);
        removeAuthCookie(response);
        return new ResponseEntity<>(new AuthenticationResponse(serviceResult.getErrorMessage()), HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/logout")
    public ResponseEntity<AuthenticationResponse> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        log.info("{} Logout request received", LOG_TITLE);
        removeAuthCookie(response);
        return new ResponseEntity<>(new AuthenticationResponse("User logged out successfully!"), HttpStatus.OK);
    }


    private ResponseEntity<AuthenticationResponse> createBadRequestResponse() {
        log.info("{} Request was not valid.", LOG_TITLE);
        return new ResponseEntity<>(new AuthenticationResponse("Request Malformed"), HttpStatus.BAD_REQUEST);
    }

    private Cookie createAuthCookie(ServiceResult<AuthenticationResult> serviceResult) {
        AuthenticationResult authenticationResult = serviceResult.getData();
        String accessToken = authenticationResult.getAccessToken();
        Cookie authCookie = new Cookie("authToken",accessToken);
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
