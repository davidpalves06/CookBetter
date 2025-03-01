package dev.davidpalves.cookbetter.auth.configuration;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.Base64;

@Component
public class AuthTokenEncrypter {
    private static final byte[] salt = {123, -52, -47, -94, 97, 34, 93, 107, -62, -124, -56, -17, 52, -112, -2, 63};

    private final SecretKeySpec ENCRYPTION_KEY;
    public AuthTokenEncrypter() throws NoSuchAlgorithmException, InvalidKeySpecException {
        //TODO: GET FROM ENV. KEY AND SALT
        String sharedSecret = "TOKENENCRYPTIONKEY";
        PBEKeySpec spec = new PBEKeySpec(sharedSecret.toCharArray(), salt, 65536, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        this.ENCRYPTION_KEY = new SecretKeySpec(keyBytes, "AES");
    }
    public String encrypt(AuthToken authToken) throws Exception {
        String token = authToken.email() + "||" + authToken.refreshDate() + "||" + authToken.expirationDate();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, ENCRYPTION_KEY);
        byte[] encrypted = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public AuthToken decrypt(String encryptedToken) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, ENCRYPTION_KEY);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedToken));
        String token = new String(decrypted, StandardCharsets.UTF_8);
        String[] split = token.split("\\|\\|");
        if (split.length != 3) {
            throw new Exception("Invalid token");
        }
        else {
            return new AuthToken(split[0], LocalDateTime.parse(split[1]),LocalDateTime.parse(split[2]));
        }
    }
}
