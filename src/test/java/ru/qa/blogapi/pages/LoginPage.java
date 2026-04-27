package ru.qa.blogapi.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void open(String uiBaseUrl) {
        driver.get(uiBaseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='email']")
        ));
    }

    public void fillEmail(String email) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='email']")
        ));
        input.clear();
        input.sendKeys(email);
    }

    public void fillPassword(String password) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='password']")
        ));
        input.clear();
        input.sendKeys(password);
    }

    public void clickLogin() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@type='submit' or contains(normalize-space(.),'Войти')]")
        ));
        button.click();
    }
}
