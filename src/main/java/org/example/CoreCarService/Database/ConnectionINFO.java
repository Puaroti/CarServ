package org.example.CoreCarService.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionINFO {
    private static final String DB_URL = "jdbc:h2:file:./database/CarService";
    private static final String USER = "sa";
    private static final String PASS = "";
    private static final Logger logger = LoggerFactory.getLogger(ConnectionINFO.class);

    public String getDB_URL() {
        return DB_URL;
    }
    public String getUSER() {
        return USER;
    }
    public String getPASS() {
        return PASS;
    }
    public Connection getConnectionH2() throws SQLException {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("Driver not found", e);
        }
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }




}

