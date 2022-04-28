package shadows.apotheosis.deadly.reload;

import java.lang.reflect.Type;
import java.util.*;

import javax.annotation.Nullable;

import com.google.gson.*;

import joptsimple.internal.Strings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.level.SpawnData;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.gen.SpawnerItem;
import shadows.apotheosis.util.SpawnerStats;
import shadows.placebo.json.NBTAdapter;
import shadows.placebo.json.PlaceboJsonReloadListener;
import shadows.placebo.json.SerializerBuilder;

public class RandomSpawnerManager extends PlaceboJsonReloadListener<SpawnerItem> {

    //Formatter::off
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(CompoundTag.class, NBTAdapter.INSTANCE)
            .create();
    //Formatter::on

    public static final RandomSpawnerManager INSTANCE = new RandomSpawnerManager();

    private final List<SpawnerItem> entries = new ArrayList<>();
//    private final Map<ResourceLocation, SpawnerItem> registry = new HashMap<>();
    private volatile int weight = 0;

    public RandomSpawnerManager() {
        super(DeadlyModule.LOGGER, "random_spawners", false, false);
    }

    protected void register(ResourceLocation id, SpawnerItem item) {
        if (!this.registry.containsKey(id)) {
            this.registry.put(id, item);
            this.entries.add(item);
        } else DeadlyModule.LOGGER.error("Attempted to register a spawner item with name {}, but it already exists!", id);
    }

    @Override
    protected void onReload() {
        super.onReload();
        INSTANCE.weight = WeightedRandom.getTotalWeight(entries);
        if(entries.size() == 0) this.logger.error("No SpawnerItems registered!");
    }

    public Optional<SpawnerItem> getRandomItem(Random rand) {
        return WeightedRandom.getRandomItem(rand, this.entries, this.weight);
    }

    @Nullable
    public SpawnerItem getById(ResourceLocation id) {
        return this.registry.get(id);
    }

    @Override
    protected void registerBuiltinSerializers() {
        this.registerSerializer(DEFAULT,
                new SerializerBuilder<SpawnerItem>("SpawnerItem")
                        .withJsonDeserializer(jo -> {
                            this.logger.info("Parsing SpawnerInfo...{}", jo.toString());

                            try {
                                var stats = jo.has("stats") ? GSON.fromJson(jo.get("stats"), SpawnerStats.class) : new SpawnerStats();
                                var lootTable = GSON.fromJson(jo.get("loot_table"), ResourceLocation.class);
                                var weight = jo.get("weight").getAsInt();
                                List<WeightedEntry.Wrapper<SpawnData>> spawnPotentials = new ArrayList<>();
                                var potentials = jo.get("spawn_potentials").getAsJsonArray();
                                potentials.forEach(el -> {
                                    var dataObj = el.getAsJsonObject();
                                    String id = dataObj.get("entity").getAsString();
                                    if (Strings.isNullOrEmpty(id)) throw new JsonParseException("SpawnData missing \"entity\" attribute!");
                                    int weight1 = dataObj.get("weight").getAsInt();
                                    CompoundTag nbt = new CompoundTag();
                                    if(dataObj.has("nbt"))
                                        nbt = GSON.fromJson(dataObj.get("nbt"), CompoundTag.class);
                                    nbt.putString("id", id);
                                    spawnPotentials.add(WeightedEntry.wrap(new SpawnData(nbt, Optional.empty()), weight1));
                                });
                                return new SpawnerItem(stats, lootTable, spawnPotentials, weight);
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw e;
                            }
                        }));
    }
}

