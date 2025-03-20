package dev.davidpalves.cookbetter.recipes.repository;

import dev.davidpalves.cookbetter.ConnectionProvider;
import dev.davidpalves.cookbetter.profile.models.Profile;
import dev.davidpalves.cookbetter.recipes.models.Recipe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@DependsOn("UserRepository")
public class RecipeRepository {
    private final ConnectionProvider connectionProvider;

    public RecipeRepository(ConnectionProvider connectionProvider) throws SQLException {
        this.connectionProvider = connectionProvider;
        createRecipeTableIfNotExists();
    }

    private void createRecipeTableIfNotExists() throws SQLException {
        String recipeTableSql = "CREATE TABLE IF NOT EXISTS recipes (" +
                "id SERIAL PRIMARY KEY, " +
                "userId INTEGER NOT NULL, " +
                "title VARCHAR(100) NOT NULL, " +
                "description VARCHAR(100) NOT NULL, " +
                "image VARCHAR(100)," +
                "FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE" +
                ")";

        String ingredientTableSql = "CREATE TABLE IF NOT EXISTS recipes_ingredients (" +
                "id SERIAL PRIMARY KEY, " +
                "recipeId INTEGER NOT NULL , " +
                "ingredient VARCHAR(100) NOT NULL, " +
                "FOREIGN KEY (recipeId) REFERENCES recipes(id) ON DELETE CASCADE" +
                ")";

        String tagsTableSql = "CREATE TABLE IF NOT EXISTS recipes_tags (" +
                "id SERIAL PRIMARY KEY, " +
                "recipeId INTEGER NOT NULL , " +
                "tag VARCHAR(100) NOT NULL, " +
                "FOREIGN KEY (recipeId) REFERENCES recipes(id) ON DELETE CASCADE" +
                ")";

        String instructionsTableSql = "CREATE TABLE IF NOT EXISTS recipes_instructions (" +
                "id SERIAL PRIMARY KEY, " +
                "recipeId INTEGER NOT NULL , " +
                "instruction VARCHAR(100) NOT NULL, " +
                "FOREIGN KEY (recipeId) REFERENCES recipes(id) ON DELETE CASCADE" +
                ")";

        try (Connection connection = connectionProvider.getConnection(); Statement stmt = connection.createStatement()) {
            log.info("Creating recipe table if it does not exist");
            stmt.execute(recipeTableSql);
            stmt.execute(ingredientTableSql);
            stmt.execute(tagsTableSql);
            stmt.execute(instructionsTableSql);
            connection.commit();
        }
    }

    public void startConnection() throws SQLException {
        connectionProvider.startConnection();
    }

    public void closeConnection() throws SQLException {
        connectionProvider.closeConnection();
    }

    public void rollbackConnection() {
        connectionProvider.rollbackConnection();
    }

