package ru.qa.blogapi.tests.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.qa.blogapi.base.BaseUiTest;
import ru.qa.blogapi.pages.LoginPage;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginUiTest extends BaseUiTest {

    @Test
    @Tag("e2e")
    @DisplayName("UI /login -> should login with existing user credentials")
    void shouldLoginWithExistingUserCredentials() {
        String email = randomEmail();
        String password = "SecurePass123!";

        registerUserViaApi(email, password);

        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(uiBaseUrl);
        loginPage.fillEmail(email);
        loginPage.fillPassword(password);
        loginPage.clickLogin();

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));

        assertTrue(driver.getCurrentUrl().startsWith(uiBaseUrl));
    }

    private void registerUserViaApi(String email, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("firstName", "Ronam");
        body.put("lastName", "Doe");
        body.put("nickname", "roman_" + suffix(5));
        body.put("birthDate", "1990-01-02");
        body.put("phone", randomPhone());

        given()
                .baseUri(apiBaseUrl)
                .contentType("application/json")
                .accept("application/json")
                .body(body)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));
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
