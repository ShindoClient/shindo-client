package me.miki.shindo.management.annotation;

import me.miki.shindo.management.annotation.internal.AnnotationCache;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Small helper collection for working with custom Shindo annotations.
 */
public final class AnnotationUtils {

    private AnnotationUtils() {
    }

    public static Optional<String> getName(AnnotatedElement element) {
        Name annotation = element.getAnnotation(Name.class);
        return annotation != null ? Optional.of(annotation.value()) : Optional.empty();
    }

    public static Optional<String> getDescription(AnnotatedElement element) {
        Description annotation = element.getAnnotation(Description.class);
        return annotation != null ? Optional.of(annotation.value()) : Optional.empty();
    }

    public static Optional<List<String>> getAuthors(AnnotatedElement element) {
        Author annotation = element.getAnnotation(Author.class);
        if (annotation == null) {
            return Optional.empty();
        }
        return Optional.of(Arrays.asList(annotation.value()));
    }

    public static Optional<String> getVersion(AnnotatedElement element) {
        Version annotation = element.getAnnotation(Version.class);
        return annotation != null ? Optional.of(annotation.value()) : Optional.empty();
    }

    public static Optional<String> getSince(AnnotatedElement element) {
        Since annotation = element.getAnnotation(Since.class);
        return annotation != null ? Optional.of(annotation.value()) : Optional.empty();
    }

    public static Optional<String> getLastModified(AnnotatedElement element) {
        LastModified annotation = element.getAnnotation(LastModified.class);
        return annotation != null ? Optional.of(annotation.value()) : Optional.empty();
    }

    /**
     * Validates {@link NotNull} and {@link Range} annotations present on the provided instance.
     *
     * @param target instance to validate
     * @return list of validation issues. Empty list means OK.
     */
    public static List<String> validate(Object target) {
        List<String> issues = new ArrayList<>();
        if (target == null) {
            issues.add("Target instance is null");
            return issues;
        }

        Class<?> type = target.getClass();
        for (Field field : AnnotationCache.getAllFields(type)) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(target);

                if (field.isAnnotationPresent(NotNull.class) && value == null) {
                    issues.add(buildMessage(type, field, "@NotNull violation"));
                }

                Range range = field.getAnnotation(Range.class);
                if (range != null && value != null && value instanceof Number) {
                    double numeric = ((Number) value).doubleValue();
                    if (numeric < range.min() || numeric > range.max()) {
                        issues.add(buildMessage(type, field,
                                "@Range violation (expected between " + range.min() + " and " + range.max() + ", got " + numeric + ")"));
                    }
                }
            } catch (IllegalAccessException e) {
                issues.add(buildMessage(type, field, "Failed to access field: " + e.getMessage()));
            }
        }
        return issues;
    }

    public static <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement element, Class<A> type) {
        return Optional.ofNullable(element.getAnnotation(type));
    }

    private static String buildMessage(Class<?> owner, Field field, String message) {
        return owner.getSimpleName() + "." + field.getName() + ": " + message;
    }
}
