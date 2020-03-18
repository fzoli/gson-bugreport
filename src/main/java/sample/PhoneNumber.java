package sample;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Optional;

/**
 * International phone number.
 * This is a data type like boolean, number, OffsetDateTime!
 * It works like {@code java.util.Optional}, so first check whether it's present then you can get the data.
 */
public final class PhoneNumber {

    public interface Data extends Serializable {

        /**
         * @return country based on the calling code in the phone number (as we know)
         */
        @Nonnull
        Country getCountry();

        /**
         * @return true if the phone number is valid in it's country (as we know)
         */
        boolean isValidNumber();

        /**
         * @return E164 encoded string
         */
        @Nonnull
        String toIsoString();

        /**
         * @return human-readable international string
         */
        @Nonnull
        String toReadableString();

    }

    @Builder
    @Getter
    @ToString
    public static final class Country {

        /**
         * 2-character ISO country code or empty string if not known
         */
        @Nonnull
        private final String isoCode;

        /**
         * The calling code in the phone number
         */
        private final int callingCode;

    }

    private interface Strategy extends Serializable {

        /**
         * @return true if data present and safe to call {@link #get}
         */
        boolean isPresent();

        /**
         * @return the data (if present)
         * @throws NullPointerException if data is not present
         * @see #isPresent
         */
        @Nonnull
        Data get();

    }

    /**
     * Parse the optional string as (inter)national phone number.
     * Usage: import from XLS, CSV or other external system
     * @param numberText complete phone number (can be international or national)
     * @param countryCode 2-character ISO country code (like US, HU)
     * @return absent if text is empty
     * @throws ParseException if failed to parse the text
     */
    @Nonnull
    public static PhoneNumber parseNational(
            @Nullable String numberText, @Nonnull String countryCode) throws ParseException {
        Validate.notEmpty(countryCode, "Country code can not be empty"); // validate always
        if (StringUtils.isEmpty(numberText)) {
            return new PhoneNumber(numberText, AbsentStrategy.INSTANCE);
        } else {
            return new PhoneNumber(numberText, new PresentStrategy(new ParsedNumber(numberText, countryCode)));
        }
    }

    /**
     * Parse the optional string as international phone number.
     * @return absent if text is empty
     * @throws ParseException if failed to parse the text
     */
    @Nonnull
    public static PhoneNumber parseOptional(@Nullable String numberText) throws ParseException {
        // Please, do not add region code as argument.
        // It's international number only! So 1234 is not a PhoneNumber, just a string.
        if (StringUtils.isEmpty(numberText)) {
            return new PhoneNumber(numberText, AbsentStrategy.INSTANCE);
        } else {
            return new PhoneNumber(numberText, new PresentStrategy(numberText));
        }
    }

    /**
     * Parse the required string as international phone number.
     * @return the phone number
     * @throws ParseException if failed to parse the text
     * @throws NullPointerException if text is empty
     */
    @Nonnull
    public static PhoneNumber parseRequired(@Nonnull String numberText) throws ParseException {
        // Please, do not add region code as argument.
        // It's international number only! So 1234 is not a PhoneNumber, just a string.
        if (StringUtils.isEmpty(numberText)) {
            throw new NullPointerException("Phone number is required");
        } else {
            return new PhoneNumber(numberText, new PresentStrategy(numberText));
        }
    }

    /**
     * Try to parse the string as international phone number.
     * Server usage: from database; to server response
     * Client usage: from server response
     * @return absent if text is empty or failed to parse the text
     */
    @Nonnull
    public static PhoneNumber raw(@Nullable String numberText) {
        // Please, do not add region code as argument.
        // It's international number only! So 1234 is not a PhoneNumber, just a string.
        if (StringUtils.isEmpty(numberText)) {
            return new PhoneNumber(numberText, AbsentStrategy.INSTANCE);
        } else {
            try {
                return new PhoneNumber(numberText, new PresentStrategy(numberText));
            } catch (ParseException ex) {
                return new PhoneNumber(numberText, AbsentStrategy.INSTANCE);
            }
        }
    }

    /**
     * @return absent phone number
     */
    @Nonnull
    public static PhoneNumber absent() {
        return new PhoneNumber(null, AbsentStrategy.INSTANCE);
    }

    /**
     * @param phoneNumber the provided phone number
     * @return an absent phone number if the provided one is null
     */
    @Nonnull
    public static PhoneNumber ofNullable(@Nullable PhoneNumber phoneNumber) {
        if (phoneNumber == null) {
            return absent();
        }
        return phoneNumber;
    }

    @Nonnull
    private final String rawText;

