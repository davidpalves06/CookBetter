package dev.davidpalves.cookbetter.recipes.api;

import dev.davidpalves.cookbetter.models.ServiceResult;
import dev.davidpalves.cookbetter.recipes.dto.RecipeDTO;
import dev.davidpalves.cookbetter.recipes.dto.RecipesDTO;
import dev.davidpalves.cookbetter.recipes.service.RecipeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/recipes")
@Slf4j
public class RecipeController {

    private static final String LOG_TITLE = "[RecipeController] -";
    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping(value = {"/user/{userId}"})
    public ResponseEntity<RecipesDTO> getRecipes(@PathVariable String userId) {
        return getRecipesForUser(userId);
    }

    @GetMapping(value = {"/user"})
    public ResponseEntity<RecipesDTO> getRecipesForAuthenticatedUser(HttpServletRequest request) {
        String userId = request.getAttribute("userId").toString();
        return getRecipesForUser(userId);
    }

    private ResponseEntity<RecipesDTO> getRecipesForUser(String userId) {
        log.info("{} Get recipes for user {} request received", LOG_TITLE, userId);
        ServiceResult<RecipesDTO> serviceResult = recipeService.getRecipesForUser(userId);
        if (serviceResult.isSuccess()) {
            log.info("{} Recipes found successfully", LOG_TITLE);
            return new ResponseEntity<>(serviceResult.getData(), HttpStatus.OK);
        }
        else {
            log.info("{} Recipes search failed due to {}.", LOG_TITLE,serviceResult.getErrorMessage());
            if (serviceResult.getErrorCode() == 2) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<Void> createRecipe(HttpServletRequest request, RecipeDTO recipeDTO) {
        log.info("{} Create recipe request received", LOG_TITLE);
        String userId = request.getAttribute("userId").toString();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (recipeDTO.getIngredients() == null || recipeDTO.getIngredients().isEmpty() || recipeDTO.getInstructions() == null || recipeDTO.getInstructions().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ServiceResult<String> serviceResult = recipeService.createRecipe(recipeDTO,userId);
        if (serviceResult.isSuccess()) {
            log.info("{} Recipe created successfully", LOG_TITLE);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        else {
            log.info("{} Recipe creation failed due to {}.", LOG_TITLE,serviceResult.getErrorMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{recipeId}")
    public ResponseEntity<RecipeDTO> getRecipe(@PathVariable String recipeId) {
        log.info("{} Get recipe {} request received", LOG_TITLE, recipeId);
        ServiceResult<RecipeDTO> serviceResult = recipeService.getRecipe(recipeId);
        if (serviceResult.isSuccess()) {
            log.info("{} Recipe found successfully", LOG_TITLE);
            return new ResponseEntity<>(serviceResult.getData(),HttpStatus.OK);
        }
        else {
            log.info("{} Recipe search failed due to {}.", LOG_TITLE,serviceResult.getErrorMessage());
            if (serviceResult.getErrorCode() == 2) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
