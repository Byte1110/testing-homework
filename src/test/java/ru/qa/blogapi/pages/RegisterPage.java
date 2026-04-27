package ru.qa.blogapi.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class RegisterPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public RegisterPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void open(String uiBaseUrl) {
        driver.get(uiBaseUrl + "/register");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
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

    public void fillFirstName(String firstName) {
        WebElement input = driver.findElement(By.xpath("//label[contains(.,'Имя')]/following::input[1]"));
        input.clear();
        input.sendKeys(firstName);
    }

    public void fillLastName(String lastName) {
        WebElement input = driver.findElement(By.xpath("//label[contains(.,'Фамилия')]/following::input[1]"));
        input.clear();
        input.sendKeys(lastName);
    }

    public void fillNickname(String nickname) {
        WebElement input = driver.findElement(By.xpath("//label[contains(.,'Никнейм')]/following::input[1]"));
        input.clear();
        input.sendKeys(nickname);
    }

    public void fillPhone(String phone) {
        WebElement input = driver.findElement(By.xpath("//label[contains(.,'Телефон')]/following::input[1]"));
        input.clear();
        input.sendKeys(phone);
    }

    public void fillBirthDate(String birthDate) {
        WebElement input = driver.findElement(By.xpath("//label[contains(.,'Дата рождения')]/following::input[1]"));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].removeAttribute('readonly'); arguments[0].value = arguments[1];"
                        + "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));"
                        + "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                input,
                birthDate
        );
    }

    public void clickRegister() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@type='submit' and contains(normalize-space(.),'Зарегистрироваться')]")
        ));
        button.click();
    }

    public boolean isOpened() {
        return !driver.findElements(By.xpath("//h1[contains(.,'Регистрация')]")).isEmpty();
    }
}
