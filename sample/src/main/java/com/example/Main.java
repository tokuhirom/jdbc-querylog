package com.example;

import me.geso.jdbcquerylog.QueryLogDriver;

import java.sql.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        // create fixture data.
        try (Connection fixtureConn = DriverManager.getConnection("jdbc:h2:mem:test")) {
            try (Statement stmt = fixtureConn.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS user (id int PRIMARY KEY, name varchar(255))");
                stmt.executeUpdate("INSERT INTO user (id, name) values (1, 'John'), (2, 'nick')");
            }
            doMain();
        }

    }

    private static void doMain() throws SQLException {
        QueryLogDriver.setExplain(true);
        QueryLogDriver.setQueryHandler((connection, query) -> {
            System.err.println("Query: " + query);
        });
        QueryLogDriver.setExplainHandler((connection, query, header, rows) -> {
            System.err.println("Query: " + query);
            System.err.println("Header: " + Arrays.toString(header));
            System.err.println("Rows: " + rows.stream()
                    .map(it -> Arrays.stream(it).collect(Collectors.joining(",")))
                    .collect(Collectors.joining("\n")));
        });

        try (Connection conn = DriverManager.getConnection("jdbc:querylog:h2:mem:test")) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user WHERE id=?")) {
                stmt.setInt(1, 1);
                try (ResultSet resultSet = stmt.executeQuery()) {
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user")) {
                try (ResultSet resultSet = stmt.executeQuery()) {
                }
            }
        }

    }
}
