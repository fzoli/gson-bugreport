package sample;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.IOException;

@AllArgsConstructor
public final class PhoneNumberTypeAdapterFactory implements TypeAdapterFactory {

    @Nonnull
    private final PhoneNumberParser parser;

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (type.getRawType() != PhoneNumber.class) {
            return null;
        }
        TypeAdapter<String> delegate = gson.getAdapter(String.class);
        return (TypeAdapter<T>) new PhoneNumberTypeAdapter(parser, delegate);
    }

    @AllArgsConstructor
    private static final class PhoneNumberTypeAdapter extends TypeAdapter<PhoneNumber> {

        @Nonnull
        private final PhoneNumberParser parser;

        @Nonnull
        private final TypeAdapter<String> delegate;

        @Override
        public void write(JsonWriter out, PhoneNumber src) throws IOException {
            if (src == null) {
                // PhoneNumber is like optional, reject nulls
                throw new NullPointerException();
            }
            if (src.hasEmptyRaw()) {
                // raw is empty, so we send null object
                out.nullValue();
                return;
            }
            // not empty, has a valid or invalid number; send it as-is
            delegate.write(out, src.toRawString());
        }

        @Nonnull
        @Override
        public PhoneNumber read(JsonReader in) throws IOException {
            // FIXME: What about JsonToken.UNDEFINED ?
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return PhoneNumber.absent();
            }
            String text = delegate.read(in);
            if (StringUtils.isBlank(text)) {
                return PhoneNumber.absent();
            }
            try {
                return parser.parse(text);
            }
            catch (PhoneNumber.ParseException ex) {
                throw new JsonParseException(ex);
            }
        }

    }

}
