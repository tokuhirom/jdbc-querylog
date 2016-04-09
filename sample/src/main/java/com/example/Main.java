package com.example;

import de.vandermeer.asciitable.v2.RenderedTable;
import de.vandermeer.asciitable.v2.V2_AsciiTable;
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer;
import de.vandermeer.asciitable.v2.render.WidthLongestLine;
import de.vandermeer.asciitable.v2.themes.V2_E_TableThemes;
import me.geso.jdbcquerylog.QueryLogDriver;

import java.sql.*;

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
            V2_AsciiTable at = new V2_AsciiTable();
            at.addRule();
            at.addRow((Object[]) header);
            at.addRule();
            rows.forEach(at::addRow);
            at.addRule();

            V2_AsciiTableRenderer rend = new V2_AsciiTableRenderer();
            rend.setTheme(V2_E_TableThemes.UTF_LIGHT.get());
            rend.setWidth(new WidthLongestLine());
            RenderedTable render = rend.render(at);
            System.err.println(render.toString());
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