    @Nonnull
    private final Strategy delegate;

    private PhoneNumber(@Nullable String rawText, @Nonnull Strategy delegate) {
        this.rawText = StringUtils.isEmpty(rawText) ? "" : rawText;
        this.delegate = delegate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
            return false;
        }
        PhoneNumber rhs = (PhoneNumber) obj;
        return delegate.equals(rhs.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * Only for developers and debuggers.
     * @return string that informs the developer whether is the data present and what is it
     */
    @Override
    public String toString() {
        if (!delegate.isPresent()) {
            return String.format("PhoneNumber(rawText='%s')", rawText);
        }
        String isoString = delegate.get().toIsoString();
        Country country = delegate.get().getCountry();
        return String.format("PhoneNumber(rawText='%s', isoString='%s', country='%s')", rawText, isoString, country);
    }

    /**
     * @return the original raw string or empty text if null
     */
    @Nonnull
    public String toRawString() {
        return rawText;
    }

    /**
     * @return the data (if present)
     * @throws NullPointerException if data is not present
     * @see #isPresent
     */
    @Nonnull
    public Data get() {
        return delegate.get();
    }

    /**
     * @return true if the raw data is blank
     */
    public boolean hasEmptyRaw() {
        return StringUtils.isBlank(toRawString());
    }

    /**
     * @return true if the raw data is not blank
     */
    public boolean hasRaw() {
        return !hasEmptyRaw();
    }

    /**
     * @return true if the raw data is not blank and the data could be parsed
     */
    public boolean hasPresentRaw() {
        return hasRaw() && isPresent();
    }

    /**
     * @return true if the raw data is not blank and the data could not be parsed
     */
    public boolean hasAbsentRaw() {
        return hasRaw() && isAbsent();
    }

    /**
     * @return true if data is present and safe to call {@link #get}
     */
    public boolean isPresent() {
        return delegate.isPresent();
    }

    /**
     * @return true if data is present and safe to call {@link #get}
     */
    public static boolean isPresent(@Nullable PhoneNumber phoneNumber) {
        return phoneNumber != null && phoneNumber.isPresent();
    }

    /**
     * @return true if data is not present and invocation of {@link #get} throws a {@link NullPointerException}
     */
    public boolean isAbsent() {
        return !isPresent();
    }

    /**
     * @return true if data is not present and invocation of {@link #get} throws a {@link NullPointerException}
     */
    public static boolean isAbsent(@Nullable PhoneNumber phoneNumber) {
        return phoneNumber == null || phoneNumber.isAbsent();
    }

    /**
     * @return the ISO string or null
     */
    @Nullable
    public static String toIsoString(@Nullable PhoneNumber phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        if (phoneNumber.isPresent()) {
            return phoneNumber.get().toIsoString();
        }
        return null;
    }

    /**
     * @return the non-empty raw string or null
     */
    @Nullable
    public static String toNonEmptyRawString(@Nullable PhoneNumber phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        if (phoneNumber.hasEmptyRaw()) {
            return null;
        }
        return phoneNumber.toRawString();
    }

    /**
     * Use it only for readonly features, because it can return invalid raw data too!
     * @return the readable string (valid international or invalid raw) or empty string
     */
    public static String toReadableString(@Nullable PhoneNumber phoneNumber) {
        if (PhoneNumber.isPresent(phoneNumber)) {
            return phoneNumber.get().toReadableString();
        } else {
            String rawString = PhoneNumber.toNonEmptyRawString(phoneNumber);
            if (rawString == null) {
                return "";
            }
            return rawString;
        }
    }

    /**
     * @return the parsed data or empty optional
     */
    @Nonnull
    public static Optional<Data> optGet(@Nullable PhoneNumber phoneNumber) {
        if (phoneNumber == null) {
            return Optional.empty();
        }
        if (phoneNumber.isAbsent()) {
            return Optional.empty();
        }
        return Optional.of(phoneNumber.get());
    }

    private static class AbsentStrategy implements Strategy {

        private static final Strategy INSTANCE = new AbsentStrategy();

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        @Nonnull
        public Data get() {
            throw new NullPointerException("No data present");
        }

    }

    private static class PresentStrategy implements Strategy {

        private final ParsedNumber number;

        public PresentStrategy(@Nonnull String numberText) throws ParseException {
            this.number = new ParsedNumber(numberText);
        }

        public PresentStrategy(@Nonnull ParsedNumber number) {
            this.number = Validate.notNull(number, "null number");
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) { return false; }
            if (obj == this) { return true; }
            if (obj.getClass() != getClass()) {
                return false;
            }
            PresentStrategy rhs = (PresentStrategy) obj;
            return number.equals(rhs.number);
        }

        @Override
        public int hashCode() {
            return number.hashCode();
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        @Nonnull
        public Data get() {
            return number;
        }

    }

    private static class ParsedNumber implements Data {

        private static final PhoneNumberUtil util = PhoneNumberUtil.getInstance();

        private final Phonenumber.PhoneNumber internal;
        private final Country country;

        public ParsedNumber(@Nonnull String numberText) throws ParseException {
            checkCountryCodeInNumber(numberText);
            this.internal = parse(numberText);
            this.country = createCountry(internal);
        }

        public ParsedNumber(@Nonnull String numberText, @Nonnull String countryCode) throws ParseException {
            this.internal = parse(numberText, countryCode);
            this.country = createCountry(internal);
        }

        private static void checkCountryCodeInNumber(@Nonnull String numberText) throws ParseException {
            if (!numberText.startsWith("+") && !numberText.startsWith("00")) {
                throw new ParseException(
                        ParseException.ErrorType.MISSING_COUNTRY_CODE, "Missing country code");
            }
        }

        @Nonnull
        private static Country createCountry(Phonenumber.PhoneNumber number) {
            String isoCode = util.getRegionCodeForCountryCode(number.getCountryCode());
            isoCode = isoCode == null || isoCode.equals("ZZ") ? "" : isoCode;
            return Country.builder()
                    .callingCode(number.getCountryCode())
                    .isoCode(isoCode)
                    .build();
        }

        @Nonnull
        private Phonenumber.PhoneNumber parse(@Nonnull String numberText) throws ParseException {
            try {
                return util.parse(numberText, null);
            } catch (NumberParseException ex) {
                throw fromLibError(ex);
            }
        }

        @Nonnull
        private Phonenumber.PhoneNumber parse(@Nonnull String numberText, @Nonnull String countryCode) throws ParseException {
            try {
                return util.parse(numberText, countryCode);
            } catch (NumberParseException ex) {
                throw fromLibError(ex);
            }
        }

        private ParseException fromLibError(NumberParseException ex) {
            return new ParseException(fromLibErrorType(ex.getErrorType()), ex);
        }

        private ParseException.ErrorType fromLibErrorType(@Nullable NumberParseException.ErrorType type) {
            if (type != null) {
                switch (type) {
                    case INVALID_COUNTRY_CODE:
                        return ParseException.ErrorType.INVALID_COUNTRY_CODE;
                    case NOT_A_NUMBER:
                        return ParseException.ErrorType.NOT_A_NUMBER;
                    case TOO_SHORT_AFTER_IDD:
                        return ParseException.ErrorType.TOO_SHORT_AFTER_IDD;
                    case TOO_SHORT_NSN:
                        return ParseException.ErrorType.TOO_SHORT_NSN;
                    case TOO_LONG:
                        return ParseException.ErrorType.TOO_LONG;
                    default:
                        return ParseException.ErrorType.GENERAL;
                }
            }
            return ParseException.ErrorType.GENERAL;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) { return false; }
            if (obj == this) { return true; }
            if (obj.getClass() != getClass()) {
                return false;
            }
            ParsedNumber rhs = (ParsedNumber) obj;
            return internal.equals(rhs.internal);
        }

        @Override
        public int hashCode() {
            return internal.hashCode();
        }

        @Override
        public boolean isValidNumber() {
            return util.isValidNumber(internal);
        }

        @Nonnull
        @Override
        public Country getCountry() {
            return country;
        }

        @Override
        @Nonnull
        public String toIsoString() {
            return format(PhoneNumberUtil.PhoneNumberFormat.E164);
        }

        @Override
        @Nonnull
        public String toReadableString() {
            return format(PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        }

        @Nonnull
        private String format(@Nonnull PhoneNumberUtil.PhoneNumberFormat format) {
            return util.format(internal, format);
        }

    }

    public static class ParseException extends Exception {

        public enum ErrorType {

            GENERAL,

            MISSING_COUNTRY_CODE,

            INVALID_COUNTRY_CODE,
            NOT_A_NUMBER,
            TOO_SHORT_AFTER_IDD,
            TOO_SHORT_NSN,
            TOO_LONG,
        }

        @Nonnull
        @Getter
        private final ErrorType errorType;

        public ParseException(ErrorType errorType, String message) {
            super(message);
            this.errorType = errorType;
        }

        public ParseException(ErrorType errorType, Exception ex) {
            super(ex);
            this.errorType = errorType;
        }

    }

}
