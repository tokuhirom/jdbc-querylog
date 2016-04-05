package me.geso.jdbcquerylog;

import me.geso.jdbctracer.TracerConnection;

import java.sql.*;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class QueryLogDriver implements Driver {
    private static boolean enabled = true;
    private static boolean explain = false;
    private static boolean compact = true;

    static {
        try {
            DriverManager.registerDriver(new QueryLogDriver());
        } catch (SQLException e) {
            throw new RuntimeException("Can't register jdbc-querylog driver!: " + e.getMessage());
        }
    }

    public static void setEnabled(boolean enabled) {
        QueryLogDriver.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setExplain(boolean explain) {
        QueryLogDriver.explain = explain;
    }

    public static boolean isExplain() {
        return explain;
    }

    public static boolean isCompact() {
        return compact;
    }

    public static void setCompact(boolean compact) {
        QueryLogDriver.compact = compact;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        String underlingUri = parseURL(url);
        if (underlingUri != null) {
            Driver underlyingDriver = getUnderlyingDriver(underlingUri);
            if (underlyingDriver == null) {
                return null;
            }

            Connection connection = Objects.requireNonNull(underlyingDriver.connect(underlingUri, info));
            return TracerConnection.newInstance(connection, new PreparedStatementLogger(), null);
        } else {
            return null;
        }
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        String underlingUri = parseURL(url);
        if (underlingUri != null) {
            return getUnderlyingDriver(underlingUri) != null;
        } else {
            return false;
        }
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        Driver underlyingDriver = getUnderlyingDriver(url);
        if (underlyingDriver == null) {
            return new DriverPropertyInfo[0];
        }
        return underlyingDriver.getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    private String parseURL(String url) {
        if (url.startsWith("jdbc:querylog:")) {
            return "jdbc:" + url.substring("jdbc:querylog:".length());
        } else {
            return null;
        }
    }

    private Driver getUnderlyingDriver(String underlingUri) throws SQLException {
        Enumeration<Driver> e = DriverManager.getDrivers();

        Driver d;
        while (e.hasMoreElements()) {
            d = e.nextElement();

            if (d.acceptsURL(underlingUri)) {
                return d;
            }
        }
        return null;
    }
}
