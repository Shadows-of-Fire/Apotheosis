package shadows.apotheosis.adventure.affix.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.netty.handler.codec.http.multipart.Attribute;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.placebo.json.DimWeightedJsonReloadListener;
import shadows.placebo.json.JsonUtil;
import shadows.placebo.json.RandomAttributeModifier;
import shadows.placebo.json.SerializerBuilder;

public class GemManager extends DimWeightedJsonReloadListener<Gem> {

	public static final Gson GSON = new GsonBuilder().registerTypeAdapter(RandomAttributeModifier.class, new RandomAttributeModifier.Deserializer()).registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).registerTypeAdapter(Attribute.class, JsonUtil.makeSerializer(ForgeRegistries.ATTRIBUTES)).create();
	public static final GemManager INSTANCE = new GemManager();

	protected List<Gem> gemList = new ArrayList<>();
	protected int totalWeight = 0;

	public GemManager() {
		super(AdventureModule.LOGGER, "gems", false, false);
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(DEFAULT, new SerializerBuilder<Gem>("Gem").withJsonDeserializer(obj -> GSON.fromJson(obj, Gem.class)));
	}

	public static ItemStack getRandomGemStack(Random rand, float luck, ServerLevelAccessor level) {
		return GemItem.fromGem(INSTANCE.getRandomItem(rand, luck, level), rand);
	}

}
