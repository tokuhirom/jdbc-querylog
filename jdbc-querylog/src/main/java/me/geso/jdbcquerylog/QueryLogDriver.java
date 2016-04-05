package me.geso.jdbcquerylog;

import de.vandermeer.asciitable.v2.RenderedTable;
import de.vandermeer.asciitable.v2.V2_AsciiTable;
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer;
import de.vandermeer.asciitable.v2.render.WidthLongestLine;
import de.vandermeer.asciitable.v2.themes.V2_E_TableThemes;
import me.geso.jdbctracer.TracerConnection;

import java.sql.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class QueryLogDriver implements Driver {
    private static boolean enabled = true;
    private static boolean explain = false;
    private static boolean compact = true;
    private static BiConsumer<Connection, String> printQuery = (connection, query) -> {
        System.err.println(query);
    };
    private static PrintExplainCallback printExplain = (connection, query, header, rows) -> {
        V2_AsciiTable at = new V2_AsciiTable();
        at.addRule();
        at.addRow(header);
        at.addRule();
        rows.forEach(at::addRow);
        at.addRule();

        V2_AsciiTableRenderer rend = new V2_AsciiTableRenderer();
        rend.setTheme(V2_E_TableThemes.UTF_LIGHT.get());
        rend.setWidth(new WidthLongestLine());
        RenderedTable render = rend.render(at);
        System.err.println(render.toString());
    };

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

    public static void setPrintQuery(BiConsumer<Connection, String> printQuery) {
        QueryLogDriver.printQuery = printQuery;
    }

    public static BiConsumer<Connection, String> getPrintQuery() {
        return QueryLogDriver.printQuery;
    }

    public static PrintExplainCallback getPrintExplain() {
        return printExplain;
    }

    public static void setPrintExplain(PrintExplainCallback printExplain) {
        QueryLogDriver.printExplain = printExplain;
    }

    @FunctionalInterface
    public interface PrintExplainCallback {
        void accept(Connection connection, String query, Object[] header, List<Object[]> rows);
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
