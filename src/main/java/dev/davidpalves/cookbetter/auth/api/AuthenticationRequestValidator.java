package dev.davidpalves.cookbetter.auth.api;

import dev.davidpalves.cookbetter.auth.dto.AuthenticationDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationRequestValidator {
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
            "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$";
    private static final String LOG_TITLE = "[AuthenticationRequestValidator] -";

    public static boolean validateRegisterDTO(AuthenticationDTO userDTO) {
        return validatePassword(userDTO.getPassword())
                && validateName(userDTO.getName())
                && validateEmail(userDTO.getEmail())
                && validateUsername(userDTO.getUsername());
    }

    public static boolean validateLoginDTO(AuthenticationDTO userDTO) {
        return validatePassword(userDTO.getPassword())
                && validateEmail(userDTO.getEmail());
    }

    private static boolean validateEmail(String email) {
        boolean validEmail = email != null && !email.isEmpty() && email.matches(EMAIL_REGEX);
        if (!validEmail) {
            log.debug("{} Invalid email address: {}",LOG_TITLE, email);
        }
        return validEmail;
    }

    private static boolean validatePassword(String password) {
        boolean validPassword = password != null && password.length() >= 8
                && password.length() <= 32 && password.matches(PASSWORD_REGEX);
        if (!validPassword) {
            log.debug("{} Invalid password: {}",LOG_TITLE, password);
        }
        return validPassword;
    }

    private static boolean validateName(String name) {
        boolean validName = name != null && !name.isEmpty() && name.trim().split(" ").length >= 2;
        if (!validName) {
            log.debug("{} Invalid name: {}",LOG_TITLE, name);
        }
        return validName;
    }

    private static boolean validateUsername(String username) {
        boolean validUsername = username != null && username.length() >= 6;
        if (!validUsername) {
            log.debug("{} Invalid username: {}",LOG_TITLE, username);
        }
        return validUsername;
    }
}
