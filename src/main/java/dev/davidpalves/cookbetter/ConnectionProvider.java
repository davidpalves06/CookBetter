package dev.davidpalves.cookbetter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@Slf4j
public class ConnectionProvider {

    private final DataSource dataSource;
    private final InheritableThreadLocal<Connection> connectionThreadLocal = new InheritableThreadLocal<>();

    public ConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void startConnection() throws SQLException {
        if (connectionThreadLocal.get() == null || connectionThreadLocal.get().isClosed()) {
            connectionThreadLocal.set(dataSource.getConnection());
            connectionThreadLocal.get().setAutoCommit(false);
        }
    }

    public Connection getConnection() throws SQLException {
        Connection connection = connectionThreadLocal.get();
        if (connection == null || connection.isClosed()) {
            startConnection();
        };
        return connectionThreadLocal.get();
    }

    public void closeConnection() throws SQLException {
        Connection connection = connectionThreadLocal.get();
        if (connection != null && !connection.isClosed()) {
            connectionThreadLocal.get().commit();
            connection.close();
        }
    };

    public void rollbackConnection() {
        Connection connection = connectionThreadLocal.get();
        try {
            if (connection != null && !connection.isClosed()) {
                connectionThreadLocal.get().rollback();
                connection.close();
            }
        } catch (SQLException e) {
            log.error("Could not rollback connection", e);
        }
    };
}
