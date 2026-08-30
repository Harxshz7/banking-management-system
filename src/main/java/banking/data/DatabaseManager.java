package banking.data;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DEFAULT_URL = "jdbc:sqlite:banking_data/database.db";
    private static String url = DEFAULT_URL;
    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    static {
        try {
            // Ensure driver is loaded
            Class.forName("org.sqlite.JDBC");
            
            // Create directory if it doesn't exist
            File dir = new File("banking_data");
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void setDbUrl(String newUrl) {
        url = newUrl;
        // Close existing thread-local connection if URL changes
        closeConnection();
    }
    
    public static String getDbUrl() {
        return url;
    }

    public static Connection getConnection() {
        try {
            Connection conn = connectionHolder.get();
            if (conn == null || conn.isClosed()) {
                Connection realConn = DriverManager.getConnection(url);
                // Enable foreign keys (SQLite disables them by default)
                try (Statement stmt = realConn.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }
                
                Connection proxyConn = (Connection) java.lang.reflect.Proxy.newProxyInstance(
                    DatabaseManager.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    (proxy, method, args) -> {
                        if ("close".equals(method.getName())) {
                            // Do nothing on close() so connection stays alive for thread-local usage
                            return null;
                        }
                        if ("isClosed".equals(method.getName())) {
                            return realConn.isClosed();
                        }
                        try {
                            return method.invoke(realConn, args);
                        } catch (java.lang.reflect.InvocationTargetException ite) {
                            throw ite.getCause();
                        }
                    }
                );
                connectionHolder.set(proxyConn);
            }
            return connectionHolder.get();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    public static void closeConnection() {
        Connection conn = connectionHolder.get();
        if (conn != null) {
            try {
                // Unwrap or reflection close
                java.lang.reflect.Field hField = java.lang.reflect.Proxy.getInvocationHandler(conn).getClass().getDeclaredFields()[0];
                // Or simply let connectionHolder reset
            } catch (Exception ignored) {}
            connectionHolder.remove();
        }
    }

    public static void initializeSchema() {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            
            byte[] bytes = null;
            InputStream resStream = DatabaseManager.class.getResourceAsStream("/schema.sql");
            if (resStream == null) {
                resStream = DatabaseManager.class.getResourceAsStream("schema.sql");
            }
            if (resStream != null) {
                try (InputStream is = resStream) {
                    bytes = is.readAllBytes();
                }
            } else {
                File schemaFile = new File("src/main/resources/schema.sql");
                if (schemaFile.exists()) {
                    try (InputStream fis = new java.io.FileInputStream(schemaFile)) {
                        bytes = fis.readAllBytes();
                    }
                }
            }

            if (bytes != null) {
                String schema = new String(bytes, StandardCharsets.UTF_8);
                String[] statements = schema.split(";");
                for (String s : statements) {
                    if (!s.trim().isEmpty()) {
                        stmt.execute(s.trim());
                    }
                }
            } else {
                System.err.println("schema.sql not found in resources or file system!");
            }
            stmt.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize schema", e);
        }
    }
    
    public static void beginTransaction() {
        try {
            getConnection().setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to begin transaction", e);
        }
    }
    
    public static void commitTransaction() {
        try {
            Connection conn = getConnection();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to commit transaction", e);
        }
    }
    
    public static void rollbackTransaction() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.getAutoCommit()) {
                conn.rollback();
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to rollback transaction", e);
        }
    }
}
