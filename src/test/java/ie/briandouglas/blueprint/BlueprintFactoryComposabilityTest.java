package ie.briandouglas.blueprint;

import com.github.javafaker.Faker;
import ie.briandouglas.blueprint.variant.VariantList;
import ie.briandouglas.blueprint.variant.VariantMap;
import ie.briandouglas.blueprint.variant.VariantMapList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class BlueprintFactoryComposabilityTest {
    private PersonFactory personFactory;
    private PetFactory petFactory;

    @Data
    @AllArgsConstructor
    static class Person {
        private String name;
        private int age;
        private List<Pet> pets;
    }

    @Data
    @AllArgsConstructor
    static class Pet {
        private String name;
        private String type;
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    static class PersonFactory extends BlueprintFactory<Person> {
        private final Faker faker;
        private final PetFactory petFactory;

        @Override
        protected Person blueprint() {
            return new Person(faker.name().firstName(), faker.random().nextInt(18, 99), petFactory.create(2));
        }
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    static class PetFactory extends BlueprintFactory<Pet> {
        private final Faker faker;

        @Override
        protected Pet blueprint() {
            return new Pet(faker.name().firstName(), faker.animal().name());
        }
    }

    @BeforeEach
    public void setUp() {
        Faker faker = new Faker();
        petFactory = new PetFactory(faker);
        personFactory = new PersonFactory(faker, petFactory);
    }

    @Test
    public void testComposability() {
        List<VariantMap> petMap = List.of(
                new VariantMap(Map.of("name", "toad", "type", "toad")),
                new VariantMap(Map.of("name", "toadette", "type", "toad"))
        );
        var toadFactory = personFactory.with(petFactory::create, new VariantMapList(petMap), Person::setPets);

        List<Pet> expectedPets = petFactory.create(new VariantMapList(petMap));
        List<Person> persons = toadFactory.create(3);

        for (Person person : persons) {
            assertEquals(expectedPets, person.getPets());
        }

        // ensure custom setters persist
        for (Person person : toadFactory.create(3)) {
            assertEquals(expectedPets, person.getPets());
        }

        // ensure original person factory is unaltered
        assertNotEquals(personFactory.create(), toadFactory.create());
    }
}
