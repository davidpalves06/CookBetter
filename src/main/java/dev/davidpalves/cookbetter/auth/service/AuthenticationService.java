package dev.davidpalves.cookbetter.auth.service;

import dev.davidpalves.cookbetter.auth.dto.AuthenticationDTO;
import dev.davidpalves.cookbetter.models.ServiceResult;
import dev.davidpalves.cookbetter.models.User;
import dev.davidpalves.cookbetter.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Optional;

@Service
@Slf4j
public class AuthenticationService {
    private static final String LOG_TITLE = "[AuthenticationService] -";

    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public ServiceResult<String> signupUser(AuthenticationDTO userDTO) {
        log.debug("{} Registering user with email {}", LOG_TITLE,userDTO.getEmail());
        ServiceResult<String> serviceResult;
        try {
            String password = userDTO.getPassword();
            String encodedPassword = PasswordHasher.hash(password);
            User user = new User();
            user.setPassword(encodedPassword);
            user.setUsername(userDTO.getUsername());
            user.setEmail(userDTO.getEmail());
            String[] fullNameString = userDTO.getName().trim().split(" ");
            user.setName(fullNameString[0] + " " + fullNameString[fullNameString.length - 1]);
            userRepository.startConnection();
            if (!userRepository.existsByEmail(user.getEmail())) {
                if (userRepository.existsByUsername(user.getUsername())) {
                    log.debug("{} Could not register user because username was already taken", LOG_TITLE);
                    serviceResult = new ServiceResult<>(false, null, "Username already taken", 1);
                } else {
                    log.debug("{} Saving user in database", LOG_TITLE);
                    if (userRepository.save(user) != -1) {
                        serviceResult = new ServiceResult<>(true,"User registered.",null,0);
                    } else {
                        serviceResult = new ServiceResult<>(false,"User could not be registered",null,2);
                    }
                }
            } else {
                log.debug("{} Could not register user because email was already taken", LOG_TITLE);
                serviceResult = new ServiceResult<>(false,null,"Email already taken",1);
            }
            userRepository.closeConnection();
            return serviceResult;
        } catch (SQLException | NoSuchAlgorithmException e) {
            log.error(String.valueOf(e));
            return new ServiceResult<>(false,null,"Internal Error",2);
        }
    }

    public ServiceResult<String> loginUser(AuthenticationDTO userDTO) {
        log.debug("{} Checking if user exists", LOG_TITLE);
        try {
            userRepository.startConnection();
            Optional<User> userOptional = userRepository.findByEmail(userDTO.getEmail());
            userRepository.closeConnection();
            if (userOptional.isPresent()) {
                log.debug("{} User exists", LOG_TITLE);
                User user = userOptional.get();
                if (PasswordHasher.matches(userDTO.getPassword(), user.getPassword())) {
                    log.debug("{} Password matches", LOG_TITLE);
                    return new ServiceResult<>(true,user.getUsername(),null,0);
                }
                else {
                    log.debug("{} Password does not match", LOG_TITLE);
                    return new ServiceResult<>(false,null,"Wrong password",1);
                }
            }
            else {
                log.debug("{} User does not exist", LOG_TITLE);
                return new ServiceResult<>(false,null,"User not found",1);
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            return new ServiceResult<>(false,null,"Internal Error",2);
        }
    }


}