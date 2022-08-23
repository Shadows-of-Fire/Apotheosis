package shadows.apotheosis.adventure.boss;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.coremod.api.ASMAPI;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.util.AxisAlignedBBDeserializer;
import shadows.apotheosis.util.ChancedEffectInstance;
import shadows.apotheosis.util.EntityTypeDeserializer;
import shadows.apotheosis.util.GearSet.SetPredicate;
import shadows.apotheosis.util.GearSet.SetPredicateAdapter;
import shadows.placebo.json.DimWeightedJsonReloadListener;
import shadows.placebo.json.NBTAdapter;
import shadows.placebo.json.RandomAttributeModifier;
import shadows.placebo.json.SerializerBuilder;

public class BossItemManager extends DimWeightedJsonReloadListener<BossItem> {

	//Formatter::off
	public static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(EntityType.class, new EntityTypeDeserializer())
			.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.registerTypeAdapter(SetPredicate.class, new SetPredicateAdapter())
			.setFieldNamingStrategy(f -> f.getName().equals(ASMAPI.mapField("field_76292_a")) ? "weight" : f.getName())
			.registerTypeAdapter(ChancedEffectInstance.class, new ChancedEffectInstance.Deserializer())
			.registerTypeAdapter(RandomAttributeModifier.class, new RandomAttributeModifier.Deserializer())
			.registerTypeAdapter(AABB.class, new AxisAlignedBBDeserializer())
			.registerTypeAdapter(LootRarity.class, new LootRarity.Serializer())
			.registerTypeAdapter(CompoundTag.class, new NBTAdapter()).create();
	//Formatter::on

	public static final BossItemManager INSTANCE = new BossItemManager();

	public BossItemManager() {
		super(AdventureModule.LOGGER, "bosses", false, false);
	}

	@Override
	protected void validateItem(BossItem item) {
		super.validateItem(item);
		item.validate();
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(DEFAULT, new SerializerBuilder<BossItem>("Apotheosis Boss").withJsonDeserializer(obj -> GSON.fromJson(obj, BossItem.class)));
	}

}