package shadows.apotheosis.adventure.affix.socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.placebo.json.DimWeightedJsonReloadListener;
import shadows.placebo.json.JsonUtil;
import shadows.placebo.json.PSerializer;

public class GemManager extends DimWeightedJsonReloadListener<Gem> {

	public static final Gson GSON = new GsonBuilder().registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).registerTypeAdapter(Attribute.class, JsonUtil.makeSerializer(ForgeRegistries.ATTRIBUTES)).create();
	public static final GemManager INSTANCE = new GemManager();

	public GemManager() {
		super(AdventureModule.LOGGER, "gems", false, false);
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(DEFAULT, new PSerializer.Builder<Gem>("Gem").withJsonDeserializer(obj -> GSON.fromJson(obj, Gem.class)));
	}

	public static ItemStack getRandomGemStack(RandomSource rand, float luck, ServerLevelAccessor level) {
		return GemItem.fromGem(INSTANCE.getRandomItem(rand, luck, level), rand);
	}

}
