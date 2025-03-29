package dev.davidpalves.cookbetter.recipes.service;

import dev.davidpalves.cookbetter.models.ServiceResult;
import dev.davidpalves.cookbetter.profile.service.ProfileService;
import dev.davidpalves.cookbetter.recipes.dto.RecipeDTO;
import dev.davidpalves.cookbetter.recipes.dto.RecipesDTO;
import dev.davidpalves.cookbetter.recipes.models.Recipe;
import dev.davidpalves.cookbetter.recipes.repository.RecipeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class RecipeService {
    private static final String LOG_TITLE = "[RecipeService] -";
    //TODO: THIS SHOULD BE ANOTHER FOLDER. Fix FilePath using "src/main/resources/static/"
    private static final String RECIPE_PHOTO_DIR = "recipes/assets/";

    private final RecipeRepository recipeRepository;
    private final ProfileService profileService;


    public RecipeService(RecipeRepository recipeRepository, ProfileService profileService) {
        this.recipeRepository = recipeRepository;
        this.profileService = profileService;
        File file = new File("src/main/resources/static/" + RECIPE_PHOTO_DIR);
        boolean created = file.mkdirs();
    }

    public ServiceResult<RecipesDTO> getRecipes() {
        return new ServiceResult<>();
    }

    public ServiceResult<String> createRecipe(RecipeDTO recipeDTO, MultipartFile image, String userId) {
        log.debug("{} Create recipe : {}", LOG_TITLE,recipeDTO);
        ServiceResult<String> serviceResult;
        try {
            recipeRepository.startConnection();
            Recipe recipe = new Recipe();
            recipe.setUserId(userId);
            recipe.setTitle(recipeDTO.getTitle());
            recipe.setDescription(recipeDTO.getDescription());
            recipe.setImage(recipeDTO.getImageUrl());
            recipe.setInstructions(recipeDTO.getInstructions());
            recipe.setIngredients(recipeDTO.getIngredients());
            recipe.setTags(recipeDTO.getTags());
            String imageUrl = getImageUrl(image);
            recipe.setImage(imageUrl);
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
        } catch (SQLException | IOException e) {
            log.error(String.valueOf(e));
            recipeRepository.rollbackConnection();
            return new ServiceResult<>(false,null,"Internal Error",1);
        }
    }

    public ServiceResult<String> updateRecipe(RecipeDTO recipeDTO, MultipartFile image, String recipeId) {
        log.debug("{} Create recipe : {}", LOG_TITLE,recipeDTO);
        ServiceResult<String> serviceResult;
        try {
            recipeRepository.startConnection();
            Optional<Recipe> optionalRecipe = recipeRepository.findById(recipeId);
            if (optionalRecipe.isPresent()) {
                Recipe recipe = optionalRecipe.get();
                recipe.setTitle(recipeDTO.getTitle());
                recipe.setDescription(recipeDTO.getDescription());
                recipe.setIngredients(recipeDTO.getIngredients());
                recipe.setInstructions(recipeDTO.getInstructions());
                recipe.setTags(recipeDTO.getTags());
                String imageUrl = getImageUrl(image);
                if (imageUrl != null) recipe.setImage(imageUrl);

                if (recipeRepository.update(recipe)) {
                    log.debug("{} Recipe updated {}", LOG_TITLE, recipeId);
                    serviceResult = new ServiceResult<>(true, "Recipe Updated", null, 0);
                    recipeRepository.closeConnection();
                } else {
                    log.debug("{} Error updating recipe", LOG_TITLE);
                    serviceResult = new ServiceResult<>(false,null,"Error updating recipe",2);
                    recipeRepository.rollbackConnection();
                }
            } else {
                log.debug("{} Error founding recipe", LOG_TITLE);
                serviceResult = new ServiceResult<>(false,null,"Error founding recipe",1);
                recipeRepository.rollbackConnection();
            }
            return serviceResult;
        } catch (SQLException | IOException e) {
            log.error(String.valueOf(e));
            recipeRepository.rollbackConnection();
            return new ServiceResult<>(false,null,"Internal Error",2);
        }
    }

    private String getImageUrl(MultipartFile image) throws IOException {
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path filePath = Paths.get("src/main/resources/static/" + RECIPE_PHOTO_DIR + fileName);
            Files.write(filePath, image.getBytes());
            imageUrl = "/" + RECIPE_PHOTO_DIR + fileName;
        }
        return imageUrl;
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
            List<Recipe> userRecipes = recipeRepository.findAllByUser(userId);
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

    public ServiceResult<String> deleteRecipe(String recipeId) {
        log.debug("{} Delete recipe {}", LOG_TITLE,recipeId);
        ServiceResult<String> serviceResult;
        try {
            recipeRepository.startConnection();
            Recipe deleted = recipeRepository.delete(recipeId);
            if (deleted != null) {
                ServiceResult<Integer> deleteRecipeFromProfile = profileService.deleteRecipeFromProfile(deleted.getUserId());
                if (deleteRecipeFromProfile.isSuccess()) {
                    log.debug("{} Recipe deleted {}", LOG_TITLE, recipeId);
                    serviceResult = new ServiceResult<>(true, "", null, 0);
                    recipeRepository.closeConnection();
                } else{
                    log.debug("{} Error updating recipe count ", LOG_TITLE);
                    serviceResult = new ServiceResult<>(false, null, "Error creating recipe", 2);
                    recipeRepository.rollbackConnection();
                }
            }
            else {
                log.debug("{} Error deleting recipe", LOG_TITLE);
                serviceResult = new ServiceResult<>(false,null,"Error deleting recipe",1);
                recipeRepository.rollbackConnection();
            }
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
        recipeDTO.setUserId(recipe.getUserId());
        recipeDTO.setTitle(recipe.getTitle());
        recipeDTO.setDescription(recipe.getDescription());
        recipeDTO.setImageUrl(recipe.getImage());
        recipeDTO.setInstructions(recipe.getInstructions());
        recipeDTO.setIngredients(recipe.getIngredients());
        recipeDTO.setTags(recipe.getTags());
        return recipeDTO;
    }

}
