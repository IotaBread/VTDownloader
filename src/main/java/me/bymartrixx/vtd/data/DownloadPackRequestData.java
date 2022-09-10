package me.bymartrixx.vtd.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.jetbrains.annotations.Contract;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DownloadPackRequestData {
    private final Map<String, List<String>> packs;

    private DownloadPackRequestData(Map<String, List<String>> packs) {
        this.packs = packs;
    }

    @Contract("_ -> new")
    public static DownloadPackRequestData create(Map<Category, List<Pack>> selectedPacks) {
        Map<String, List<String>> packs = selectedPacks.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey().getName(),
                entry -> entry.getValue().stream().map(Pack::getId).toList()
        ));
        return new DownloadPackRequestData(packs);
    }

    public static class Serializer implements JsonSerializer<DownloadPackRequestData> {
        @Override
        public JsonElement serialize(DownloadPackRequestData downloadPackData, Type type, JsonSerializationContext jsonSerializationContext) {
            return jsonSerializationContext.serialize(downloadPackData.packs);
        }
    }
}
