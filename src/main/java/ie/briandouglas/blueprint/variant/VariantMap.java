package ie.briandouglas.blueprint.variant;

import java.util.Map;

/**
 * Wraps a map of field overrides for customizing a blueprint. Used to avoid method overload ambiguity.
 */
public record VariantMap(Map<String, ?> data) { }