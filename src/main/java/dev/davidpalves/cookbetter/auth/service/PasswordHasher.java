package dev.davidpalves.cookbetter.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class PasswordHasher {

    public static String hash(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hashBytes).toLowerCase();
    }

    public static boolean matches(String password, String hashedPassword) throws NoSuchAlgorithmException {
        String newHash = hash(password);
        return hashedPassword.equals(newHash);
    }
}
