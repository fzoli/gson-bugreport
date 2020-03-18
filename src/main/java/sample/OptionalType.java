package sample;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This data-type behaves like a regular object wrapped into {@link java.util.Optional}.
 * The object is never {@code null}, but the value can be absent (empty).
 * Calling {@code object.get()} when the value is absent produces {@link NullPointerException}
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionalType {
}
