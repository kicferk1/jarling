package org.jarling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jarling.services.ApiService;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Base service to provide some convenience functions and utilities to all service classes
 *
 * @author Nav Roudsari (nav@rzari.co.uk)
 */
public abstract class StarlingBase {

    protected static ApiService apiService;

    public StarlingBase(StarlingBankApiVersion apiVersion, StarlingBankEnvironment environment, String accessToken) {
        if (accessToken == null || accessToken.equals("")) {
            throw new IllegalArgumentException("access token cannot be null or blank");
        } else {
            apiService = new ApiService(apiVersion, environment, accessToken);
        }
    }

    protected final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
                @Override
                public void write(final JsonWriter jsonWriter, final LocalDate localDate) throws IOException {
                    jsonWriter.value(localDate.toString());
                }

                @Override
                public LocalDate read(final JsonReader jsonReader) throws IOException {
                    return LocalDate.parse(jsonReader.nextString());
                }
            }.nullSafe())
            .create();
    static final DateFormat transactionDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private JsonObject toJsonObject(String json) {
        return gson.fromJson(json, JsonObject.class);
    }

    private String getJsonArray(String json, String memberName) {
        JsonObject jsonObject = toJsonObject(json);
        String result;
        if (jsonObject.has("_embedded")) {
            result = jsonObject.get("_embedded").getAsJsonObject().get(memberName).toString();
        } else {
            result = jsonObject.get(memberName).toString();
        }
        return result;
    }

    // https://stackoverflow.com/questions/14139437/java-type-generic-as-argument-for-gson
    final <T> List<T> fromJsonList(final Class<T[]> clazz, String json, String memberName) {
        return Arrays.asList(gson.fromJson(getJsonArray(json, memberName), clazz));
    }

    final <T> T fromJson(String json, final Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
}
