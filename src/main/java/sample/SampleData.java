package sample;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

@Builder
@Getter
@ToString
@EqualsAndHashCode
public class SampleData implements Validatable {

    @Nonnull
    private final String name;

    @Nonnull
    @OptionalType
    @SerializedName("phone_number")
    private final PhoneNumber phoneNumber;

    @Override
    public void validate() {
        Validate.notBlank(name, "Name is required");
        Validate.isTrue(!phoneNumber.hasAbsentRaw(), "Invalid phone number");
    }

}
