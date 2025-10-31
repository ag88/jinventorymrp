package com.inventorymrp.util;

import org.flywaydb.core.Flyway;
import org.sql2o.Sql2o;

/**
 * Database utility class for managing H2 database connection.
 */
public class DatabaseUtil {
    private static Sql2o sql2o;
    //private static final String DB_URL = "jdbc:h2:./data/inventorydb;AUTO_SERVER=TRUE";
    private static final String DB_URL = "jdbc:h2:./data/inventorydb";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private DatabaseUtil() {
        // Private constructor to prevent instantiation
    }

    public static Sql2o getSql2o() {
        if (sql2o == null) {
            sql2o = new Sql2o(DB_URL, DB_USER, DB_PASSWORD);
            sql2o.setDefaultCaseSensitive(false);
        }
        return sql2o;
    }

    public static Sql2o getSql2o(String url, String user, String password) {
        sql2o = new Sql2o(url, user, password);
        sql2o.setDefaultCaseSensitive(false);
        return sql2o;
    }

    public static void initializeDatabase() {
        initializeDatabase(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void initializeDatabase(String url, String user, String password) {
        try {
            // Configure and run Flyway migrations
            Flyway flyway = Flyway.configure()
                .dataSource(url, user, password)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
            
            flyway.migrate();
            
            System.out.println("Database initialized successfully with Flyway");
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}
