package dev.davidpalves.cookbetter.profile.api;

import dev.davidpalves.cookbetter.models.ServiceResult;
import dev.davidpalves.cookbetter.profile.dto.ProfileDTO;
import dev.davidpalves.cookbetter.profile.service.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@Slf4j
public class ProfileController {
    private static final String LOG_TITLE = "[ProfileController] -";
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }


    @GetMapping
    public ResponseEntity<ProfileDTO> getProfile(@RequestParam(value = "userId", required = false) String userId,
                                                           @RequestParam(value = "username", required = false) String username) {
        if (userId != null) {
            return getProfileByUserId(userId);
        } else if (username != null) {
            return getProfileByUsername(username);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<ProfileDTO> getProfileByUserId(String userId) {
        log.info("{} Get profile by userId request received", LOG_TITLE);
        ServiceResult<ProfileDTO> serviceResult = profileService.getProfileByUserId(userId);
        if (serviceResult.isSuccess()) {
            log.info("{} Profile found successfully", LOG_TITLE);
            return new ResponseEntity<>(serviceResult.getData(), HttpStatus.OK);
        }
        else {
            log.info("{} Profile search failed due to {}.", LOG_TITLE,serviceResult.getErrorMessage());
            if (serviceResult.getErrorCode() == 2) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private ResponseEntity<ProfileDTO> getProfileByUsername(String username) {
        log.info("{} Get profile by username request received", LOG_TITLE);
        ServiceResult<ProfileDTO> serviceResult = profileService.getProfileByUsername(username);
        if (serviceResult.isSuccess()) {
            log.info("{} Profile found successfully", LOG_TITLE);
            return new ResponseEntity<>(serviceResult.getData(), HttpStatus.OK);
        }
        else {
            log.info("{} Profile search failed due to {}.", LOG_TITLE,serviceResult.getErrorMessage());
            if (serviceResult.getErrorCode() == 2) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value = "/{username}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateProfileByUsername(@PathVariable String username, @ModelAttribute ProfileDTO profileDTO,
                                                        @RequestParam(value = "image", required = false) MultipartFile image) {
        String authUsername = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        if (!authUsername.equals(username)) {
            log.warn("{} Wrong user {} tried to update profile {}", LOG_TITLE, authUsername, username);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        profileDTO.setDescription(profileDTO.getDescription().trim().replace("\r\n","\n"));
        if (profileDTO.getDescription().length() > 250) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        log.info("{} Update profile by username request received", LOG_TITLE);
        ServiceResult<ProfileDTO> serviceResult = profileService.updateProfileByUsername(username,profileDTO,image);
        if (serviceResult.isSuccess()) {
            log.info("{} Profile updated successfully", LOG_TITLE);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else {
            log.info("{} Profile update failed due to {}.", LOG_TITLE,serviceResult.getErrorMessage());
            if (serviceResult.getErrorCode() == 2) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
