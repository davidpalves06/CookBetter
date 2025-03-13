package dev.davidpalves.cookbetter.profile.api;

import dev.davidpalves.cookbetter.models.ServiceResult;
import dev.davidpalves.cookbetter.profile.dto.ProfileDTO;
import dev.davidpalves.cookbetter.profile.service.ProfileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@Slf4j
public class ProfileController {
    private static final String LOG_TITLE = "[ProfileController] -";
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }


    @GetMapping("/{username}")
    public ResponseEntity<ProfileDTO> getProfileByUsername(HttpServletResponse response, @PathVariable String username) {
        log.info("{} Get username by username request received", LOG_TITLE);
        ServiceResult<ProfileDTO> serviceResult = profileService.getProfileByUsername(username);
        if (serviceResult.isSuccess()) {
            log.info("{} Profile found successfully", LOG_TITLE);
            return new ResponseEntity<>(serviceResult.getData(),HttpStatus.OK);
        }
        else {
            log.info("{} Profile search failed due to {}.", LOG_TITLE,serviceResult.getErrorMessage());
            if (serviceResult.getErrorCode() == 2) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
