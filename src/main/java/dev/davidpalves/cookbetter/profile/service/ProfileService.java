package dev.davidpalves.cookbetter.profile.service;

import dev.davidpalves.cookbetter.models.ServiceResult;
import dev.davidpalves.cookbetter.profile.dto.ProfileDTO;
import dev.davidpalves.cookbetter.profile.models.Profile;
import dev.davidpalves.cookbetter.profile.repository.ProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ProfileService {

    private static final String LOG_TITLE = "[ProfileService] -";
    //TODO: THIS SHOULD BE ANOTHER FOLDER. Fix FilePath using "src/main/resources/static/"
    private static final String PROFILE_PHOTO_DIR = "profile/assets/";

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
        File file = new File("src/main/resources/static/" + PROFILE_PHOTO_DIR);
        boolean created = file.mkdirs();
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
                ProfileDTO profileDTO = new ProfileDTO(profile.getUserId(),profile.getUsername(),profile.getName(),
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

    public ServiceResult<ProfileDTO> updateProfileByUsername(String username, ProfileDTO profileDTO, MultipartFile image) {
        log.debug("{} Update profile by username {}", LOG_TITLE,username);
        ServiceResult<ProfileDTO> serviceResult;
        try {
            profileRepository.startConnection();
            Optional<Profile> optionalProfile = profileRepository.findByUsername(username);
            if (optionalProfile.isPresent()) {
                Profile profile = optionalProfile.get();
                if (profileDTO.getDescription() != null && !profileDTO.getDescription().equals(profile.getDescription())) {
                    profile.setDescription(profileDTO.getDescription());
                }
                String imageUrl = getImageUrl(image);
                profile.setAvatarPhoto(imageUrl);
                if (profileDTO.getAvatarPhoto() != null && !profileDTO.getAvatarPhoto().equals(profile.getAvatarPhoto())) {
                    profile.setAvatarPhoto(profileDTO.getAvatarPhoto());
                }
                if (profileRepository.update(profile)) {
                    log.debug("{} Profile updated {}", LOG_TITLE, profileDTO);
                    serviceResult = new ServiceResult<>(true, profileDTO, null, 0);
                } else {
                    log.debug("{} Error updating profile", LOG_TITLE);
                    serviceResult = new ServiceResult<>(false,null,"Internal Error",2);
                }
            } else {
                log.debug("{} Profile not found", LOG_TITLE);
                serviceResult = new ServiceResult<>(false,null,"Profile Not found",1);
            }
            profileRepository.closeConnection();
            return serviceResult;
        } catch (SQLException | IOException e) {
            log.error(String.valueOf(e));
            profileRepository.rollbackConnection();
            return new ServiceResult<>(false,null,"Internal Error",2);
        }
    }

    public ServiceResult<Integer> addRecipeToProfile(String userId) {
        log.debug("{} Increment recipe count in profile  {}", LOG_TITLE,userId);
        ServiceResult<Integer> serviceResult;
        try {
            profileRepository.startConnection();
            Optional<Profile> optionalProfile = profileRepository.findByUserID(userId);
            if (optionalProfile.isPresent()) {
                Profile profile = optionalProfile.get();
                profile.setRecipes(profile.getRecipes() + 1);
                if (profileRepository.update(profile)) {
                    log.debug("{} Number of recipes updated to {}", LOG_TITLE,profile.getRecipes());
                    serviceResult = new ServiceResult<>(true, profile.getRecipes(), null, 0);
                } else {
                    log.debug("{} Error updating profile", LOG_TITLE);
                    serviceResult = new ServiceResult<>(false,null,"Internal Error",2);
                }
            } else {
                log.debug("{} Profile not found", LOG_TITLE);
                serviceResult = new ServiceResult<>(false,null,"Profile Not found",1);
            }
            return serviceResult;
        } catch (SQLException e) {
            log.error(String.valueOf(e));
            return new ServiceResult<>(false,null,"Internal Error",2);
        }
    }

    public ServiceResult<Integer> deleteRecipeFromProfile(String userId) {
        log.debug("{} Decrement recipe count in profile  {}", LOG_TITLE,userId);
        ServiceResult<Integer> serviceResult;
        try {
            profileRepository.startConnection();
            Optional<Profile> optionalProfile = profileRepository.findByUserID(userId);
            if (optionalProfile.isPresent()) {
                Profile profile = optionalProfile.get();
                profile.setRecipes(profile.getRecipes() - 1);
                if (profileRepository.update(profile)) {
                    log.debug("{} Number of recipes updated to {}", LOG_TITLE,profile.getRecipes());
                    serviceResult = new ServiceResult<>(true, profile.getRecipes(), null, 0);
                } else {
                    log.debug("{} Error updating profile", LOG_TITLE);
                    serviceResult = new ServiceResult<>(false,null,"Internal Error",2);
                }
            } else {
                log.debug("{} Profile not found", LOG_TITLE);
                serviceResult = new ServiceResult<>(false,null,"Profile Not found",1);
            }
            return serviceResult;
        } catch (SQLException e) {
            log.error(String.valueOf(e));
            return new ServiceResult<>(false,null,"Internal Error",2);
        }
    }

    private String getImageUrl(MultipartFile image) throws IOException {
        String imageUrl = "";
        if (image != null && !image.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path filePath = Paths.get("src/main/resources/static/" + PROFILE_PHOTO_DIR + fileName);
            Files.write(filePath, image.getBytes());
            imageUrl = "/" + PROFILE_PHOTO_DIR + fileName;
        }
        return imageUrl;
    }
}
