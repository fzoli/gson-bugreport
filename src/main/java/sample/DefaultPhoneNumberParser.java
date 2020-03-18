package sample;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DefaultPhoneNumberParser implements PhoneNumberParser {

    public static final PhoneNumberParser INSTANCE = new DefaultPhoneNumberParser();

    private DefaultPhoneNumberParser() {
        // singleton
    }

    @Nonnull
    @Override
    public PhoneNumber parse(@Nullable String text) throws PhoneNumber.ParseException {
        // parse it as E-164 phone number; reject invalid ones
        return PhoneNumber.parseOptional(text);
    }

}
