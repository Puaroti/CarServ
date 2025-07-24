package org.example.CoreCarService.Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionServerInfo {
    private static final String DB_URL = "jdbc:postgresql://192.168.31.142:5432/mydb";
    private static final String USER = "myuser";
    private static final String PASS = "mypassword";
    private static final Logger logger = LoggerFactory.getLogger(ConnectionServerInfo.class);

    public String getDB_URL() {
        return DB_URL;
    }
    public String getUSER() {
        return USER;
    }
    public String getPASS() {
        return PASS;
    }
    public Connection getConnectionPostgresql() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("Driver not found", e);
        }
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}
