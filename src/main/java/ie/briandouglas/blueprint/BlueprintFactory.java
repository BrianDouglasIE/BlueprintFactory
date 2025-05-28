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

    private final List<Consumer<T>> customSetters = new ArrayList<>();

    /**
     * Returns a fresh copy of the blueprint object.
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
     *
     * @param variation a map of field names and values to override
     */
    public T create(Map<String, ?> variation) {
        T instance = create();
        ObjectMerger.mergeNonNullFields(instance, variation);
        return instance;
    }

    /**
     * Creates an instance with field values overridden by another instance.
     *
     * @param variation an object with override values
     */
    public T create(T variation) {
        T instance = create();
        ObjectMerger.mergeNonNullFields(instance, variation);
        return instance;
    }

    /**
     * Creates multiple instances using the blueprint.
     *
     * @param count the number of instances to create
     */
    public List<T> create(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> create())
                .toList();
    }

    /**
     * Creates instances by applying a list of instance-based variations.
     *
     * @param variants wrapper for list of variation instances
     */
    public List<T> create(VariantList<T> variants) {
        return variants.list().stream().map(this::create).toList();
    }

    /**
     * Creates instances by applying a list of map-based variations.
     *
     * @param variants wrapper for list of variation maps
     */
    public List<T> create(VariantMapList variants) {
        return variants.list().stream().map(vm -> create(vm.data())).toList();
    }

    /**
     * Sets a related value using a supplier.
     *
     * @param relatedFactory factory for the related value
     * @param setter         setter to apply the value to the instance
     */
    public <R> BlueprintFactory<T> with(Supplier<R> relatedFactory, BiConsumer<T, R> setter) {
        customSetters.add(instance -> setter.accept(instance, relatedFactory.get()));
        return this;
    }

    /**
     * Sets a related list of values by count.
     *
     * @param relatedFactory factory accepting count and returning a list
     * @param count          number of values to generate
     * @param setter         setter to apply the list to the instance
     */
    public <R> BlueprintFactory<T> with(Function<Integer, List<R>> relatedFactory, int count, BiConsumer<T, List<R>> setter) {
        customSetters.add(instance -> setter.accept(instance, relatedFactory.apply(count)));
        return this;
    }

    /**
     * Sets a related value by passing a variant to a factory.
     *
     * @param relatedFactory function accepting a variant and returning the related value
     * @param variant        the variation input
     * @param setter         setter to apply the value to the instance
     */
    public <V, R> BlueprintFactory<T> with(Function<V, R> relatedFactory, V variant, BiConsumer<T, R> setter) {
        customSetters.add(instance -> setter.accept(instance, relatedFactory.apply(variant)));
        return this;
    }

    private void applyCustomSetters(T instance) {
        customSetters.forEach(setter -> setter.accept(instance));
        customSetters.clear();
    }
}
