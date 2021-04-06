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

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraftforge.coremod.api.ASMAPI;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.gen.SpawnerItem;
import shadows.apotheosis.util.WeightedSpawnerEntityAdapter;
import shadows.placebo.util.json.NBTAdapter;

public class RandomSpawnerManager extends JsonReloadListener {

	//Formatter::off
	public static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(WeightedSpawnerEntity.class, new WeightedSpawnerEntityAdapter())
			.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.setFieldNamingStrategy(f -> f.getName().equals(ASMAPI.mapField("field_76292_a")) ? "weight" : f.getName())
			.registerTypeAdapter(CompoundNBT.class, NBTAdapter.INSTANCE)
			.create();
	//Formatter::on

	public static final RandomSpawnerManager INSTANCE = new RandomSpawnerManager();

	private final List<SpawnerItem> entries = new ArrayList<>();
	private final Map<ResourceLocation, SpawnerItem> registry = new HashMap<>();
	private int weight = 0;

	public RandomSpawnerManager() {
		super(GSON, "random_spawners");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objects, IResourceManager resourceManagerIn, IProfiler profilerIn) {
		this.entries.clear();
		this.registry.clear();
		for (Entry<ResourceLocation, JsonElement> obj : objects.entrySet()) {
			try {
				this.register(obj.getKey(), GSON.fromJson(obj.getValue(), SpawnerItem.class));
			} catch (Exception e) {
				DeadlyModule.LOGGER.error("Failed to load spawner item {}.", obj.getKey());
				e.printStackTrace();
			}
		}
		if (this.entries.size() == 0) throw new RuntimeException("No Random Spawners were registered.  This is not supported.");
		this.weight = WeightedRandom.getTotalWeight(this.entries);
		DeadlyModule.LOGGER.info("Loaded {} spawner items from resources.", this.entries.size());
	}

	protected void register(ResourceLocation id, SpawnerItem item) {
		if (!this.registry.containsKey(id)) {
			this.registry.put(id, item);
			this.entries.add(item);
		} else DeadlyModule.LOGGER.error("Attempted to register a spawner item with name {}, but it already exists!", id);
	}

	public SpawnerItem getRandomItem(Random rand) {
		return WeightedRandom.getRandomItem(rand, this.entries, this.weight);
	}

	@Nullable
	public SpawnerItem getById(ResourceLocation id) {
		return this.registry.get(id);
	}

}
