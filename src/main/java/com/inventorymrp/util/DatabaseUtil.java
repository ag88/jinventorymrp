package com.inventorymrp.util;

import org.flywaydb.core.Flyway;
import org.sql2o.Sql2o;
import org.sql2o.converters.Converter;
import org.sql2o.quirks.NoQuirks;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Database utility class for managing H2 database connection.
 */
public class DatabaseUtil {
    private static Sql2o sql2o;
    //private static final String DB_URL = "jdbc:h2:./data/inventorydb;AUTO_SERVER=TRUE";
    //private static final String DB_URL = "jdbc:h2:./data/inventorydb;TRACE_LEVEL_SYSTEM_OUT=2";
    private static final String DB_URL = "jdbc:h2:./data/inventorydb";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private DatabaseUtil() {
        // Private constructor to prevent instantiation
    }

    public static Sql2o getSql2o() {
        if (sql2o == null) {
            sql2o = createSql2oWithConverters(DB_URL, DB_USER, DB_PASSWORD);
        }
        return sql2o;
    }

    public static Sql2o getSql2o(String url, String user, String password) {
        sql2o = createSql2oWithConverters(url, user, password);
        return sql2o;
    }
    
    private static Sql2o createSql2oWithConverters(String url, String user, String password) {
        // Register LocalDateTime converter for Java 8 time support
        Map<Class, Converter> converters = new HashMap<>();
        converters.put(LocalDateTime.class, new LocalDateTimeConverter());
        converters.put(LocalDate.class, new LocalDateConverter());
        
        Sql2o instance = new Sql2o(url, user, password, new NoQuirks(converters));
        instance.setDefaultCaseSensitive(false);
        return instance;
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
