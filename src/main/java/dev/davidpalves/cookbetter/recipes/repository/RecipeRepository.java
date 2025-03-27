package dev.davidpalves.cookbetter.recipes.repository;

import dev.davidpalves.cookbetter.ConnectionProvider;
import dev.davidpalves.cookbetter.recipes.models.Recipe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
                "id BIGSERIAL PRIMARY KEY, " +
                "userId INTEGER NOT NULL, " +
                "title VARCHAR(100) NOT NULL, " +
                "description VARCHAR(100) NOT NULL, " +
                "image VARCHAR(100)," +
                "created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "modified_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE" +
                ")";

        String ingredientTableSql = "CREATE TABLE IF NOT EXISTS recipes_ingredients (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "recipeId INTEGER NOT NULL , " +
                "ingredient VARCHAR(100) NOT NULL, " +
                "FOREIGN KEY (recipeId) REFERENCES recipes(id) ON DELETE CASCADE" +
                ")";

        String tagsTableSql = "CREATE TABLE IF NOT EXISTS recipes_tags (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "recipeId INTEGER NOT NULL , " +
                "tag VARCHAR(100) NOT NULL, " +
                "FOREIGN KEY (recipeId) REFERENCES recipes(id) ON DELETE CASCADE" +
                ")";

        String instructionsTableSql = "CREATE TABLE IF NOT EXISTS recipes_instructions (" +
                "id BIGSERIAL PRIMARY KEY, " +
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
        String recipeSql = """
                SELECT
                    r.id,
                    r.title,
                    r.description,
                    r.userid,
                    r.image,
                    r.created_at,
                    r.modified_at,
                    (SELECT array_agg(ingredient) FROM recipes_ingredients WHERE recipeid = r.id) AS ingredients,
                    (SELECT array_agg(tag) FROM recipes_tags WHERE recipeid = r.id) AS tags,
                    (SELECT array_agg(instruction) FROM recipes_instructions WHERE recipeid = r.id) AS instructions
                FROM recipes r
                WHERE r.id = ?;""";
        Recipe recipe = new Recipe();
        try (PreparedStatement stmt = connection.prepareStatement(recipeSql)) {
            stmt.setInt(1, Integer.parseInt(recipeId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String userId = rs.getString("userId");
                    String title = rs.getString("title");
                    String description = rs.getString("description");
                    String image = rs.getString("image");
                    String[] ingredientsArray = (String[]) rs.getArray("ingredients").getArray();
                    List<String> ingredients = ingredientsArray != null ? Arrays.asList(ingredientsArray) : new ArrayList<>();
                    String[] instructionsArray = (String[]) rs.getArray("instructions").getArray();
                    List<String> instructions = instructionsArray != null ? Arrays.asList(instructionsArray) : new ArrayList<>();
                    Array tagsArray = rs.getArray("tags");
                    List<String> tags = (tagsArray != null) ? Arrays.asList((String[]) tagsArray.getArray()) : new ArrayList<>();
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    LocalDateTime modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime();

                    recipe.setId(recipeId);
                    recipe.setTitle(title);
                    recipe.setUserId(userId);
                    recipe.setDescription(description);
                    recipe.setImage(image);
                    recipe.setIngredients(ingredients);
                    recipe.setInstructions(instructions);
                    recipe.setTags(tags);
                    recipe.setCreatedAt(createdAt);
                    recipe.setModifiedAt(modifiedAt);
                    return Optional.of(recipe);
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    public List<Recipe> findAllByUser(String userId) throws SQLException {
        Connection connection = connectionProvider.getConnection();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Connection is not open");
        }
        String recipeSql = "SELECT * FROM recipes WHERE userid = ?";
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
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    LocalDateTime modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime();
                    recipe.setId(recipeId);
                    recipe.setTitle(title);
                    recipe.setUserId(userId);
                    recipe.setDescription(description);
                    recipe.setImage(image);
                    recipe.setCreatedAt(createdAt);
                    recipe.setModifiedAt(modifiedAt);
                    recipeList.add(recipe);
                }
            }
        }
        return recipeList;
    }

    public Recipe delete(String recipeId) throws SQLException {
        Connection connection = connectionProvider.getConnection();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Connection is not open");
        }
        String sql = "DELETE FROM recipes WHERE id = ? RETURNING id, title, userid, description, image;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(recipeId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Recipe recipe = new Recipe();
                    String title = rs.getString("title");
                    String userId = rs.getString("userId");
                    String description = rs.getString("description");
                    String image = rs.getString("image");
                    recipe.setId(recipeId);
                    recipe.setTitle(title);
                    recipe.setUserId(userId);
                    recipe.setDescription(description);
                    recipe.setImage(image);
                    return recipe;
                }
            }
        }
        return null;
    }
}
