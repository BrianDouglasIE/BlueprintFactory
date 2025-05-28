package ie.briandouglas.blueprint;

import com.github.javafaker.Faker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class BlueprintFactoryFakerTest {
    private UserFactory userFactory;

    @Data
    @AllArgsConstructor
    static class User {
        private String name;
        private int age;
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    static class UserFactory extends BlueprintFactory<User> {
        private final Faker faker;

        @Override
        protected User blueprint() {
            return new User(faker.name().firstName(), faker.random().nextInt(18, 99));
        }
    }

    @BeforeEach
    public void setUp() {
        Faker faker = new Faker();
        userFactory = new UserFactory(faker);
    }

    @Test
    public void testUniqueUsersCreatedUsingFaker() {
        assertNotEquals(userFactory.create(), userFactory.create());
    }

    @Test
    public void testUniqueUsersCreatedInSequenceUsingFaker() {
        var users = userFactory.create(3);
        var names = users.stream().map(User::getName).collect(Collectors.toSet());
        assertEquals(3, names.size());
    }
}
