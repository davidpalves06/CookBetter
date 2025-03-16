package dev.davidpalves.cookbetter.profile.repository;

import dev.davidpalves.cookbetter.ConnectionProvider;
import dev.davidpalves.cookbetter.profile.models.Profile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.Optional;

@Component
@Slf4j
@DependsOn("UserRepository")
public class ProfileRepository {

    private final ConnectionProvider connectionProvider;

    public ProfileRepository(ConnectionProvider connectionProvider) throws SQLException {
        this.connectionProvider = connectionProvider;
        createProfileTableIfNotExists();
    }

    private void createProfileTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS profiles (" +
                "id SERIAL PRIMARY KEY, " +
                "userId INTEGER NOT NULL , " +
                "name VARCHAR(100) NOT NULL, " +
                "username VARCHAR(100) UNIQUE NOT NULL," +
                "description VARCHAR(100)," +
                "avatarPhoto VARCHAR(100)," +
                "followers INTEGER NOT NULL," +
                "following INTEGER NOT NULL," +
                "recipes INTEGER NOT NULL," +
                "FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE" +
                ")";

        try (Connection connection = connectionProvider.getConnection(); Statement stmt = connection.createStatement()) {
            log.info("Creating profile table if it does not exist");
            stmt.execute(sql);
        }
    }

    public void startConnection() throws SQLException {
        connectionProvider.startConnection();
    }

    public boolean existsByUsername(String username) throws SQLException {
        Connection connection = connectionProvider.getConnection();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Connection is not open");
        }
        String sql = "SELECT COUNT(*) FROM profiles WHERE username = ? LIMIT 1";
        return executeExistsQuery(username, connection, sql);

    }

    private boolean executeExistsQuery(String parameter, Connection connection, String sql) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, parameter);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1) > 0;
                } else {
                    throw new SQLException("Result set is empty. Could not check for existence");
                }
            }
        }
    }

    public Optional<Profile> findByUsername(String username) throws SQLException {
        Connection connection = connectionProvider.getConnection();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Connection is not open");
        }
        String sql = "SELECT * FROM profiles WHERE username = ?";
        return getProfile(username, connection, sql);
    }

    private Optional<Profile> getProfile(String username, Connection connection, String sql) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("id");
                    String userId = rs.getString("userid");
                    String name = rs.getString("name");
                    String profileUsername = rs.getString("username");
                    String description = rs.getString("description");
                    String avatarPhoto = rs.getString("avatarPhoto");
                    int followers = rs.getInt("followers");
                    int following = rs.getInt("following");
                    int recipes = rs.getInt("recipes");
                    Profile profile = new Profile(id,userId,profileUsername,name,description,avatarPhoto,followers,following,recipes);
                    return Optional.of(profile);
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    public String save(Profile profile) throws SQLException {
        Connection connection = connectionProvider.getConnection();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Connection is not open");
        }
        String sql = """
                INSERT INTO profiles (userId, name, username, description, avatarPhoto, followers, following, recipes) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (username)
                DO UPDATE SET
                    name = EXCLUDED.name,
                    description = EXCLUDED.description,
                    avatarPhoto = EXCLUDED.avatarPhoto,
                    followers = EXCLUDED.followers,
                    following = EXCLUDED.following,
                    recipes = EXCLUDED.recipes
                RETURNING id;
""";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(profile.getUserId()));
            stmt.setString(2, profile.getName());
            stmt.setString(3, profile.getUsername());
            stmt.setString(4, profile.getDescription());
            stmt.setString(5, profile.getAvatarPhoto());
            stmt.setInt(6, profile.getFollowers());
            stmt.setInt(7, profile.getFollowing());
            stmt.setInt(8, profile.getRecipes());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("id");
                }
                return null;
            }
        }
    }

    public void closeConnection() throws SQLException {
        connectionProvider.closeConnection();
    }

    public void rollbackConnection() throws SQLException {
        connectionProvider.rollbackConnection();
    }

}
