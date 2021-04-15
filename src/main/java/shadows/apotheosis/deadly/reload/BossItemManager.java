package shadows.apotheosis.deadly.reload;

import java.util.ArrayList;
import java.util.Collections;
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
import net.minecraft.entity.EntityType;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.coremod.api.ASMAPI;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.gen.BossItem;
import shadows.apotheosis.util.AxisAlignedBBDeserializer;
import shadows.apotheosis.util.ChancedEffectInstance;
import shadows.apotheosis.util.EntityTypeDeserializer;
import shadows.apotheosis.util.GearSet.SetPredicate;
import shadows.apotheosis.util.GearSet.SetPredicateAdapter;
import shadows.apotheosis.util.RandomAttributeModifier;

public class BossItemManager extends JsonReloadListener {

	//Formatter::off
	public static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(EntityType.class, new EntityTypeDeserializer())
			.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.registerTypeAdapter(SetPredicate.class, new SetPredicateAdapter())
			.setFieldNamingStrategy(f -> f.getName().equals(ASMAPI.mapField("field_76292_a")) ? "weight" : f.getName())
			.registerTypeAdapter(RandomValueRange.class, new RandomValueRange.Serializer())
			.registerTypeAdapter(ChancedEffectInstance.class, new ChancedEffectInstance.Deserializer())
			.registerTypeAdapter(RandomAttributeModifier.class, new RandomAttributeModifier.Deserializer())
			.registerTypeAdapter(AxisAlignedBB.class, new AxisAlignedBBDeserializer()).create();
	//Formatter::on

	public static final BossItemManager INSTANCE = new BossItemManager();

	private final List<BossItem> entries = new ArrayList<>();
	private final Map<ResourceLocation, BossItem> registry = new HashMap<>();
	private int weight = 0;

	public BossItemManager() {
		super(GSON, "bosses");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objects, IResourceManager resourceManagerIn, IProfiler profilerIn) {
		this.entries.clear();
		this.registry.clear();
		for (Entry<ResourceLocation, JsonElement> obj : objects.entrySet()) {
			try {
				this.register(obj.getKey(), GSON.fromJson(obj.getValue(), BossItem.class));
			} catch (Exception e) {
				DeadlyModule.LOGGER.error("Failed to load boss item {}.", obj.getKey());
				e.printStackTrace();
			}
		}
		if (this.entries.size() == 0) throw new RuntimeException("No Bosses were registered.  This is not supported.");
		Collections.shuffle(this.entries);
		this.weight = WeightedRandom.getTotalWeight(this.entries);
		DeadlyModule.LOGGER.info("Loaded {} boss items from resources.", this.entries.size());
	}

	protected void register(ResourceLocation id, BossItem item) {
		if (!this.registry.containsKey(id)) {
			item.setId(id);
			this.registry.put(id, item);
			this.entries.add(item);
		} else DeadlyModule.LOGGER.error("Attempted to register a boss item with name {}, but it already exists!", id);
	}

	public BossItem getRandomItem(Random rand) {
		return WeightedRandom.getRandomItem(rand, this.entries, this.weight);
	}

	@Nullable
	public BossItem getById(ResourceLocation id) {
		return this.registry.get(id);
	}

}
