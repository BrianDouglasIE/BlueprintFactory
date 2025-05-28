package ie.briandouglas.blueprint;

import lombok.NonNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ObjectMerger {

    public static <T> void mergeNonNullFields(@NonNull T target, @NonNull T source) {
        Class<?> targetClass = target.getClass();
        Class<?> sourceClass = source.getClass();

        if (!targetClass.equals(sourceClass)) {
            throw new IllegalArgumentException("Source and target must be of the same type: " +
                    targetClass.getName() + " vs " + sourceClass.getName());
        }

        for (Field field : getAllFields(targetClass)) {
            field.setAccessible(true);
            try {
                Object value = field.get(source);
                if (value != null) {
                    field.set(target, value);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to merge field: " + field.getName(), e);
            }
        }
    }

    public static <T> void mergeNonNullFields(@NonNull T target, @NonNull Map<String, ?> sourceMap) {
        Class<?> targetClass = target.getClass();

        for (Map.Entry<String, ?> entry : sourceMap.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();

            if (value == null) continue;

            try {
                Field field = targetClass.getDeclaredField(fieldName);
                field.setAccessible(true);

                if (field.getType().isAssignableFrom(value.getClass())) {
                    field.set(target, value);
                } else {
                    throw new IllegalArgumentException("Cannot assign value of type "
                            + value.getClass().getName() + " to field " + fieldName);
                }
            } catch (NoSuchFieldException ignored) {
                // Field doesn't exist on target, skip it.
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + fieldName, e);
            }
        }
    }

    private static List<Field> getAllFields(Class<?> targetClass) {
        List<Field> fields = new ArrayList<>();
        while (targetClass != null && targetClass != Object.class) {
            fields.addAll(Arrays.asList(targetClass.getDeclaredFields()));
            targetClass = targetClass.getSuperclass();
        }
        return fields;
    }
}
