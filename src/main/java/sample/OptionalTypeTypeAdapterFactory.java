package sample;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public final class OptionalTypeTypeAdapterFactory implements TypeAdapterFactory {

    @Builder
    private static final class OptionalTypeFieldWrapper {

        @Nonnull
        private final Field field;

        @Nullable
        private final DefaultValueFactory<?> defaultValueFactory;

        @Nonnull
        public DefaultValueFactory<?> requireDefaultValueFactory() {
            return Validate.notNull(defaultValueFactory);
        }

    }

    @Nonnull
    private final DefaultValueFactoryContainer defaultValueFactoryContainer;

    @Nullable
    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> typeToken) {
        final Collection<OptionalTypeFieldWrapper> optionalTypeFields = classesOf(typeToken.getRawType())
                .stream()
                .flatMap(c -> Stream.of(c.getDeclaredFields()))
                .map(field -> OptionalTypeFieldWrapper.builder()
                        .field(field)
                        .defaultValueFactory(defaultValueFactoryContainer.find(field.getType()).orElse(null))
                        .build()
                )
                .filter(wrapper -> wrapper.defaultValueFactory != null)
                .peek(wrapper -> wrapper.field.setAccessible(true))
                .collect(Collectors.toList());
        if (optionalTypeFields.isEmpty()) {
            // if the class does not have any optional-like fields, delegate it elsewhere
            return null;
        }
        final TypeAdapter<T> delegateTypeAdapter = gson.getDelegateAdapter(this, typeToken);
        return new TypeAdapter<T>() {
            @Override
            public void write(final JsonWriter out, final T value)
                    throws IOException {
                delegateTypeAdapter.write(out, value);
            }

            @Override
            public T read(final JsonReader in)
                    throws IOException {
                final T read = delegateTypeAdapter.read(in);
                normalizeAbsents(read, optionalTypeFields);
                return read;
            }
        };
    }

    private static Collection<Class<?>> classesOf(final Class<?> clazz) {
        final List<Class<?>> classes = new ArrayList<>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            classes.add(0, c);
        }
        return classes;
    }

    private static void normalizeAbsents(final Object o, final Iterable<OptionalTypeFieldWrapper> optionalTypeFields) {
        for ( final OptionalTypeFieldWrapper wrapper : optionalTypeFields ) {
            try {
                final DefaultValueFactory<?> defaultValueFactory = wrapper.requireDefaultValueFactory();
                final Field field = wrapper.field;
                final Object fieldValue = field.get(o);
                if (fieldValue == null) {
                    final Class<?> fieldType = field.getType();
                    final Object defaultValue = Validate.notNull(
                            defaultValueFactory.create(), "Null can not be the default value"
                    );
                    field.set(o, defaultValue);
                }
            } catch ( final IllegalAccessException ex ) {
                throw new RuntimeException(ex);
            }
        }
    }

}
