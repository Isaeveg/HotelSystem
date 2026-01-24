package com.hotel.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration class for database connection settings.
 * <p>
 * Loads database configuration from a `database.properties` file or uses
 * default values if the file is not found.
 * </p>
 */
public class DatabaseConfig {
    private static final Logger logger = LogManager.getLogger(DatabaseConfig.class);
    private static final String CONFIG_FILE = "database.properties";

    private static Properties properties;

    static {
        loadProperties();
    }

    /**
     * Loads the properties from the configuration file.
     * <p>
     * If looking up the file fails, default properties are set.
     * </p>
     */
    private static void loadProperties() {
        properties = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {

            if (input == null) {
                logger.warn("Configuration file {} not found, using default values", CONFIG_FILE);
                setDefaultProperties();
                return;
            }

            properties.load(input);
            logger.info("Database configuration loaded successfully");

        } catch (IOException e) {
            logger.error("Error loading configuration: {}", e.getMessage());
            setDefaultProperties();
        }
    }

    /**
     * Sets default database properties.
     */
    private static void setDefaultProperties() {
        properties.setProperty("db.url", "jdbc:postgresql://localhost:5432/hotel_db");
        properties.setProperty("db.username", "postgres");
        properties.setProperty("db.password", "");
    }

    /**
     * Gets the database URL.
     *
     * @return the database URL
     */
    public static String getUrl() {
        return properties.getProperty("db.url");
    }

    /**
     * Gets the database username.
     *
     * @return the database username
     */
    public static String getUsername() {
        return properties.getProperty("db.username");
    }

    /**
     * Gets the database password.
     *
     * @return the database password
     */
    public static String getPassword() {
        return properties.getProperty("db.password");
    }
}
