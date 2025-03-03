package dev.davidpalves.cookbetter.integration.auth;

import dev.davidpalves.cookbetter.auth.dto.AuthenticationDTO;
import dev.davidpalves.cookbetter.auth.dto.AuthenticationResponse;
import dev.davidpalves.cookbetter.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AuthenticationTestIT {
        @Autowired
        private TestRestTemplate restTemplate;
        @Autowired
        private UserRepository userRepository;

        @AfterEach
        public void cleanUp() throws SQLException {
            userRepository.deleteAllUsers();
        }


        @Test
        public void testAuthenticationUser() {
            AuthenticationDTO registerDTO = new AuthenticationDTO();
            registerDTO.setEmail("test@test.com");
            registerDTO.setPassword("Password123");
            registerDTO.setUsername("TestUser");
            registerDTO.setName("Test Name");

            ResponseEntity<AuthenticationResponse> registerResponse = restTemplate.postForEntity("/auth/signup",registerDTO, AuthenticationResponse.class);
            assertEquals(HttpStatus.CREATED,registerResponse.getStatusCode());

            AuthenticationDTO loginDTO = new AuthenticationDTO();
            loginDTO.setEmail("test@test.com");
            loginDTO.setPassword("Password123");
            ResponseEntity<AuthenticationResponse> loginResponse = restTemplate.postForEntity("/auth/login",loginDTO, AuthenticationResponse.class);
            assertEquals(HttpStatus.OK,loginResponse.getStatusCode());

            String accessToken = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE).get(0).split(";")[0].substring(10);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", "authToken="+accessToken);
            headers.set("Content-Type", "application/json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<AuthenticationResponse> validateLogin = restTemplate.exchange("/auth/verify", HttpMethod.GET,entity, AuthenticationResponse.class);
            assertEquals(HttpStatus.OK,validateLogin.getStatusCode());
        }
}
