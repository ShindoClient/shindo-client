package me.miki.shindo.management.annotation.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Small cache for reflective lookups used by the annotation utilities.
 */
public final class AnnotationCache {

    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private AnnotationCache() {
    }

    public static List<Field> getAllFields(Class<?> type) {
        if (type == null) {
            return Collections.emptyList();
        }
        return FIELD_CACHE.computeIfAbsent(type, AnnotationCache::collectFields);
    }

    private static List<Field> collectFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> cursor = type;
        while (cursor != null && cursor != Object.class) {
            Collections.addAll(fields, cursor.getDeclaredFields());
            cursor = cursor.getSuperclass();
        }
        return fields;
    }
}
