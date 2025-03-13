package dev.davidpalves.cookbetter.profile.service;

import dev.davidpalves.cookbetter.models.ServiceResult;
import dev.davidpalves.cookbetter.models.User;
import dev.davidpalves.cookbetter.profile.dto.ProfileDTO;
import dev.davidpalves.cookbetter.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;

@Service
@Slf4j
public class ProfileService {

    private static final String LOG_TITLE = "[ProfileService] -";

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ServiceResult<ProfileDTO> getProfileByUsername(String username) {
        log.debug("{} Get profile by username {}", LOG_TITLE,username);
        ServiceResult<ProfileDTO> serviceResult;
        try {
            userRepository.startConnection();
            Optional<User> optionalUser = userRepository.findByUsername(username);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                ProfileDTO profileDTO = new ProfileDTO(user.getName(),user.getUsername());
                log.debug("{} Profile found {}", LOG_TITLE, profileDTO);
                serviceResult = new ServiceResult<>(true, profileDTO, null, 0);
            } else {
                log.debug("{} Profile not found", LOG_TITLE);
                serviceResult = new ServiceResult<>(false,null,"Profile Not found",1);
            }
            userRepository.closeConnection();
            return serviceResult;
        } catch (SQLException e) {
            log.error(String.valueOf(e));
            return new ServiceResult<>(false,null,"Internal Error",2);
        }
    }
}
