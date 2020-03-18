package sample;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface PhoneNumberParser {

    @Nonnull
    PhoneNumber parse(@Nullable String text) throws PhoneNumber.ParseException;

}
