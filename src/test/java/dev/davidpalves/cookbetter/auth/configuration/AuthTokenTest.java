package dev.davidpalves.cookbetter.auth.configuration;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AuthTokenTest {

    private final AuthTokenEncrypter authTokenEncrypter;

    public AuthTokenTest() throws NoSuchAlgorithmException, InvalidKeySpecException {
        authTokenEncrypter = new AuthTokenEncrypter();
    }

    @Test
    public void testValidAuthToken() throws Exception {
        AuthToken authToken = new AuthToken("test@gmail.com", LocalDateTime.now().plusDays(1));
        String encrypted = authTokenEncrypter.encrypt(authToken);

        AuthToken decryptedToken = authTokenEncrypter.decrypt(encrypted);
        assertEquals(decryptedToken, authToken);
        assertFalse(decryptedToken.isExpired());
    }

    @Test
    public void testExpiredAuthToken() throws Exception {
        AuthToken authToken = new AuthToken("test@gmail.com", LocalDateTime.now().minusDays(1));
        String encrypted = authTokenEncrypter.encrypt(authToken);

        AuthToken decryptedToken = authTokenEncrypter.decrypt(encrypted);
        assertEquals(decryptedToken, authToken);
        assertTrue(decryptedToken.isExpired());
    }
}
