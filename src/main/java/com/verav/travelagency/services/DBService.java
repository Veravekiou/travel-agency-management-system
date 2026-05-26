package com.verav.travelagency.services;

import java.sql.*;

public class DBService {

    private static final String URL = getConfig("DB_URL", "jdbc:mariadb://localhost:3306/agency_db");
    private static final String USER = getConfig("DB_USER", "root");
    private static final String PASSWORD = getConfig("DB_PASSWORD", "");

    private static Connection connection;

    private DBService() {}

    private static String getConfig(String key, String defaultValue) {
        String systemProperty = System.getProperty(key);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty;
        }

        String environmentVariable = System.getenv(key);
        if (environmentVariable != null && !environmentVariable.isBlank()) {
            return environmentVariable;
        }

        return defaultValue;
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    public static ResultSet executeQuery(String sql) throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(sql);
    }




}


