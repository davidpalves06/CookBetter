package dev.davidpalves.cookbetter.auth.service;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordHasherTest {
    @Test
    public void testHashPassword() throws NoSuchAlgorithmException {
        String password = "password";
        String hashed = PasswordHasher.hash(password);
        assertTrue(PasswordHasher.matches(password, hashed));
    }

    @Test
    public void testDifferentHashPassword() throws NoSuchAlgorithmException {
        String password = "password";
        String hashed = PasswordHasher.hash(password);
        assertFalse(PasswordHasher.matches("TestFail", hashed));
    }
}
