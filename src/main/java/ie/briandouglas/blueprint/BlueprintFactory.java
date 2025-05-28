package ie.briandouglas.blueprint;

import ie.briandouglas.blueprint.variant.VariantList;
import ie.briandouglas.blueprint.variant.VariantMapList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.*;
import java.util.stream.IntStream;

/**
 * A flexible factory for creating and customizing entity instances.
 * Supports cloning a blueprint, applying variations, and composing related entities.
 *
 * @param <T> the type of object this factory creates
 */
public abstract class BlueprintFactory<T> {

    private final List<Consumer<T>> customSetters;

    /** Default constructor starts with empty setters. */
    protected BlueprintFactory() {
        this.customSetters = List.of();
    }

    /** Internal constructor for creating derived factories with combined setters */
    protected BlueprintFactory(List<Consumer<T>> customSetters) {
        this.customSetters = List.copyOf(customSetters);
    }

    /**
     * Returns a fresh copy of the blueprint object.
     * Must be implemented by subclasses.
     */
    protected abstract T blueprint();

    /**
     * Creates a new instance based on the blueprint and applies any configured setters.
     */
    public T create() {
        T instance = blueprint();
        applyCustomSetters(instance);
        return instance;
    }

    /**
     * Creates an instance with field values overridden by the provided map.
     */
    public T create(Map<String, ?> variation) {
        T instance = create();
        ObjectMerger.mergeNonNullFields(instance, variation);
        return instance;
    }

    /**
     * Creates an instance with field values overridden by another instance.
     */
    public T create(T variation) {
        T instance = create();
        ObjectMerger.mergeNonNullFields(instance, variation);
        return instance;
    }

    /**
     * Creates multiple instances using the blueprint.
     */
    public List<T> create(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> create())
                .toList();
    }

    /**
     * Creates instances by applying a list of instance-based variations.
     */
    public List<T> create(VariantList<T> variants) {
        return variants.list().stream()
                .map(this::create)
                .toList();
    }

    /**
     * Creates instances by applying a list of map-based variations.
     */
    public List<T> create(VariantMapList variants) {
        return variants.list().stream()
                .map(vm -> create(vm.data()))
                .toList();
    }

    /**
     * Adds a related value using a supplier.
     * Returns a new BlueprintFactory with this setter appended.
     */
    public <R> BlueprintFactory<T> with(Supplier<R> relatedFactory, BiConsumer<T, R> setter) {
        return new BlueprintFactory<>(appendSetter(instance -> setter.accept(instance, relatedFactory.get()))) {
            @Override
            protected T blueprint() {
                return BlueprintFactory.this.blueprint();
            }
        };
    }

    /**
     * Adds a related list of values by count.
     * Returns a new BlueprintFactory with this setter appended.
     */
    public <R> BlueprintFactory<T> with(Function<Integer, List<R>> relatedFactory, int count, BiConsumer<T, List<R>> setter) {
        return new BlueprintFactory<>(appendSetter(instance -> setter.accept(instance, relatedFactory.apply(count)))) {
            @Override
            protected T blueprint() {
                return BlueprintFactory.this.blueprint();
            }
        };
    }

    /**
     * Adds a related value by passing a variant to a factory.
     * Returns a new BlueprintFactory with this setter appended.
     */
    public <V, R> BlueprintFactory<T> with(Function<V, R> relatedFactory, V variant, BiConsumer<T, R> setter) {
        return new BlueprintFactory<>(appendSetter(instance -> setter.accept(instance, relatedFactory.apply(variant)))) {
            @Override
            protected T blueprint() {
                return BlueprintFactory.this.blueprint();
            }
        };
    }

    /** Applies all custom setters to the instance */
    private void applyCustomSetters(T instance) {
        customSetters.forEach(setter -> setter.accept(instance));
    }

    /** Returns a new List with the new setter appended */
    private List<Consumer<T>> appendSetter(Consumer<T> newSetter) {
        List<Consumer<T>> newList = new ArrayList<>(customSetters);
        newList.add(newSetter);
        return newList;
    }
}