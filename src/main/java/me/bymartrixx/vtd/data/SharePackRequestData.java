package me.bymartrixx.vtd.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class SharePackRequestData {
    private final String type;
    private final String version;
    private final Map<String, List<String>> selectedPacks;

    public SharePackRequestData(String type, String version, Map<String, List<String>> selectedPacks) {
        this.type = type;
        this.version = version;
        this.selectedPacks = selectedPacks;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof SharePackRequestData data) {
            return this.type.equals(data.type) && this.version.equals(data.version)
                    && this.selectedPacks == data.selectedPacks;
        }

        return false;
    }

    public static class Serializer implements JsonSerializer<SharePackRequestData> {
        @Override
        public JsonElement serialize(SharePackRequestData data, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.add("type", context.serialize(data.type));
            obj.add("version", context.serialize(data.version));
            JsonElement selected = context.serialize(data.selectedPacks);
            obj.add("selected", context.serialize(selected.toString(), String.class));
            return obj;
        }
    }
}
