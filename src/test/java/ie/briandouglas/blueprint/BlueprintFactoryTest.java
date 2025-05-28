package ie.briandouglas.blueprint;

import ie.briandouglas.blueprint.variant.VariantList;
import ie.briandouglas.blueprint.variant.VariantMap;
import ie.briandouglas.blueprint.variant.VariantMapList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BlueprintFactoryTest {
    private Item itemBlueprint;
    private ItemFactory itemFactory;
    private Store storeBlueprint;
    private StoreFactory storeFactory;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Store implements Cloneable {
        private String name;
        private Item bestSeller;
        private List<Item> items;

        @Override
        public Store clone() {
            try {
                return (Store) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }

    static class StoreFactory extends BlueprintFactory<Store> {
        @Override
        protected Store blueprint() {
            return new Store();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Item implements Cloneable {
        private String name;
        private Float price;
        private Float weight;

        @Override
        public Item clone() {
            try {
                return (Item) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }

    static class ItemFactory extends BlueprintFactory<Item> {
        @Override
        protected Item blueprint() {
            return new Item();
        }
    }

    @BeforeEach
    void setUp() {
        itemBlueprint = new Item();
        itemBlueprint.setName("Blueprint");
        itemBlueprint.setPrice(5.0f);
        itemBlueprint.setWeight(5.0f);
        itemFactory = new ItemFactory() {
            protected Item blueprint() {
                return itemBlueprint.clone();
            }
        };

        storeBlueprint = new Store();
        storeBlueprint.setName("StoreBlueprint");
        storeFactory = new StoreFactory() {
            protected Store blueprint() {
                return storeBlueprint.clone();
            }
        };
    }

    @Test
    void testCreationOfBluePrintWithInstanceVariation() {
        var variation = new Item("Cookie", 1.99f, 1.0f);

        var instance = itemFactory.create(variation);
        assertEquals("Cookie", instance.getName());
        assertEquals(1.99f, instance.getPrice());
        assertEquals(1.0f, instance.getWeight());
    }

    @Test
    void testCreationOfBluePrintWithMapVariation() {
        var variation = Map.of("name", "Cookie", "price", 1.99f, "weight", 1.0f);

        var instance = itemFactory.create(variation);
        assertEquals("Cookie", instance.getName());
        assertEquals(1.99f, instance.getPrice());
        assertEquals(1.0f, instance.getWeight());
    }

    @Test
    void testCreationOfBluePrintSequenceWithInstances() {
        List<Item> sequence = List.of(
                new Item("Strawberry", 1.0f, 1.0f),
                new Item("Apple", 1.0f, 1.0f),
                new Item("Orange", 1.0f, 1.0f)
        );

        var instances = itemFactory.create(new VariantList<>(sequence));
        assertEquals(3, instances.size());
        assertEquals("Strawberry", instances.get(0).getName());
        assertEquals("Apple", instances.get(1).getName());
        assertEquals("Orange", instances.get(2).getName());
    }

    @Test
    void testCreationOfBluePrintSequenceWithMaps() {
        List<VariantMap> sequence = List.of(
                new VariantMap(Map.of("name", "Strawberry")),
                new VariantMap(Map.of("name", "Apple")),
                new VariantMap(Map.of("name", "Orange"))
        );

        var instances = itemFactory.create(new VariantMapList(sequence));
        assertEquals(3, instances.size());
        assertEquals("Strawberry", instances.get(0).getName());
        assertEquals("Apple", instances.get(1).getName());
        assertEquals("Orange", instances.get(2).getName());
    }

    @Test
    void testCreationWithCustomSetter() {
        var result = storeFactory
                .with(itemFactory::create, Store::setBestSeller)
                .create();

        assertEquals(itemBlueprint, result.getBestSeller());
    }

    @Test
    void testCreationWithCustomSetterAndInstanceVariant() {
        var testItem = new Item("Strawberry", 1.0f, 1.0f);
        var result = storeFactory
                .with(itemFactory::create, testItem, Store::setBestSeller)
                .create();

        assertEquals(testItem, result.getBestSeller());
    }

    @Test
    void testCreationWithCustomSetterAndManyInstanceVariants() {
        List<Item> sequence = List.of(
                new Item("Strawberry", 1.0f, 1.0f),
                new Item("Apple", 1.0f, 1.0f),
                new Item("Orange", 1.0f, 1.0f)
        );

        var result = storeFactory
                .with(itemFactory::create, new VariantList<>(sequence), Store::setItems)
                .create();

        assertEquals(sequence, result.getItems());
    }

    @Test
    void testCreationWithCustomSetterAndManyMapVariants() {
        List<VariantMap> sequence = List.of(
                new VariantMap(Map.of("name", "Strawberry")),
                new VariantMap(Map.of("name", "Apple")),
                new VariantMap(Map.of("name", "Orange"))
        );

        var result = storeFactory
                .with(itemFactory::create, new VariantMapList(sequence), Store::setItems)
                .create();

        var expected = sequence.stream()
                .map(vm -> itemFactory.create(vm.data()))
                .toList();

        assertEquals(expected, result.getItems());
    }

    @Test
    void testCreationWithCustomSetterAndManyMapVariantsWithAdditionalMapVariant() {
        List<VariantMap> sequence = List.of(
                new VariantMap(Map.of("name", "Strawberry")),
                new VariantMap(Map.of("name", "Apple")),
                new VariantMap(Map.of("name", "Orange"))
        );

        var result = storeFactory
                .with(itemFactory::create, new VariantMapList(sequence), Store::setItems)
                .create(Map.of("name", "test"));

        var expected = sequence.stream()
                .map(vm -> itemFactory.create(vm.data()))
                .toList();

        assertEquals(expected, result.getItems());
    }

    @Test
    void testCreationWithCustomSetterAndManyMapVariantsWithAdditionalInstanceVariant() {
        List<VariantMap> sequence = List.of(
                new VariantMap(Map.of("name", "Strawberry")),
                new VariantMap(Map.of("name", "Apple")),
                new VariantMap(Map.of("name", "Orange"))
        );

        var instance = new Store();
        instance.setName("test");
        var result = storeFactory
                .with(itemFactory::create, new VariantMapList(sequence), Store::setItems)
                .create(instance);

        var expected = sequence.stream()
                .map(vm -> itemFactory.create(vm.data()))
                .toList();

        assertEquals(expected, result.getItems());
    }

    @Test
    void testCreationWithCustomSetterCreatingMany() {
        var result = storeFactory
                .with(itemFactory::create, 3, Store::setItems)
                .create();

        assertEquals(3, result.getItems().size());
    }
}