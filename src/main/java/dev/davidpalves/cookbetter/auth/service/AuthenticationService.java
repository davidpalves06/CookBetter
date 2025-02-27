package dev.davidpalves.cookbetter.auth.service;

import dev.davidpalves.cookbetter.auth.configuration.AuthTokenEncrypter;
import dev.davidpalves.cookbetter.auth.dto.AuthenticationDTO;
import dev.davidpalves.cookbetter.auth.dto.AuthenticationResult;
import dev.davidpalves.cookbetter.models.ServiceResult;
import dev.davidpalves.cookbetter.models.User;
import dev.davidpalves.cookbetter.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
@Slf4j
public class AuthenticationService {
    private static final String LOG_TITLE = "[AuthenticationService] -";

    private final UserRepository userRepository;
    private final AuthTokenEncrypter authTokenEncrypter;

    public AuthenticationService(UserRepository userRepository, AuthTokenEncrypter authTokenEncrypter) {
        this.userRepository = userRepository;
        this.authTokenEncrypter = authTokenEncrypter;
    }


    public ServiceResult<AuthenticationResult> registerUser(AuthenticationDTO userDTO) throws NoSuchAlgorithmException {
        log.debug("{} Registering user with email {}", LOG_TITLE,userDTO.getEmail());
        String password = userDTO.getPassword();
        String encodedPassword = PasswordHasher.hash(password);
        User financialUser = new User();
//        financialUser.setPassword(encodedPassword);
//        financialUser.setUsername(userDTO.getUsername());
//        financialUser.setEmail(userDTO.getEmail());
//        String[] fullNameString = userDTO.getName().trim().split(" ");
//        financialUser.setName(fullNameString[0] + " " + fullNameString[fullNameString.length - 1]);
//        if (!userRepository.existsByEmail(financialUser.getEmail())) {
//            if (userRepository.existsByUsername(financialUser.getUsername())) {
//                return new ServiceResult<>(false,null,"Username already taken",2);
//            }
//            log.debug("{} Saving user in database", LOG_TITLE);
//            userRepository.save(financialUser);
//            return new ServiceResult<>(true,new AuthenticationResult("User registered."),null,0);
//        }
        log.debug("{} Could not register user because email was already taken", LOG_TITLE);
        return new ServiceResult<>(false,null,"Email already taken",1);
    }

    public ServiceResult<AuthenticationResult> loginUser(AuthenticationDTO userDTO) {
        log.debug("{} Checking if user exists", LOG_TITLE);
//        Optional<User> financialUserOptional = userRepository.findByEmail(userDTO.getEmail());
//        if (financialUserOptional.isPresent()) {
//            log.debug("{} User exists", LOG_TITLE);
//            User financialUser = financialUserOptional.get();
//            if (PasswordHasher.matches(userDTO.getPassword(), financialUser.getPassword())) {
//                log.debug("{} Password matches", LOG_TITLE);
//                log.debug("{} Generating Access Token", LOG_TITLE);
//                String accessToken = jwtUtils.createAccessToken(financialUser.getEmail());
//                AuthenticationDTO userInfo = new AuthenticationDTO();
//                userInfo.setEmail(financialUser.getEmail());
//                userInfo.setUsername(financialUser.getUsername());
//                userInfo.setName(financialUser.getName());
//                return new ServiceResult<>(true,new AuthenticationResult(accessToken,"User logged in.",userInfo),null,0);
//            }
//            else {
//                log.debug("{} Password does not match", LOG_TITLE);
//                return new ServiceResult<>(false,null,"Wrong password",1);
//            }
//        }
//        else {
//            log.debug("{} User does not exist", LOG_TITLE);
            return new ServiceResult<>(false,null,"User not found",2);
//        }
    }


    public ServiceResult<AuthenticationResult> checkAuthentication(Cookie[] cookies) {
        if (cookies == null) {
            log.debug("{} No authentication cookie", LOG_TITLE);
            return new ServiceResult<>(false,null,"User not authenticated",1);
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("authToken")) {
                String token = cookie.getValue();
//                Boolean validated = jwtUtils.validateToken(token);
//                if (validated) {
//                    String email = jwtUtils.extractEmail(token);
//                    Optional<User> financialUserOptional = userRepository.findByEmail(email);
//                    if (financialUserOptional.isPresent()) {
//                        User financialUser = financialUserOptional.get();
//                        String accessToken = jwtUtils.createAccessToken(email);
//                        AuthenticationDTO userInfo = new AuthenticationDTO();
//                        userInfo.setEmail(email);
//                        userInfo.setUsername(financialUser.getUsername());
//                        userInfo.setName(financialUser.getName());
//                        return new ServiceResult<>(true,new AuthenticationResult(accessToken,"User is authenticated.",userInfo),null,0);
//                    }
//                    else log.debug("{} Could not find user in JWT", LOG_TITLE);
//                }
//                else log.debug("{} JWT Token not valid", LOG_TITLE);
                return new ServiceResult<>(false,null,"User not authenticated",1);
            }
        }
        log.debug("{} No authentication cookie", LOG_TITLE);
        return new ServiceResult<>(false,null,"User not authenticated",1);
    }
}