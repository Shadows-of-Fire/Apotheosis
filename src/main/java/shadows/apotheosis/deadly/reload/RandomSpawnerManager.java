package shadows.apotheosis.deadly.reload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.level.SpawnData;
import net.minecraftforge.coremod.api.ASMAPI;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.gen.SpawnerItem;
import shadows.apotheosis.util.JsonUtil;
import shadows.apotheosis.util.WeightedSpawnerEntityAdapter;
import shadows.placebo.json.NBTAdapter;

public class RandomSpawnerManager extends SimpleJsonResourceReloadListener {

	//Formatter::off
	public static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(SpawnData.class, new WeightedSpawnerEntityAdapter())
			.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.setFieldNamingStrategy(f -> f.getName().equals(ASMAPI.mapField("field_76292_a")) ? "weight" : f.getName())
			.registerTypeAdapter(CompoundTag.class, NBTAdapter.INSTANCE)
			.create();
	//Formatter::on

	public static final RandomSpawnerManager INSTANCE = new RandomSpawnerManager();

	private final List<SpawnerItem> entries = new ArrayList<>();
	private final Map<ResourceLocation, SpawnerItem> registry = new HashMap<>();
	private volatile int weight = 0;

	public RandomSpawnerManager() {
		super(GSON, "random_spawners");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
		this.entries.clear();
		this.registry.clear();
		for (Entry<ResourceLocation, JsonElement> obj : objects.entrySet()) {
			try {
				if (JsonUtil.checkAndLogEmpty(obj.getValue(), obj.getKey(), "Random Spawner", DeadlyModule.LOGGER)) continue;
				this.register(obj.getKey(), GSON.fromJson(obj.getValue(), SpawnerItem.class));
			} catch (Exception e) {
				DeadlyModule.LOGGER.error("Failed to load spawner item {}.", obj.getKey());
				e.printStackTrace();
			}
		}
		if (this.entries.size() == 0) throw new RuntimeException("No Random Spawners were registered.  This is not supported.");
		this.weight = WeightedRandom.getTotalWeight(this.entries);
		if (this.weight == 0) throw new RuntimeException("The total spawner weight is zero.  This is not supported.");
		DeadlyModule.LOGGER.info("Loaded {} spawner items from resources.", this.entries.size());
	}

	protected void register(ResourceLocation id, SpawnerItem item) {
		if (!this.registry.containsKey(id)) {
			this.registry.put(id, item);
			this.entries.add(item);
		} else DeadlyModule.LOGGER.error("Attempted to register a spawner item with name {}, but it already exists!", id);
	}

	public SpawnerItem getRandomItem(Random rand) {
		return WeightedRandom.getRandomItem(rand, this.entries, this.weight).get();
	}

	@Nullable
	public SpawnerItem getById(ResourceLocation id) {
		return this.registry.get(id);
	}

}
