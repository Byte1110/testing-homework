package ru.qa.blogapi.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qa.blogapi.base.BaseAuthorizedApiTest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class BlogApiHomeworkTest extends BaseAuthorizedApiTest {

    @Test
    @DisplayName("POST /api/auth/register -> should register user with valid required fields")
    void shouldRegisterUserWithValidRequiredFields() {
        String email = randomEmail();
        String password = "SecurePass123!";

        Map<String, Object> body = registrationBody(email, password);

        given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("message", equalTo("User registered successfully"))
                .body("user.id", notNullValue())
                .body("user.email", equalTo(email))
                .body("user.firstName", equalTo(body.get("firstName")))
                .body("user.lastName", equalTo(body.get("lastName")))
                .body("user.nickname", equalTo(body.get("nickname")))
                .body("user.birthDate", equalTo(body.get("birthDate")))
                .body("user.phone", equalTo(body.get("phone")));
    }

    @Test
    @DisplayName("POST /api/auth/register -> should return validation error for invalid email")
    void shouldReturnValidationErrorForInvalidEmailOnRegistration() {
        Map<String, Object> body = registrationBody("invalid-email", "SecurePass123!");

        given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body("error", notNullValue())
                .body("error.code", equalTo(400))
                .body("error.message", notNullValue());
    }

    @Test
    @DisplayName("POST /api/login -> should login with valid credentials")
    void shouldLoginWithValidCredentials() {
        String email = randomEmail();
        String password = "SecurePass123!";

        registerUser(email, password);

        given()
                .spec(requestSpec)
                .body(loginBody(email, password))
                .when()
                .post("/api/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("refresh_token", notNullValue());
    }

    private Response registerUser(String email, String password) {
        return given()
                .spec(requestSpec)
                .body(registrationBody(email, password))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .extract()
                .response();
    }

    private Map<String, Object> registrationBody(String email, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("firstName", "Ronam");
        body.put("lastName", "Doe");
        body.put("nickname", "roman_" + suffix(5));
        body.put("birthDate", "1990-01-02");
        body.put("phone", randomPhone());
        return body;
    }

    private Map<String, Object> loginBody(String email, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", email);
        body.put("password", password);
        return body;
    }

    private String randomEmail() {
        return "student_" + suffix(8) + "@example.com";
    }

    private String randomPhone() {
        return "+79" + UUID.randomUUID()
                .toString()
                .replaceAll("[^0-9]", "")
                .substring(0, 9);
    }

    private String suffix(int length) {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, length);
    }

    class MyClass {
        private MyClass() {
            //тест
        }
    }
}