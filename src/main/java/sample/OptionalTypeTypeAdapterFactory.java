package sample;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class OptionalTypeTypeAdapterFactory
        implements TypeAdapterFactory {

    private static final TypeAdapterFactory instance = new OptionalTypeTypeAdapterFactory();

    private OptionalTypeTypeAdapterFactory() {
    }

    static TypeAdapterFactory get() {
        return instance;
    }

    @Nullable
    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> typeToken) {
        final Collection<Field> optionalTypeFields = classesOf(typeToken.getRawType())
                .stream()
                .flatMap(c -> Stream.of(c.getDeclaredFields()))
                .filter(field -> field.getAnnotation(OptionalType.class) != null)
                .peek(field -> field.setAccessible(true))
                .collect(Collectors.toList());
        if ( optionalTypeFields.isEmpty() ) {
            // if the class does not have any OptionalType-annotated fields, delegate it elsewhere
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
        for ( Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass() ) {
            classes.add(0, c);
        }
        return classes;
    }

    private static void normalizeAbsents(final Object o, final Iterable<Field> optionalTypeFields) {
        for ( final Field field : optionalTypeFields ) {
            try {
                final Object fieldValue = field.get(o);
                if ( fieldValue == null ) {
                    final Class<?> fieldType = field.getType();
                    final Object absent;
                    // TODO: Maintain the supported classes somehow...
                    if ( fieldType == PhoneNumber.class ) {
                        absent = PhoneNumber.absent();
                    } else {
                        throw new UnsupportedOperationException("Cannot normalize " + fieldType);
                    }
                    field.set(o, absent);
                }
            } catch ( final IllegalAccessException ex ) {
                throw new RuntimeException(ex);
            }
        }
    }

}
