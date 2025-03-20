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
        AuthToken authToken = new AuthToken("test@gmail.com","userId",LocalDateTime.now().plusDays(1) ,LocalDateTime.now().plusDays(1));
        String encrypted = authTokenEncrypter.encrypt(authToken);

        AuthToken decryptedToken = authTokenEncrypter.decrypt(encrypted);
        assertEquals(decryptedToken, authToken);
        assertFalse(decryptedToken.isExpired());
    }

    @Test
    public void testExpiredAuthToken() throws Exception {
        AuthToken authToken = new AuthToken("test@gmail.com","userId",LocalDateTime.now().plusDays(1), LocalDateTime.now().minusDays(1));
        String encrypted = authTokenEncrypter.encrypt(authToken);

        AuthToken decryptedToken = authTokenEncrypter.decrypt(encrypted);
        assertEquals(decryptedToken, authToken);
        assertTrue(decryptedToken.isExpired());
    }

    @Test
    public void testRefreshAuthToken() throws Exception {
        AuthToken authToken = new AuthToken("test@gmail.com","userId",LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusDays(1));
        String encrypted = authTokenEncrypter.encrypt(authToken);

        AuthToken decryptedToken = authTokenEncrypter.decrypt(encrypted);
        assertEquals(decryptedToken, authToken);
        assertTrue(decryptedToken.needsRefresh());
    }
}
