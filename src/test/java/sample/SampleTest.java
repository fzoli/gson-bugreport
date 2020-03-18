package sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;

public class SampleTest {

    @Nonnull
    private GsonBuilder gsonBuilder() {
        PhoneNumberParser parser = DefaultPhoneNumberParser.INSTANCE;
        DefaultValueFactoryContainer defaultValueFactoryContainer = DefaultValueFactoryContainer.builder()
                .register(PhoneNumber.class, PhoneNumber::absent)
                .build();
        GsonBuilder builder = new GsonBuilder();
        // NOTE: OptionalTypeTypeAdapterFactory must be registered before PhoneNumberTypeAdapterFactory.
        builder.registerTypeAdapterFactory(new OptionalTypeTypeAdapterFactory(defaultValueFactoryContainer));
        builder.registerTypeAdapterFactory(new PhoneNumberTypeAdapterFactory(parser));
        builder.registerTypeAdapterFactory(new ValidatingTypeAdapterFactory());
        return builder;
    }

    @Test
    public void serializeSampleDataWithPhoneNumber() {
        Gson gson = gsonBuilder().create();
        String expectedJson = "{\"name\":\"name\",\"phone_number\":\"+36301234567\"}";
        SampleData request = SampleData.builder()
                .name("name")
                .phoneNumber(PhoneNumber.raw("+36301234567"))
                .build();
        String json = gson.toJson(request);
        Assert.assertEquals(expectedJson, json);
    }

    @Test
    public void serializeSampleDataWithoutPhoneNumberWithoutNulls() {
        Gson gson = gsonBuilder().create();
        String expectedJson = "{\"name\":\"name\"}";
        SampleData request = SampleData.builder()
                .name("name")
                .phoneNumber(PhoneNumber.absent())
                .build();
        String json = gson.toJson(request);
        Assert.assertEquals(expectedJson, json);
    }

    @Test
    public void serializeSampleDataWithoutPhoneNumberWithNulls() {
        Gson gson = gsonBuilder().serializeNulls().create();
        String expectedJson = "{\"name\":\"name\",\"phone_number\":null}";
        SampleData request = SampleData.builder()
                .name("name")
                .phoneNumber(PhoneNumber.absent())
                .build();
        String json = gson.toJson(request);
        Assert.assertEquals(expectedJson, json);
    }

    @Test
    public void parseSampleDataWithPhoneNumber() {
        Gson gson = gsonBuilder().create();
        String requestJson = "{\"name\":\"name\",\"phone_number\":\"+36301234567\"}";
        SampleData expectedRequest = SampleData.builder()
                .name("name")
                .phoneNumber(PhoneNumber.raw("+36301234567"))
                .build();
        SampleData request = gson.fromJson(requestJson, SampleData.class);
        Assert.assertEquals(expectedRequest, request);
    }

    @Test
    public void parseSampleDataWithoutPhoneNumber() {
        Gson gson = gsonBuilder().create();
        String requestJson = "{\"name\":\"name\"}";
        SampleData expectedRequest = SampleData.builder()
                .name("name")
                .phoneNumber(PhoneNumber.absent())
                .build();
        SampleData request = gson.fromJson(requestJson, SampleData.class);
        Assert.assertEquals(expectedRequest, request);
    }

}
