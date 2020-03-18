package sample;

/**
 * This data-type behaves like a regular object wrapped into {@link java.util.Optional}.
 * The object is never {@code null}, but the value can be absent (empty).
 * Calling {@code object.get()} when the value is absent produces {@link NullPointerException}
 */
public @interface OptionalType {
}
