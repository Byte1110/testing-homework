package ru.qa.blogapi.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TestConfig {

    private static final Properties PROPERTIES = loadProperties();

    private TestConfig() {
    }

    public static String baseUrl() {
        return System.getProperty("base.url",
                PROPERTIES.getProperty("base.url", "http://localhost:3000"));
    }

    public static String apiBaseUrl() {
        return System.getProperty("api.base.url",
                System.getProperty("base.url",
                        PROPERTIES.getProperty("api.base.url", baseUrl())));
    }

    public static String uiBaseUrl() {
        return System.getProperty("ui.base.url",
                System.getProperty("base.url",
                        PROPERTIES.getProperty("ui.base.url", baseUrl())));
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = TestConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить application.properties", e);
        }
        return properties;
    }
}
