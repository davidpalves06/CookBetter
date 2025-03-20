package dev.davidpalves.cookbetter.recipes.service;

import dev.davidpalves.cookbetter.models.ServiceResult;
import dev.davidpalves.cookbetter.profile.service.ProfileService;
import dev.davidpalves.cookbetter.recipes.dto.RecipeDTO;
import dev.davidpalves.cookbetter.recipes.dto.RecipesDTO;
import dev.davidpalves.cookbetter.recipes.models.Recipe;
import dev.davidpalves.cookbetter.recipes.repository.RecipeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RecipeService {
    private static final String LOG_TITLE = "[RecipeService] -";

    private final RecipeRepository recipeRepository;
    private final ProfileService profileService;


    public RecipeService(RecipeRepository recipeRepository, ProfileService profileService) {
        this.recipeRepository = recipeRepository;
        this.profileService = profileService;
    }

    public ServiceResult<RecipesDTO> getRecipes() {
        return new ServiceResult<>();
    }

    public ServiceResult<String> createRecipe(RecipeDTO recipeDTO,String userId) {
        log.debug("{} Create recipe : {}", LOG_TITLE,recipeDTO);
        ServiceResult<String> serviceResult;
        try {
            recipeRepository.startConnection();
            Recipe recipe = new Recipe();
            recipe.setUserId(userId);
            recipe.setTitle(recipeDTO.getTitle());
            recipe.setDescription(recipeDTO.getDescription());
            recipe.setImage(recipeDTO.getImage());
            recipe.setInstructions(recipeDTO.getInstructions());
            recipe.setIngredients(recipeDTO.getIngredients());
            recipe.setInstructions(recipeDTO.getInstructions());
            String id = recipeRepository.save(recipe);
            if (id != null) {
                ServiceResult<Integer> addRecipeToProfile = profileService.addRecipeToProfile(userId);
                if (addRecipeToProfile.isSuccess()) {
                    log.debug("{} Recipe created with id {}", LOG_TITLE, id);
                    serviceResult = new ServiceResult<>(true, "Recipe created", null, 0);
                    recipeRepository.closeConnection();
                } else {
                    log.debug("{} Error updating recipe count ", LOG_TITLE);
                    serviceResult = new ServiceResult<>(false, null, "Error creating recipe", 1);
                    recipeRepository.rollbackConnection();
                }
            } else {
                log.debug("{} Error creating recipe", LOG_TITLE);
                serviceResult = new ServiceResult<>(false,null,"Error creating recipe",1);
                recipeRepository.rollbackConnection();
            }
            return serviceResult;
        } catch (SQLException e) {
            log.error(String.valueOf(e));
            recipeRepository.rollbackConnection();
            return new ServiceResult<>(false,null,"Internal Error",1);
        }
    }

    public ServiceResult<RecipeDTO> getRecipe(String recipeId) {
        log.debug("{} Get recipe : {}", LOG_TITLE,recipeId);
        ServiceResult<RecipeDTO> serviceResult;
        try {
            recipeRepository.startConnection();
            Optional<Recipe> optionalRecipe = recipeRepository.findById(recipeId);
            if (optionalRecipe.isPresent()) {
                RecipeDTO recipeDTO = buildRecipeDTO(optionalRecipe.get());
                log.debug("{} Recipe found {}", LOG_TITLE, recipeDTO);
                serviceResult = new ServiceResult<>(true, recipeDTO, null, 0);
                recipeRepository.closeConnection();
            } else {
                log.debug("{} Error founding recipe", LOG_TITLE);
                serviceResult = new ServiceResult<>(false,null,"Error founding recipe",1);
                recipeRepository.rollbackConnection();
            }
            return serviceResult;
        } catch (SQLException e) {
            log.error(String.valueOf(e));
            recipeRepository.rollbackConnection();
            return new ServiceResult<>(false,null,"Internal Error",2);
        }
    }

    public ServiceResult<RecipesDTO> getRecipesForUser(String userId) {
        log.debug("{} Get recipes for user : {}", LOG_TITLE,userId);
        ServiceResult<RecipesDTO> serviceResult;
        try {
            recipeRepository.startConnection();
            List<Recipe> userRecipes = recipeRepository.findByUser(userId);
            List<RecipeDTO> recipeDTOList = userRecipes.stream().map((this::buildRecipeDTO)).toList();
            RecipesDTO recipesDTO = new RecipesDTO(recipeDTOList);
            log.debug("{} Recipes found {}", LOG_TITLE, recipesDTO);
            serviceResult = new ServiceResult<>(true, recipesDTO, null, 0);
            recipeRepository.closeConnection();
            return serviceResult;
        } catch (SQLException e) {
            log.error(String.valueOf(e));
            recipeRepository.rollbackConnection();
            return new ServiceResult<>(false,null,"Internal Error",2);
        }
    }

    private RecipeDTO buildRecipeDTO(Recipe recipe) {
        RecipeDTO recipeDTO = new RecipeDTO();
        recipeDTO.setId(recipe.getId());
        recipeDTO.setTitle(recipe.getTitle());
        recipeDTO.setDescription(recipe.getDescription());
        recipeDTO.setImage(recipe.getImage());
        recipeDTO.setInstructions(recipe.getInstructions());
        recipeDTO.setIngredients(recipe.getIngredients());
        recipeDTO.setInstructions(recipe.getInstructions());
        return recipeDTO;
    }
}
