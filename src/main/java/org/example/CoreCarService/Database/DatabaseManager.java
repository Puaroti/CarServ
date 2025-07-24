package org.example.CoreCarService.Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:h2:file:./database/calculatorDB";
    private static final String USER = "sa";
    private static final String PASS = "";
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    public  Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
    public boolean isLoginExists(String login) {
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT COUNT(*) FROM REG_INFO WHERE login = ?")) {
            stmt.setString(1, login);  // Подставляем логин в запрос
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;  // Если count > 0, значит, логин существует
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка", e);
        }
        return false;
    }
}
