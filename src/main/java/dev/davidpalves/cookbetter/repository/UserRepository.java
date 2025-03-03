package dev.davidpalves.cookbetter.repository;

import dev.davidpalves.cookbetter.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Component
@Slf4j
public class UserRepository {

    private final DataSource dataSource;
    private final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

    public UserRepository(DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
        createUserTableIfNotExists();
    }

    private void createUserTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100) UNIQUE NOT NULL," +
                "username VARCHAR(100) UNIQUE NOT NULL," +
                "password VARCHAR(100) NOT NULL" +
                ")";

        try (Connection connection = dataSource.getConnection(); Statement stmt = connection.createStatement()) {
            log.info("Creating user table if it does not exist");
            stmt.execute(sql);
        }
    }

    public void startConnection() throws SQLException {
        if (connectionThreadLocal.get() == null || connectionThreadLocal.get().isClosed()) {
            connectionThreadLocal.set(dataSource.getConnection());
        }
    }

    public boolean existsByUsername(String username) throws SQLException {
        Connection connection = connectionThreadLocal.get();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Connection is not open");
        }
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? LIMIT 1";
        return executeExistsQuery(username, connection, sql);

    }

    public boolean existsByEmail(String email) throws SQLException {
        Connection connection = connectionThreadLocal.get();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Connection is not open");
        }
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? LIMIT 1";
        return executeExistsQuery(email, connection, sql);
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

    public Optional<User> findByEmail(String email) throws SQLException {
        Connection connection = connectionThreadLocal.get();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Connection is not open");
        }
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setEmail(rs.getString("email"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setName(rs.getString("name"));
                    return Optional.of(user);
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    public int save(User user) throws SQLException {
        Connection connection = connectionThreadLocal.get();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Connection is not open");
        }
        String sql = "INSERT INTO users (email,username,password,name) VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getName());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
                return -1;
            }
        }
    }

    public void deleteAllUsers() throws SQLException {
        String sql = "TRUNCATE TABLE users RESTART IDENTITY";
        try (Connection connection = dataSource.getConnection();Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void closeConnection() throws SQLException {
        Connection connection = connectionThreadLocal.get();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    };

}
