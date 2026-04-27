package ru.qa.blogapi.base;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import ru.qa.blogapi.config.TestConfig;

public abstract class BaseUiTest {

    protected WebDriver driver;
    protected String uiBaseUrl;
    protected String apiBaseUrl;

    @BeforeEach
    void setUpUi() {
        uiBaseUrl = TestConfig.uiBaseUrl();
        apiBaseUrl = TestConfig.apiBaseUrl();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        if (Boolean.parseBoolean(System.getProperty("headless", "true"))) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }

        driver = new ChromeDriver(options);
    }

    @AfterEach
    void tearDownUi() {
        if (driver != null) {
            driver.quit();
        }
    }
}
