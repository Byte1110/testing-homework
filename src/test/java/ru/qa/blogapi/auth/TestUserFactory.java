package ru.qa.blogapi.auth;

import ru.qa.blogapi.models.UserRegistrationRequest;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class TestUserFactory {

    private TestUserFactory() {
    }

    public static UserRegistrationRequest validUser() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return new UserRegistrationRequest(
                "student_" + suffix + "@example.com",
                "SecurePass123!",
                "Student" + suffix,
                "Api",
                "student_" + suffix,
                "1990-01-02",
                "+7987" + ThreadLocalRandom.current().nextLong(1_000_000L, 10_000_000L)
        );
    }
}
