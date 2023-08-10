package dev.shadowsoffire.apotheosis.adventure.spawner;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.placebo.json.NBTAdapter;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.SpawnData;

public class RogueSpawnerRegistry extends WeightedDynamicRegistry<RogueSpawner> {

    public static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(new TypeToken<SimpleWeightedRandomList<SpawnData>>(){}.getType(), new SpawnDataListAdapter())
        .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
        .registerTypeAdapter(CompoundTag.class, NBTAdapter.INSTANCE)
        .create();

    public static final RogueSpawnerRegistry INSTANCE = new RogueSpawnerRegistry();

    public RogueSpawnerRegistry() {
        super(AdventureModule.LOGGER, "rogue_spawners", false, false);
    }

    @Override
    protected void registerBuiltinSerializers() {
        this.registerSerializer(DEFAULT, RogueSpawner.SERIALIZER);
    }

    private static class SpawnDataListAdapter implements JsonDeserializer<SimpleWeightedRandomList<SpawnData>>, JsonSerializer<SimpleWeightedRandomList<SpawnData>> {

        @Override
        public SimpleWeightedRandomList<SpawnData> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return SpawnData.LIST_CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, s -> {
                throw new RuntimeException("Failed to parse SpawnDataList " + s);
            });
        }

        @Override
        public JsonElement serialize(SimpleWeightedRandomList<SpawnData> src, Type typeOfSrc, JsonSerializationContext context) {
            return SpawnData.LIST_CODEC.encodeStart(JsonOps.INSTANCE, src).getOrThrow(false, s -> {
                throw new RuntimeException("Failed to encode SpawnListData " + s);
            });
        }

    }

}
