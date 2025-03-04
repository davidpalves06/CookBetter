package dev.davidpalves.cookbetter.auth.api;

import dev.davidpalves.cookbetter.auth.configuration.AuthTokenEncrypter;
import dev.davidpalves.cookbetter.auth.dto.AuthenticationDTO;
import dev.davidpalves.cookbetter.auth.service.AuthenticationService;
import dev.davidpalves.cookbetter.auth.service.PasswordHasher;
import dev.davidpalves.cookbetter.models.User;
import dev.davidpalves.cookbetter.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Optional;

import static dev.davidpalves.cookbetter.auth.api.AuthenticationController.AUTH_COOKIE_EXPIRY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class AuthenticationAPITest {
    private final AuthenticationController authenticationController;
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final AuthTokenEncrypter authTokenEncrypter;

    public AuthenticationAPITest() throws NoSuchAlgorithmException, InvalidKeySpecException {
        userRepository = Mockito.mock(UserRepository.class);
        authenticationService = new AuthenticationService(userRepository);
        this.authTokenEncrypter = new AuthTokenEncrypter();
        authenticationController = new AuthenticationController(authenticationService,authTokenEncrypter);
    }


    @Test
    public void testLoginUserSuccessfully() throws Exception {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test@test.com");
        authenticationDTO.setPassword("Password123");

        User mockUser = new User();
        mockUser.setEmail("test@test.com");
        mockUser.setPassword(PasswordHasher.hash("Password123"));

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ResponseEntity<String> response = authenticationController.loginUser(authenticationDTO,servletResponse);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Cookie cookie = servletResponse.getCookie("authToken");
        assertNotNull(cookie);
        assertEquals("authToken",cookie.getName());
        assertEquals("test@test.com", this.authTokenEncrypter.decrypt(cookie.getValue()).email());
        assertEquals(AUTH_COOKIE_EXPIRY,cookie.getMaxAge());
    }

    @Test
    public void testLoginUserFailingDueToBadEmail() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test");
        authenticationDTO.setPassword("Password123");

        ResponseEntity<String> response = authenticationController.loginUser(authenticationDTO,new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authenticationDTO.setEmail("test@.org");

        response = authenticationController.loginUser(authenticationDTO,new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authenticationDTO.setEmail("");

        response = authenticationController.loginUser(authenticationDTO,new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authenticationDTO.setEmail("@test.org");

        response = authenticationController.loginUser(authenticationDTO,new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testLoginUserFailingDueToBadPassword() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test@test.com");
        authenticationDTO.setPassword("");

        ResponseEntity<String> response = authenticationController.loginUser(authenticationDTO,new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authenticationDTO.setPassword("password");

        response = authenticationController.loginUser(authenticationDTO,new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authenticationDTO.setPassword("Password");

        response = authenticationController.loginUser(authenticationDTO,new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authenticationDTO.setPassword("PASSWORD123");

        response = authenticationController.loginUser(authenticationDTO,new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authenticationDTO.setPassword("Pass1");

        response = authenticationController.loginUser(authenticationDTO,new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testLoginUserFailingDueToInexistentUser() throws SQLException {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test@test.com");
        authenticationDTO.setPassword("Password123");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        ResponseEntity<String> response = authenticationController.loginUser(authenticationDTO,new MockHttpServletResponse());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

    }

    @Test
    public void testLoginUserFailingDueToWrongPassword() throws SQLException {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test@test.com");
        authenticationDTO.setPassword("Password12");

        User mockUser = new User();
        mockUser.setEmail("test@test.com");
        //ENCODED PASSWORD123
        mockUser.setPassword("WRONGHASHEDPASSWORD");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        ResponseEntity<String> response = authenticationController.loginUser(authenticationDTO,new MockHttpServletResponse());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testRegisterFinancialUserSuccessfully() throws Exception {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test@test.com");
        authenticationDTO.setPassword("Password123");
        authenticationDTO.setUsername("TestUser");
        authenticationDTO.setName("Test Name");

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ResponseEntity<String> response = authenticationController.signupUser(authenticationDTO,servletResponse );
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Cookie cookie = servletResponse.getCookie("authToken");
        assertNotNull(cookie);
        assertEquals("authToken",cookie.getName());
        assertEquals("test@test.com", this.authTokenEncrypter.decrypt(cookie.getValue()).email());
        assertEquals(AUTH_COOKIE_EXPIRY,cookie.getMaxAge());
    }

    @Test
    public void testRegisterFinancialUserFailingDueToBadEmail() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test");
        authenticationDTO.setPassword("Password123");
        authenticationDTO.setUsername("TestUser");
        authenticationDTO.setName("Test Name");

        ResponseEntity<String> response = authenticationController.signupUser(authenticationDTO, new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authenticationDTO.setEmail("test@.org");

        response = authenticationController.signupUser(authenticationDTO, new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authenticationDTO.setEmail("");

        response = authenticationController.signupUser(authenticationDTO, new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authenticationDTO.setEmail("@test.org");

        response = authenticationController.signupUser(authenticationDTO, new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testRegisterFinancialUserFailingDueToBadPassword() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test@test.com");
        authenticationDTO.setPassword("");
        authenticationDTO.setUsername("TestUser");
        authenticationDTO.setName("Test Name");

        ResponseEntity<String> response = authenticationController.signupUser(authenticationDTO, new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authenticationDTO.setPassword("password");

        response = authenticationController.signupUser(authenticationDTO, new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authenticationDTO.setPassword("Password");

        response = authenticationController.signupUser(authenticationDTO, new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authenticationDTO.setPassword("PASSWORD123");

        response = authenticationController.signupUser(authenticationDTO, new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authenticationDTO.setPassword("Pas2WO");

        response = authenticationController.signupUser(authenticationDTO, new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testRegisterFinancialUserFailingDueToBadName() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test@test.com");
        authenticationDTO.setPassword("Password123");
        authenticationDTO.setUsername("TestUser");
        authenticationDTO.setName(" Test");

        ResponseEntity<String> response = authenticationController.signupUser(authenticationDTO, new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authenticationDTO.setName("Test ");

        response = authenticationController.signupUser(authenticationDTO, new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authenticationDTO.setName("Test");

        response = authenticationController.signupUser(authenticationDTO, new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authenticationDTO.setName("");

        response = authenticationController.signupUser(authenticationDTO, new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testRegisterFinancialUserFailingDueToBadUsername() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test@test.com");
        authenticationDTO.setPassword("Password123");
        authenticationDTO.setUsername("Test");
        authenticationDTO.setName("Test Name");

        ResponseEntity<String> response = authenticationController.signupUser(authenticationDTO, new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testRegisterFinancialUserFailingDueToExistingEmail() throws SQLException {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test@test.com");
        authenticationDTO.setPassword("Password123");
        authenticationDTO.setUsername("TestUser");
        authenticationDTO.setName("Test Name");

        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        ResponseEntity<String> response = authenticationController.signupUser(authenticationDTO, new MockHttpServletResponse());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testRegisterFinancialUserFailingDueToExistingUsername() throws SQLException {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test@test.com");
        authenticationDTO.setPassword("Password123");
        authenticationDTO.setUsername("TestUser");
        authenticationDTO.setName("Test Name");

        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("TestUser")).thenReturn(true);

        ResponseEntity<String> response = authenticationController.signupUser(authenticationDTO, new MockHttpServletResponse());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testLogout() {
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ResponseEntity<String> response = authenticationController.logoutUser(servletResponse);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Cookie cookie = servletResponse.getCookie("authToken");
        assertNotNull(cookie);
        assertEquals("authToken",cookie.getName());
        assertNull(cookie.getValue());
        assertEquals(0,cookie.getMaxAge());
    }
}
