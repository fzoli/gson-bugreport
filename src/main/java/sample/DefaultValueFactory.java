package sample;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface DefaultValueFactory<T> {
    @Nonnull
    T create();
}
