package dev.davidpalves.cookbetter.recipes.repository;

import dev.davidpalves.cookbetter.ConnectionProvider;
import dev.davidpalves.cookbetter.recipes.models.Comment;
import dev.davidpalves.cookbetter.recipes.models.Recipe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.sql.*;

@Component
@Slf4j
@DependsOn("RecipeRepository")
public class CommentRepository {

    private final ConnectionProvider connectionProvider;

    public CommentRepository(ConnectionProvider connectionProvider) throws SQLException {
        this.connectionProvider = connectionProvider;
        createCommentTableIfNotExists();
    }

    private void createCommentTableIfNotExists() throws SQLException {
        String commentsTableSql = "CREATE TABLE IF NOT EXISTS recipes_comments (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "userId INTEGER NOT NULL , " +
                "recipeId INTEGER NOT NULL , " +
                "parent INTEGER, " +
                "content VARCHAR(250) NOT NULL, " +
                "FOREIGN KEY (recipeId) REFERENCES recipes(id) ON DELETE CASCADE," +
                "FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE" +
                ")";

        try (Connection connection = connectionProvider.getConnection(); Statement stmt = connection.createStatement()) {
            log.info("Creating comment table if it does not exist");
            stmt.execute(commentsTableSql);
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

    public String save(Comment comment) throws SQLException {
        Connection connection = connectionProvider.getConnection();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Connection is not open");
        }
        String sql = """
                INSERT INTO recipes_comments (userId, recipeId, parent, content) VALUES (?, ?, ?, ?)
                RETURNING id;
""";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(comment.getUserId()));
            stmt.setInt(2, Integer.parseInt(comment.getUserId()));
            stmt.setInt(3, Integer.parseInt(comment.getParentComment()));
            stmt.setString(4, comment.getContent());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("id");
                }
                return null;
            }
        }
    }
}
