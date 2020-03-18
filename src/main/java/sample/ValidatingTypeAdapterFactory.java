package sample;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.util.Collection;

public class ValidatingTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
        return new ValidatingTypeAdapter<>(delegate);
    }

    private static class ValidatingTypeAdapter<T> extends TypeAdapter<T> {

        private final TypeAdapter<T> delegate;

        private ValidatingTypeAdapter(TypeAdapter<T> delegate) {
            this.delegate = Validate.notNull(delegate, "delegate");
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {
            validate(value);
            delegate.write(out, value);
        }

        @Override
        public T read(JsonReader in) throws IOException {
            T value = delegate.read(in);
            validate(value);
            return value;
        }

        private void validate(Object value) {
            if (value instanceof Collection) {
                Validatables.validate((Collection<?>) value);
            } else if (value instanceof Validatable) {
                ((Validatable) value).validate();
            }
        }

    }

}
