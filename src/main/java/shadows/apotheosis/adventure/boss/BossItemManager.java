package shadows.apotheosis.adventure.boss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.coremod.api.ASMAPI;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.util.AxisAlignedBBDeserializer;
import shadows.apotheosis.util.ChancedEffectInstance;
import shadows.apotheosis.util.EntityTypeDeserializer;
import shadows.apotheosis.util.GearSet.SetPredicate;
import shadows.apotheosis.util.GearSet.SetPredicateAdapter;
import shadows.placebo.json.NBTAdapter;
import shadows.placebo.json.PlaceboJsonReloadListener;
import shadows.placebo.json.RandomAttributeModifier;
import shadows.placebo.json.SerializerBuilder;

public class BossItemManager extends PlaceboJsonReloadListener<BossItem> {

	//Formatter::off
	public static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(EntityType.class, new EntityTypeDeserializer())
			.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.registerTypeAdapter(SetPredicate.class, new SetPredicateAdapter())
			.setFieldNamingStrategy(f -> f.getName().equals(ASMAPI.mapField("field_76292_a")) ? "weight" : f.getName())
			//.registerTypeAdapter(RandomIntRange.class, new RandomIntRange.Serializer())
			.registerTypeAdapter(ChancedEffectInstance.class, new ChancedEffectInstance.Deserializer())
			.registerTypeAdapter(RandomAttributeModifier.class, new RandomAttributeModifier.Deserializer())
			.registerTypeAdapter(AABB.class, new AxisAlignedBBDeserializer())
			.registerTypeAdapter(CompoundTag.class, new NBTAdapter()).create();
	//Formatter::on

	public static final BossItemManager INSTANCE = new BossItemManager();

	private final List<BossItem> entries = new ArrayList<>();
	private volatile int weight = 0;

	public BossItemManager() {
		super(AdventureModule.LOGGER, "bosses", false, false);
	}

	@Override
	protected void beginReload() {
		super.beginReload();
		this.entries.clear();
	}

	@Override
	protected void register(ResourceLocation id, BossItem item) {
		if (!this.registry.containsKey(id)) {
			this.registry.put(id, item);
			this.entries.add(item);
		} else AdventureModule.LOGGER.error("Attempted to register a boss item with name {}, but it already exists!", id);
	}

	@Override
	protected void onReload() {
		super.onReload();
		if (this.entries.size() == 0) throw new RuntimeException("No Bosses were registered.  This is not supported.");
		Collections.shuffle(this.entries);
		this.weight = WeightedRandom.getTotalWeight(this.entries);
		if (this.weight == 0) throw new RuntimeException("The total boss weight is zero.  This is not supported.");
	}

	public BossItem getRandomItem(Random rand) {
		return WeightedRandom.getRandomItem(rand, this.entries, this.weight).get();
	}

	@Nullable
	public BossItem getById(ResourceLocation id) {
		return this.registry.get(id);
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(DEFAULT, new SerializerBuilder<BossItem>("Apotheosis Boss").withJsonDeserializer(obj -> GSON.fromJson(obj, BossItem.class)));
	}

}