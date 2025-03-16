package dev.davidpalves.cookbetter.profile.service;

import dev.davidpalves.cookbetter.models.ServiceResult;
import dev.davidpalves.cookbetter.models.User;
import dev.davidpalves.cookbetter.profile.dto.ProfileDTO;
import dev.davidpalves.cookbetter.profile.models.Profile;
import dev.davidpalves.cookbetter.profile.repository.ProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;

@Service
@Slf4j
public class ProfileService {

    private static final String LOG_TITLE = "[ProfileService] -";

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public ServiceResult<ProfileDTO> createProfile(ProfileDTO profileDTO) {
        log.debug("{} Create profile : {}", LOG_TITLE,profileDTO);
        ServiceResult<ProfileDTO> serviceResult;
        try {
            profileRepository.startConnection();
            Profile profile = new Profile(profileDTO.getUserId(),profileDTO.getUsername(),profileDTO.getName());
            String id = profileRepository.save(profile);
            if (id != null) {
                log.debug("{} Profile created {}", LOG_TITLE, profileDTO);
                serviceResult = new ServiceResult<>(true, profileDTO, null, 0);
            } else {
                log.debug("{} Error creating profile", LOG_TITLE);
                serviceResult = new ServiceResult<>(false,null,"Error creating profile",1);
            }
            profileRepository.closeConnection();
            return serviceResult;
        } catch (SQLException e) {
            log.error(String.valueOf(e));
            return new ServiceResult<>(false,null,"Internal Error",2);
        }
    }

    public ServiceResult<ProfileDTO> getProfileByUsername(String username) {
        log.debug("{} Get profile by username {}", LOG_TITLE,username);
        ServiceResult<ProfileDTO> serviceResult;
        try {
            profileRepository.startConnection();
            Optional<Profile> optionalProfile = profileRepository.findByUsername(username);
            if (optionalProfile.isPresent()) {
                Profile profile = optionalProfile.get();
                ProfileDTO profileDTO = new ProfileDTO(profile.getUsername(),profile.getName(),
                        profile.getDescription(), profile.getAvatarPhoto(), profile.getFollowers(),
                        profile.getFollowing(), profile.getRecipes());
                log.debug("{} Profile found {}", LOG_TITLE, profileDTO);
                serviceResult = new ServiceResult<>(true, profileDTO, null, 0);
            } else {
                log.debug("{} Profile not found", LOG_TITLE);
                serviceResult = new ServiceResult<>(false,null,"Profile Not found",1);
            }
            profileRepository.closeConnection();
            return serviceResult;
        } catch (SQLException e) {
            log.error(String.valueOf(e));
            return new ServiceResult<>(false,null,"Internal Error",2);
        }
    }
}
