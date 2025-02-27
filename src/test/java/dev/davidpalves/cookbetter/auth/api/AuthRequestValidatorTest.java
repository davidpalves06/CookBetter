package dev.davidpalves.cookbetter.auth.api;

import dev.davidpalves.cookbetter.auth.dto.AuthenticationDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthRequestValidatorTest {
    @Test
    public void testValidLoginDTO() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test@test.com");
        authenticationDTO.setPassword("Password123");

        assertTrue(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));
    }

    @Test
    public void testLoginFinancialUserFailingDueToBadEmail() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test");
        authenticationDTO.setPassword("Password123");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));

        authenticationDTO.setEmail("test@.org");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));

        authenticationDTO.setEmail("");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));

        authenticationDTO.setEmail("@test.org");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));
    }

    @Test
    public void testLoginFinancialUserFailingDueToBadPassword() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test@test.com");
        authenticationDTO.setPassword("");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));

        authenticationDTO.setPassword("password");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));

        authenticationDTO.setPassword("Password");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));

        authenticationDTO.setPassword("PASSWORD123");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));

        authenticationDTO.setPassword("Pass1");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));
    }

    @Test
    public void testRegisterFinancialUserSuccessfully() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test@test.com");
        authenticationDTO.setPassword("Password123");
        authenticationDTO.setUsername("TestUser");
        authenticationDTO.setName("Test Name");

        assertTrue(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));

    }

    @Test
    public void testRegisterFinancialUserFailingDueToBadEmail() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test");
        authenticationDTO.setPassword("Password123");
        authenticationDTO.setUsername("TestUser");
        authenticationDTO.setName("Test Name");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));

        authenticationDTO.setEmail("test@.org");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));

        authenticationDTO.setEmail("");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));

        authenticationDTO.setEmail("@test.org");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));
    }

    @Test
    public void testRegisterFinancialUserFailingDueToBadPassword() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test@test.com");
        authenticationDTO.setPassword("");
        authenticationDTO.setUsername("TestUser");
        authenticationDTO.setName("Test Name");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));

        authenticationDTO.setPassword("password");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));

        authenticationDTO.setPassword("Password");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));

        authenticationDTO.setPassword("PASSWORD123");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));

        authenticationDTO.setPassword("Pas2WO");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));
    }

    @Test
    public void testRegisterFinancialUserFailingDueToBadName() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test@test.com");
        authenticationDTO.setPassword("Password123");
        authenticationDTO.setUsername("TestUser");
        authenticationDTO.setName(" Test");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));

        authenticationDTO.setName("Test ");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));

        authenticationDTO.setName("Test");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));

        authenticationDTO.setName("");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));
    }

    @Test
    public void testRegisterFinancialUserFailingDueToBadUsername() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("test@test.com");
        authenticationDTO.setPassword("Password123");
        authenticationDTO.setUsername("Test");
        authenticationDTO.setName("Test Name");

        assertFalse(AuthenticationRequestValidator.validateLoginDTO(authenticationDTO));
    }

}
