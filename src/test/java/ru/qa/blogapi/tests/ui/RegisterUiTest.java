package ru.qa.blogapi.tests.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.qa.blogapi.base.BaseUiTest;
import ru.qa.blogapi.pages.RegisterPage;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterUiTest extends BaseUiTest {

    @Test
    @Tag("e2e")
    @DisplayName("UI /register -> should register user from registration page")
    void shouldRegisterUserFromRegisterPage() {
        String email = randomEmail();
        String password = "SecurePass123!";
        String firstName = "Ronam";
        String lastName = "Doe";
        String nickname = "roman_" + suffix(5);
        String phone = randomPhone();
        String birthDate = "1990-01-02";

        RegisterPage registerPage = new RegisterPage(driver);

        registerPage.open(uiBaseUrl);
        registerPage.fillEmail(email);
        registerPage.fillPassword(password);
        registerPage.fillFirstName(firstName);
        registerPage.fillLastName(lastName);
        registerPage.fillNickname(nickname);
        registerPage.fillPhone(phone);
        registerPage.fillBirthDate(birthDate);
        registerPage.clickRegister();

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/login"));

        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    private String randomEmail() {
        return "student_" + suffix(8) + "@example.com";
    }

    private String randomPhone() {
        return "+79" + ThreadLocalRandom.current().nextLong(100_000_000L, 1_000_000_000L);
    }

    private String suffix(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, length);
    }
}