    public String save(Recipe recipe) throws SQLException {
        Connection connection = connectionProvider.getConnection();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Connection is not open");
        }
        String sql = """
                INSERT INTO recipes (title, userId, description, image) VALUES (?, ?, ?, ?)
                RETURNING id;
""";
        String recipeId;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, recipe.getTitle());
            stmt.setInt(2, Integer.parseInt(recipe.getUserId()));
            stmt.setString(3, recipe.getDescription());
            stmt.setString(4, recipe.getImage());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    recipeId = rs.getString("id");
                    for (String ingredient : recipe.getIngredients()) {
                        String ingredientSQL = "INSERT INTO recipes_ingredients (recipeid, ingredient) VALUES (?, ?)";
                        try (PreparedStatement ingredientStmt = connection.prepareStatement(ingredientSQL)) {
                            ingredientStmt.setInt(1, Integer.parseInt(recipeId));
                            ingredientStmt.setString(2, ingredient);
                            if (ingredientStmt.executeUpdate() == 0) {
                                return null;
                            }
                        }
                    }
                    for (String instruction : recipe.getInstructions()) {
                        String instructionSQL = "INSERT INTO recipes_instructions (recipeid, instruction) VALUES (?, ?)";
                        try (PreparedStatement instructionStmt = connection.prepareStatement(instructionSQL)) {
                            instructionStmt.setInt(1, Integer.parseInt(recipeId));
                            instructionStmt.setString(2, instruction);
                            if (instructionStmt.executeUpdate() == 0) {
                                return null;
                            }
                        }
                    }
                    if (recipe.getTags() != null) {
                        for (String tag : recipe.getTags()) {
                            String tagSQL = "INSERT INTO recipes_tags (recipeid, tag) VALUES (?, ?)";
                            try (PreparedStatement tagStmt = connection.prepareStatement(tagSQL)) {
                                tagStmt.setInt(1, Integer.parseInt(recipeId));
                                tagStmt.setString(2, tag);
                                if (tagStmt.executeUpdate() == 0) {
                                    return null;
                                }
                            }
                        }
                    }
                    return recipeId;
                }
                return null;
            }
        }
    }

    public Optional<Recipe> findById(String recipeId) throws SQLException {
        Connection connection = connectionProvider.getConnection();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Connection is not open");
        }
        String recipeSql = "SELECT * FROM recipes WHERE id = ? LIMIT 1";
        String ingredientSql = "SELECT ingredient FROM recipes_ingredients WHERE recipeid = ?";
        String instructionSql = "SELECT instruction FROM recipes_instructions WHERE recipeid = ?";
        String tagSql = "SELECT tag FROM recipes_tags WHERE recipeid = ?";
        Recipe recipe = new Recipe();
        try (PreparedStatement stmt = connection.prepareStatement(recipeSql)) {
            stmt.setString(1, recipeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String userId = rs.getString("userId");
                    String title = rs.getString("title");
                    String description = rs.getString("description");
                    String image = rs.getString("image");
                    recipe.setId(recipeId);
                    recipe.setTitle(title);
                    recipe.setUserId(userId);
                    recipe.setDescription(description);
                    recipe.setImage(image);
                    try (PreparedStatement ingredientStmt = connection.prepareStatement(ingredientSql)) {
                        ingredientStmt.setString(1, recipeId);
                        try (ResultSet ingredientResults = ingredientStmt.executeQuery()) {
                            List<String> ingredients = new ArrayList<>();
                            while (ingredientResults.next()) {
                                String ingredient = ingredientResults.getString("ingredient");
                                ingredients.add(ingredient);
                            }
                            recipe.setIngredients(ingredients);
                        }
                    }

                    try (PreparedStatement instructionStmt = connection.prepareStatement(instructionSql)) {
                        instructionStmt.setString(1, recipeId);
                        try (ResultSet instructionResults = instructionStmt.executeQuery()) {
                            List<String> instructions = new ArrayList<>();
                            while (instructionResults.next()) {
                                String instruction = instructionResults.getString("instruction");
                                instructions.add(instruction);
                            }
                            recipe.setIngredients(instructions);
                        }
                    }

                    try (PreparedStatement tagStmt = connection.prepareStatement(tagSql)) {
                        tagStmt.setString(1, recipeId);
                        try (ResultSet tagResults = tagStmt.executeQuery()) {
                            List<String> tags = new ArrayList<>();
                            while (tagResults.next()) {
                                String tag = tagResults.getString("tag");
                                tags.add(tag);
                            }
                            recipe.setIngredients(tags);
                        }
                    }
                    return Optional.of(recipe);
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    public List<Recipe> findByUser(String userId) throws SQLException {
        Connection connection = connectionProvider.getConnection();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Connection is not open");
        }
        String recipeSql = "SELECT * FROM recipes WHERE userid = ?";
        String ingredientSql = "SELECT ingredient FROM recipes_ingredients WHERE recipeid = ?";
        String instructionSql = "SELECT instruction FROM recipes_instructions WHERE recipeid = ?";
        String tagSql = "SELECT tag FROM recipes_tags WHERE recipeid = ?";
        List<Recipe> recipeList = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(recipeSql)) {
            stmt.setInt(1, Integer.parseInt(userId));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Recipe recipe = new Recipe();
                    String recipeId = rs.getString("id");
                    String title = rs.getString("title");
                    String description = rs.getString("description");
                    String image = rs.getString("image");
                    recipe.setId(recipeId);
                    recipe.setTitle(title);
                    recipe.setUserId(userId);
                    recipe.setDescription(description);
                    recipe.setImage(image);
                    try (PreparedStatement ingredientStmt = connection.prepareStatement(ingredientSql)) {
                        ingredientStmt.setInt(1, Integer.parseInt(recipeId));
                        try (ResultSet ingredientResults = ingredientStmt.executeQuery()) {
                            List<String> ingredients = new ArrayList<>();
                            while (ingredientResults.next()) {
                                String ingredient = ingredientResults.getString("ingredient");
                                ingredients.add(ingredient);
                            }
                            recipe.setIngredients(ingredients);
                        }
                    }

                    try (PreparedStatement instructionStmt = connection.prepareStatement(instructionSql)) {
                        instructionStmt.setInt(1, Integer.parseInt(recipeId));
                        try (ResultSet instructionResults = instructionStmt.executeQuery()) {
                            List<String> instructions = new ArrayList<>();
                            while (instructionResults.next()) {
                                String instruction = instructionResults.getString("instruction");
                                instructions.add(instruction);
                            }
                            recipe.setIngredients(instructions);
                        }
                    }

                    try (PreparedStatement tagStmt = connection.prepareStatement(tagSql)) {
                        tagStmt.setInt(1, Integer.parseInt(recipeId));
                        try (ResultSet tagResults = tagStmt.executeQuery()) {
                            List<String> tags = new ArrayList<>();
                            while (tagResults.next()) {
                                String tag = tagResults.getString("tag");
                                tags.add(tag);
                            }
                            recipe.setIngredients(tags);
                        }
                    }
                    recipeList.add(recipe);
                }
            }
        }
        return recipeList;
    }
}
