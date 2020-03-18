package sample;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DefaultValueFactoryContainer {

    private final Map<Class<?>, DefaultValueFactory<?>> storage;

    private DefaultValueFactoryContainer(@Nonnull Builder builder) {
        this.storage = Collections.unmodifiableMap(new HashMap<>(builder.storage));
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public <T> Optional<DefaultValueFactory<T>> find(@Nonnull Class<?> typeClass) {
        return Optional.ofNullable((DefaultValueFactory<T>) storage.get(typeClass));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final Map<Class<?>, DefaultValueFactory<?>> storage = new HashMap<>();

        private Builder() {
        }

        public <T> Builder register(
                @Nonnull Class<T> typeClass, @Nonnull DefaultValueFactory<T> factory) {
            storage.put(typeClass, factory);
            return this;
        }

        public DefaultValueFactoryContainer build() {
            return new DefaultValueFactoryContainer(this);
        }

    }

}
