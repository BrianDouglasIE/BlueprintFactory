package ie.briandouglas.blueprint.variant;

import java.util.List;

/**
 * Wraps a list of map-based variations to distinguish from other list types.
 */
public record VariantMapList(List<VariantMap> list) { }
