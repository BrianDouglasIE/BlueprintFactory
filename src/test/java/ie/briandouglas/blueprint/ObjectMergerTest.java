package ie.briandouglas.blueprint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ObjectMergerTest {

    @Data
    @AllArgsConstructor
    static class Person {
        private String name;
        private Integer age;
        private String email;
    }

    @Test
    void testMergeNonNullFields_basicMerge() {
        Person target = new Person("Alice", 25, null);
        Person source = new Person(null, 30, "alice@example.com");

        ObjectMerger.mergeNonNullFields(target, source);

        assertEquals("Alice", target.getName());
        assertEquals(30, target.getAge());
        assertEquals("alice@example.com", target.getEmail());
    }

    @Test
    void testMergeNonNullFields_allFieldsNonNull() {
        Person target = new Person(null, null, null);
        Person source = new Person("Bob", 40, "bob@example.com");

        ObjectMerger.mergeNonNullFields(target, source);

        assertEquals("Bob", target.getName());
        assertEquals(40, target.getAge());
        assertEquals("bob@example.com", target.getEmail());
    }

    @Test
    void testMergeNonNullFields_sourceHasAllNulls() {
        Person target = new Person("Charlie", 50, "charlie@example.com");
        Person source = new Person(null, null, null);

        ObjectMerger.mergeNonNullFields(target, source);

        assertEquals("Charlie", target.getName());
        assertEquals(50, target.getAge());
        assertEquals("charlie@example.com", target.getEmail());
    }

    @Test
    void testMergeNonNullFields_nullTarget() {
        Person source = new Person("David", 60, "david@example.com");

        assertThrows(NullPointerException.class, () -> ObjectMerger.mergeNonNullFields(null, source));
    }

    @Test
    void testMergeNonNullFields_nullSource() {
        Person target = new Person("Eve", 35, "eve@example.com");

        assertThrows(NullPointerException.class, () -> ObjectMerger.mergeNonNullFields(target, null));
    }

    @Test
    void testMergeNonNullFields_differentTypes() {
        Person target = new Person("Frank", 45, "frank@example.com");
        Object otherType = new Object();

        assertThrows(IllegalArgumentException.class, () -> ObjectMerger.mergeNonNullFields(target, otherType));
    }

    @Test
    void testMergeNonNullFields_worksWithAMap() {
        Person target = new Person("Frank", 45, "frank@example.com");
        var source = Map.of("name", "John", "email", "john@example.com");

        ObjectMerger.mergeNonNullFields(target, source);

        assertEquals("John", target.getName());
        assertEquals(45, target.getAge());
        assertEquals("john@example.com", target.getEmail());
    }

    @Test
    void testMergeNonNullFields_skipFieldDoesntExistOnSource() {
        Person target = new Person("Frank", 45, "frank@example.com");
        var otherType = Map.of("differentType", 45);

        assertDoesNotThrow(() -> ObjectMerger.mergeNonNullFields(target, otherType));
    }

    @Test
    void testMergeNonNullFields_skipsInaccessibleField() {
        @Getter
        class SecretPerson {
            private final String secret = "top-secret";
        }

        SecretPerson target = new SecretPerson();
        SecretPerson source = new SecretPerson();

        try {
            var field = SecretPerson.class.getDeclaredField("secret");
            field.setAccessible(true);
            field.set(source, null);
        } catch (Exception e) {
            fail("Setup failed: " + e.getMessage());
        }

        ObjectMerger.mergeNonNullFields(target, source);

        assertEquals("top-secret", target.getSecret());
    }
}