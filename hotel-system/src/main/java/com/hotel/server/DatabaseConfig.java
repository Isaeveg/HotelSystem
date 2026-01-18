package com.hotel.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final Logger logger = LogManager.getLogger(DatabaseConfig.class);
    private static final String CONFIG_FILE = "database.properties";

    private static Properties properties;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        properties = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {

            if (input == null) {
                logger.warn("Plik konfiguracji {} nie znaleziony, używane wartości domyślne", CONFIG_FILE);
                setDefaultProperties();
                return;
            }

            properties.load(input);
            logger.info("Konfiguracja bazy danych załadowana pomyślnie");

        } catch (IOException e) {
            logger.error("Błąd ładowania konfiguracji: {}", e.getMessage());
            setDefaultProperties();
        }
    }

    private static void setDefaultProperties() {
        properties.setProperty("db.url", "jdbc:postgresql://localhost:5432/hotel_db");
        properties.setProperty("db.username", "postgres");
        properties.setProperty("db.password", "");
    }

    public static String getUrl() {
        return properties.getProperty("db.url");
    }

    public static String getUsername() {
        return properties.getProperty("db.username");
    }

    public static String getPassword() {
        return properties.getProperty("db.password");
    }
}
