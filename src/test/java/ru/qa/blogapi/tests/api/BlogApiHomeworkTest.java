package ru.qa.blogapi.tests.api;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.qa.blogapi.auth.AuthApiClient;
import ru.qa.blogapi.auth.AuthSession;
import ru.qa.blogapi.base.BaseAuthorizedApiTest;
import ru.qa.blogapi.models.LoginRequest;
import ru.qa.blogapi.models.PostCreateRequest;
import ru.qa.blogapi.models.RefreshTokenRequest;
import ru.qa.blogapi.models.UserRegistrationRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlogApiHomeworkTest extends BaseAuthorizedApiTest {

    @Test
    @Tag("smoke")
    @DisplayName("POST /api/auth/register -> should register user with valid required fields")
    void shouldRegisterUserWithValidRequiredFields() {
        UserRegistrationRequest requestBody = registrationRequest(randomEmail(), "SecurePass123!");

        given()
                .spec(requestSpec)
                .body(requestBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("message", equalTo("User registered successfully"))
                .body("user.id", notNullValue())
                .body("user.email", equalTo(requestBody.getEmail()))
                .body("user.firstName", equalTo(requestBody.getFirstName()))
                .body("user.lastName", equalTo(requestBody.getLastName()))
                .body("user.nickname", equalTo(requestBody.getNickname()))
                .body("user.birthDate", equalTo(requestBody.getBirthDate()))
                .body("user.phone", equalTo(requestBody.getPhone()));
    }

    @Test
    @Tag("regression")
    @DisplayName("POST /api/auth/register -> should return validation error for invalid email")
    void shouldReturnValidationErrorForInvalidEmailOnRegistration() {
        given()
                .spec(requestSpec)
                .body(registrationRequest("invalid-email", "SecurePass123!"))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body("error", notNullValue())
                .body("error.code", equalTo(400))
                .body("error.message", notNullValue());
    }

    @Test
    @Tag("smoke")
    @DisplayName("POST /api/login -> should login with valid credentials")
    void shouldLoginWithValidCredentials() {
        String email = randomEmail();
        String password = "SecurePass123!";
        registerUser(email, password);

        given()
                .spec(requestSpec)
                .body(new LoginRequest(email, password))
                .when()
                .post("/api/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("refresh_token", notNullValue());
    }

    @Test
    @Tag("regression")
    @DisplayName("POST /api/login -> should return unauthorized for wrong password")
    void shouldReturnUnauthorizedForWrongPassword() {
        String email = randomEmail();
        registerUser(email, "SecurePass123!");

        given()
                .spec(requestSpec)
                .body(new LoginRequest(email, "WrongPass123!"))
                .when()
                .post("/api/login")
                .then()
                .statusCode(401);
    }

    @Test
    @Tag("smoke")
    @DisplayName("POST /api/token/refresh -> should refresh access token by refresh token")
    void shouldRefreshAccessToken() {
        given()
                .spec(requestSpec)
                .body(new RefreshTokenRequest(authSession.getRefreshToken()))
                .when()
                .post("/api/token/refresh")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("refresh_token", notNullValue());
    }

    @Test
    @Tag("smoke")
    @DisplayName("GET /api/profile -> should return current user profile for authorized user")
    void shouldReturnCurrentUserProfile() {
        given()
                .spec(authorizedRequestSpec)
                .when()
                .get("/api/profile")
                .then()
                .statusCode(200)
                .body("user", notNullValue())
                .body("user.id", equalTo(authSession.getUserId()));
    }

    @Test
    @Tag("regression")
    @DisplayName("PUT /api/profile -> should update current user profile")
    void shouldUpdateCurrentUserProfile() {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Updated" + suffix(4));
        body.put("lastName", "Student" + suffix(4));
        body.put("nickname", "nick_" + suffix(6));
        body.put("phone", randomPhone());

        given()
                .spec(authorizedRequestSpec)
                .body(body)
                .when()
                .put("/api/profile")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("user.firstName", equalTo(body.get("firstName")))
                .body("user.lastName", equalTo(body.get("lastName")))
                .body("user.nickname", equalTo(body.get("nickname")));
    }

    @Test
    @Tag("regression")
    @DisplayName("GET /api/posts -> should return paginated list of posts")
    void shouldReturnPaginatedPostsList() {
        createPost(authorizedRequestSpec, "Pagination " + suffix(5), "technology", false);

        given()
                .spec(authorizedRequestSpec)
                .queryParam("page", 1)
                .queryParam("limit", 10)
                .when()
                .get("/api/posts")
                .then()
                .statusCode(200)
                .body("items", notNullValue())
                .body("items.size()", lessThanOrEqualTo(10))
                .body("totalItems", notNullValue())
                .body("itemsPerPage", equalTo(10))
                .body("page", equalTo(1))
                .body("pages", notNullValue());
    }

    @Test
    @Tag("regression")
    @DisplayName("GET /api/posts -> should filter posts by category")
    void shouldFilterPostsByCategory() {
        String category = "technology";
        createPost(authorizedRequestSpec, "Filtered " + suffix(5), category, false);

        Response response = given()
                .spec(authorizedRequestSpec)
                .queryParam("category", category)
                .when()
                .get("/api/posts")
                .then()
                .statusCode(200)
                .body("items", notNullValue())
                .extract()
                .response();

        List<String> categories = response.jsonPath().getList("items.category");
        assertTrue(categories.stream().allMatch(category::equals));
    }

    @Test
    @Tag("smoke")
    @DisplayName("POST /api/posts -> should create published post")
    void shouldCreatePublishedPost() {
        String title = "Published " + suffix(6);

        createPost(authorizedRequestSpec, title, "technology", false)
                .then()
                .statusCode(201)
                .body("status", equalTo("success"))
                .body("post.id", notNullValue())
                .body("post.title", equalTo(title))
                .body("post.isDraft", equalTo(false))
                .body("post.author.email", equalTo(authSession.getEmail()));
    }

    @Test
    @Tag("regression")
    @DisplayName("POST /api/posts -> should create draft post")
    void shouldCreateDraftPost() {
        String title = "Draft " + suffix(6);

        createPost(authorizedRequestSpec, title, "technology", true)
                .then()
                .statusCode(201)
                .body("status", equalTo("success"))
                .body("post.id", notNullValue())
                .body("post.title", equalTo(title))
                .body("post.isDraft", equalTo(true));
    }

    @Test
    @Tag("regression")
    @DisplayName("GET /api/posts/my -> should return only current user posts")
    void shouldReturnOnlyCurrentUserPosts() {
        createPost(authorizedRequestSpec, "My post " + suffix(5), "technology", false);

        Response response = given()
                .spec(authorizedRequestSpec)
                .when()
                .get("/api/posts/my")
                .then()
                .statusCode(200)
                .body("items", notNullValue())
                .extract()
                .response();

        List<String> authors = response.jsonPath().getList("items.author.email");
        assertTrue(authors.stream().allMatch(authSession.getEmail()::equals));
    }

    @Test
    @Tag("regression")
    @DisplayName("GET /api/posts/feed -> should return posts from other users")
    void shouldReturnFeedPosts() {
        AuthSession anotherUser = new AuthApiClient(requestSpec).createAuthorizedSession();
        RequestSpecification anotherUserSpec = authorizedSpecFor(anotherUser);
        createPost(anotherUserSpec, "Feed " + suffix(5), "technology", false);

        Response response = given()
                .spec(authorizedRequestSpec)
                .when()
                .get("/api/posts/feed")
                .then()
                .statusCode(200)
                .body("items", notNullValue())
                .extract()
                .response();

        List<String> authors = response.jsonPath().getList("items.author.email");
        assertTrue(authors.stream().noneMatch(authSession.getEmail()::equals));
    }

    @Test
    @Tag("regression")
    @DisplayName("GET /api/posts/{id} -> should return single post by id")
    void shouldReturnSinglePostById() {
        String title = "Single " + suffix(6);
        Integer postId = createPostAndGetId(authorizedRequestSpec, title, "technology", false);

        given()
                .spec(authorizedRequestSpec)
                .pathParam("id", postId)
                .when()
                .get("/api/posts/{id}")
                .then()
                .statusCode(200)
                .body("post.id", equalTo(postId))
                .body("post.title", equalTo(title));
    }

    @Test
    @Tag("regression")
    @DisplayName("PUT /api/posts/{id} -> should update existing post")
    void shouldUpdateExistingPost() {
        Integer postId = createPostAndGetId(authorizedRequestSpec, "Before " + suffix(6), "technology", false);
        String newTitle = "After " + suffix(6);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", newTitle);
        requestBody.put("body", "Updated body " + suffix(6));
        requestBody.put("description", "Updated description " + suffix(6));
        requestBody.put("category", "technology");

        given()
                .spec(authorizedRequestSpec)
                .pathParam("id", postId)
                .body(requestBody)
                .when()
                .put("/api/posts/{id}")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("post.id", equalTo(postId))
                .body("post.title", equalTo(newTitle))
                .body("post.description", equalTo(requestBody.get("description")));
    }

    @Test
    @Tag("regression")
    @DisplayName("DELETE /api/posts/{id} -> should delete post")
    void shouldDeletePost() {
        Integer postId = createPostAndGetId(authorizedRequestSpec, "Delete " + suffix(6), "technology", false);

        given()
                .spec(authorizedRequestSpec)
                .pathParam("id", postId)
                .when()
                .delete("/api/posts/{id}")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("message", notNullValue());

        given()
                .spec(authorizedRequestSpec)
                .pathParam("id", postId)
                .when()
                .get("/api/posts/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    @Tag("regression")
    @DisplayName("POST /api/posts/{id}/favorite -> should add post to favorites")
    void shouldAddPostToFavorites() {
        Integer postId = createPostAndGetId(authorizedRequestSpec, "Favorite " + suffix(6), "technology", false);

        given()
                .spec(authorizedRequestSpec)
                .pathParam("id", postId)
                .body(Map.of("isFavorite", true))
                .when()
                .post("/api/posts/{id}/favorite")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("isFavorite", equalTo(true));
    }

    @Test
    @Tag("regression")
    @DisplayName("GET /api/posts/favorites -> should return favorite posts")
    void shouldReturnFavoritePosts() {
        Integer postId = createPostAndGetId(authorizedRequestSpec, "Favorites list " + suffix(6), "technology", false);
        addPostToFavorites(postId);

        Response response = given()
                .spec(authorizedRequestSpec)
                .when()
                .get("/api/posts/favorites")
                .then()
                .statusCode(200)
                .body("items", notNullValue())
                .extract()
                .response();

        List<Integer> favoritePostIds = response.jsonPath().getList("items.id");
        assertTrue(favoritePostIds.contains(postId));
    }

    @Test
    @Tag("regression")
    @DisplayName("POST /api/files/upload -> should upload image file for post")
    void shouldUploadImageFileForPost() {
        uploadImage()
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("url", notNullValue())
                .body("filename", notNullValue())
                .body("mimeType", notNullValue());
    }

    @Test
    @Tag("regression")
    @DisplayName("GET /api/files/{id} -> should return uploaded file metadata")
    void shouldReturnUploadedFileMetadata() {
        Integer fileId = uploadImage()
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getInt("id");

        given()
                .spec(authorizedRequestSpec)
                .pathParam("id", fileId)
                .when()
                .get("/api/files/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(fileId))
                .body("url", notNullValue())
                .body("filename", notNullValue())
                .body("size", notNullValue())
                .body("mimeType", notNullValue());
    }

    @Test
    @Tag("regression")
    @DisplayName("POST /api/profile/report/{id} -> should create report for user")
    void shouldCreateUserReport() {
        AuthSession reportedUser = new AuthApiClient(requestSpec).createAuthorizedSession();
        String description = "Autotest report " + suffix(8);

        given()
                .spec(authorizedRequestSpec)
                .pathParam("id", reportedUser.getUserId())
                .body(Map.of("descriptionReport", description))
                .when()
                .post("/api/profile/report/{id}")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("message", notNullValue());

        assertNotEquals(authSession.getUserId(), reportedUser.getUserId());
    }

    private Response registerUser(String email, String password) {
        return given()
                .spec(requestSpec)
                .body(registrationRequest(email, password))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .extract()
                .response();
    }

    private Response createPost(RequestSpecification spec, String title, String category, boolean isDraft) {
        return given()
                .spec(spec)
                .body(postRequest(title, category, isDraft))
                .when()
                .post("/api/posts")
                .then()
                .extract()
                .response();
    }

    private Integer createPostAndGetId(RequestSpecification spec, String title, String category, boolean isDraft) {
        return createPost(spec, title, category, isDraft)
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getInt("post.id");
    }

    private void addPostToFavorites(Integer postId) {
        given()
                .spec(authorizedRequestSpec)
                .pathParam("id", postId)
                .body(Map.of("isFavorite", true))
                .when()
                .post("/api/posts/{id}/favorite")
                .then()
                .statusCode(200);
    }

    private Response uploadImage() {
        byte[] pngBytes = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90, 0x77, 0x53,
                (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41,
                0x54, 0x08, (byte) 0xD7, 0x63, (byte) 0xF8, (byte) 0xCF,
                (byte) 0xC0, 0x00, 0x00, 0x03, 0x01, 0x01, 0x00,
                0x18, (byte) 0xDD, (byte) 0x8D, (byte) 0xB0, 0x00, 0x00,
                0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE,
                0x42, 0x60, (byte) 0x82
        };

        return given()
                .baseUri(RestAssured.baseURI)
                .header("Authorization", "Bearer " + authSession.getAccessToken())
                .multiPart("file", "test-image.png", pngBytes, "image/png")
                .formParam("type", "post-image")
                .when()
                .post("/api/files/upload")
                .then()
                .extract()
                .response();
    }

    private RequestSpecification authorizedSpecFor(AuthSession session) {
        return new RequestSpecBuilder()
                .addRequestSpecification(requestSpec)
                .addHeader("Authorization", "Bearer " + session.getAccessToken())
                .build();
    }

    private UserRegistrationRequest registrationRequest(String email, String password) {
        return new UserRegistrationRequest(
                email,
                password,
                "Ronam",
                "Doe",
                "roman_" + suffix(6),
                "1990-01-02",
                randomPhone()
        );
    }

    private PostCreateRequest postRequest(String title, String category, boolean isDraft) {
        String suffix = suffix(6);
        return new PostCreateRequest(
                title,
                "This is body for REST-Assured practice post " + suffix,
                "Short description " + suffix,
                category,
                isDraft
        );
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
