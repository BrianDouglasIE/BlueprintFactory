package ie.briandouglas.blueprint.variant;

import java.util.List;

/**
 * Wraps a list of blueprint variations to avoid overload conflicts.
 *
 * @param <T> the type of each variation
 */
public record VariantList<T>(List<T> list) { }