package shadows.apotheosis.deadly.reload;

import java.util.*;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.coremod.api.ASMAPI;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.loot.BossItem;
import shadows.apotheosis.util.AxisAlignedBBDeserializer;
import shadows.apotheosis.util.ChancedEffectInstance;
import shadows.apotheosis.util.EntityTypeDeserializer;
import shadows.apotheosis.util.GearSet.SetPredicate;
import shadows.apotheosis.util.GearSet.SetPredicateAdapter;
import shadows.apotheosis.util.RandomAttributeModifier;
import shadows.placebo.json.NBTAdapter;
import shadows.placebo.json.PlaceboJsonReloadListener;
import shadows.placebo.json.SerializerBuilder;

public class BossItemManager extends PlaceboJsonReloadListener<BossItem> {
//
	//Formatter::off
	public static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(EntityType.class, new EntityTypeDeserializer())
			.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.registerTypeAdapter(SetPredicate.class, new SetPredicateAdapter())
			.registerTypeAdapter(ChancedEffectInstance.class, new ChancedEffectInstance.Deserializer())
			.registerTypeAdapter(RandomAttributeModifier.class, new RandomAttributeModifier.Deserializer())
			.registerTypeAdapter(AABB.class, new AxisAlignedBBDeserializer())
			.registerTypeAdapter(CompoundTag.class, new NBTAdapter())
			.create();
	//Formatter::on

	public static final BossItemManager INSTANCE = new BossItemManager();

	private final List<BossItem> entries = new ArrayList<>();

	public BossItemManager() {
		super(DeadlyModule.LOGGER, "bosses", false, false);
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(
				DEFAULT,
				new SerializerBuilder<BossItem>("BossItem")
				.withJsonDeserializer(jsonObject -> GSON.fromJson(jsonObject, BossItem.class)));
	}

	@Override
	protected <T extends BossItem> void register(ResourceLocation id, T item) {
		if (!this.registry.containsKey(id)) {
			this.registry.put(id, item);
			this.entries.add(item);
		} else this.logger.error("Attempted to register a boss item with name {}, but it already exists!", id);
	}

	@Override
	protected void onReload()
	{
		super.onReload();
		if (this.entries.size() == 0) this.logger.error("No Bosses were registered.  This is not supported.");
	}

	public Optional<BossItem> getRandomItem(Random rand) {
		return WeightedRandom.getRandomItem(rand, this.entries);
	}

	@Nullable
	public BossItem getById(ResourceLocation id) {
		return this.registry.get(id);
	}

}
