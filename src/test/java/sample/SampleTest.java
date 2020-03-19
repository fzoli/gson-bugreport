package sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class SampleTest {

    @Parameterized.Parameters(name = "withFactory({0})")
    public static Object[] data() {
        return new Object[] {
                true, // test with PhoneNumberTypeAdapterFactory
                false // test with PhoneNumberTypeAdapter
        };
    }

    private final boolean withFactory;

    public SampleTest(boolean withFactory) {
        this.withFactory = withFactory;
    }

    @Nonnull
    private GsonBuilder gsonBuilder() {
        PhoneNumberParser phoneNumberParser = DefaultPhoneNumberParser.INSTANCE;
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new OptionalTypeTypeAdapterFactory(DefaultValueFactoryContainer.builder()
                .register(PhoneNumber.class, PhoneNumber::absent)
                .build()));
        if (withFactory) {
            // NOTE:
            // OptionalTypeTypeAdapterFactory must be registered before PhoneNumberTypeAdapterFactory.
            builder.registerTypeAdapterFactory(new PhoneNumberTypeAdapterFactory(phoneNumberParser));
        }
        builder.registerTypeAdapterFactory(new ValidatingTypeAdapterFactory());
        if (!withFactory) {
            // NOTE:
            // It fails in case of primitive types (standalone or in collection).
            builder.registerTypeAdapter(PhoneNumber.class, new PhoneNumberTypeAdapter(phoneNumberParser));
        }
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

    @Test
    public void serializeArrayWithPhoneNumbers() {
        Gson gson = gsonBuilder().create();
        String expectedJson = "[\"+36201234567\",\"+36301234567\"]";
        List<PhoneNumber> request = Arrays.asList(PhoneNumber.raw("+36201234567"), PhoneNumber.raw("+36301234567"));
        String json = gson.toJson(request);
        Assert.assertEquals(expectedJson, json);
    }

    @Test
    public void parseArrayWithPhoneNumbers() {
        Gson gson = gsonBuilder().create();
        String requestJson = "[\"+36201234567\",\"+36301234567\"]";
        List<PhoneNumber> expectedRequest = Arrays.asList(PhoneNumber.raw("+36201234567"), PhoneNumber.raw("+36301234567"));
        Type listType = new TypeToken<List<PhoneNumber>>() {}.getType();
        List<PhoneNumber> request = gson.fromJson(requestJson, listType);
        Assert.assertEquals(expectedRequest, request);
    }

    @Test
    public void serializeArrayWithAbsentPhoneNumber() {
        Gson gson = gsonBuilder().create();
        String expectedJson = "[null,\"+36301234567\"]";
        List<PhoneNumber> request = Arrays.asList(PhoneNumber.absent(), PhoneNumber.raw("+36301234567"));
        String json = gson.toJson(request);
        Assert.assertEquals(expectedJson, json);
    }

    @Test
    public void parseArrayWithNullPhoneNumber() {
        Gson gson = gsonBuilder().create();
        String requestJson = "[null,\"+36301234567\"]";
        List<PhoneNumber> expectedRequest = Arrays.asList(PhoneNumber.absent(), PhoneNumber.raw("+36301234567"));
        Type listType = new TypeToken<List<PhoneNumber>>() {}.getType();
        List<PhoneNumber> request = gson.fromJson(requestJson, listType);
        Assert.assertEquals(expectedRequest, request);
    }

    @Test
    public void parseArrayWithEmptyPhoneNumber() {
        Gson gson = gsonBuilder().create();
        String requestJson = "[\"\",\"+36301234567\"]";
        List<PhoneNumber> expectedRequest = Arrays.asList(PhoneNumber.absent(), PhoneNumber.raw("+36301234567"));
        Type listType = new TypeToken<List<PhoneNumber>>() {}.getType();
        List<PhoneNumber> request = gson.fromJson(requestJson, listType);
        Assert.assertEquals(expectedRequest, request);
    }

    @Test
    public void serializeAbsentPhoneNumber() {
        Gson gson = gsonBuilder().create();
        String expectedJson = "null";
        PhoneNumber request = PhoneNumber.absent();
        String json = gson.toJson(request);
        Assert.assertEquals(expectedJson, json);
    }

    @Test
    public void parseEmptyPhoneNumber() {
        Gson gson = gsonBuilder().create();
        String requestJson = "\"\"";
        PhoneNumber expectedRequest = PhoneNumber.absent();
        PhoneNumber request = gson.fromJson(requestJson, PhoneNumber.class);
        Assert.assertEquals(expectedRequest, request);
    }

    @Test
    public void parseNullPhoneNumber() {
        Gson gson = gsonBuilder().create();
        String requestJson = "null";
        PhoneNumber expectedRequest = PhoneNumber.absent();
        PhoneNumber request = gson.fromJson(requestJson, PhoneNumber.class);
        Assert.assertEquals(expectedRequest, request);
    }

}
