package sample;

import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

public class PhoneNumberTypeAdapter implements JsonSerializer<PhoneNumber>, JsonDeserializer<PhoneNumber> {

    @Nonnull
    private final PhoneNumberParser parser;

    public PhoneNumberTypeAdapter() {
        this.parser = DefaultPhoneNumberParser.INSTANCE;
    }

    public PhoneNumberTypeAdapter(@Nonnull PhoneNumberParser parser) {
        this.parser = Validate.notNull(parser, "parser");
    }

    @Nonnull
    @Override
    public JsonElement serialize(PhoneNumber src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            // PhoneNumber is like optional, reject nulls
            throw new NullPointerException();
        }
        if (src.hasEmptyRaw()) {
            // raw is empty, so we send null object
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(src.toRawString()); // not empty, has a valid or invalid number; send it as-is
    }

    @Nonnull
    @Override
    public PhoneNumber deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String text = json.getAsString();
        if (StringUtils.isBlank(text)) {
            return PhoneNumber.absent();
        }
        try {
            return Validate.notNull(parser.parse(text));
        }
        catch (PhoneNumber.ParseException ex) {
            throw new JsonParseException(ex);
        }
    }

}
