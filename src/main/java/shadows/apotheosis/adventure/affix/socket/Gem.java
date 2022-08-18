package shadows.apotheosis.adventure.affix.socket;

import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.placebo.json.JsonUtil;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyedBase;

public final class Gem extends TypeKeyedBase<Gem> implements WeightedEntry {

	protected int weight;
	protected int variant;
	protected int quality;
	protected Attribute attribute;
	protected Operation operation;
	protected Float2FloatFunction value;

	Gem() {

	}

	public int getVariant() {
		return this.variant;
	}

	public int getQuality() {
		return this.quality;
	}

	@Override
	public Weight getWeight() {
		return Weight.of(this.weight);
	}

	public static Gem fromJson(JsonObject json) {
		Gem gem = new Gem();
		gem.weight = GsonHelper.getAsInt(json, "weight");
		gem.variant = GsonHelper.getAsInt(json, "variant");
		gem.quality = GsonHelper.getAsInt(json, "quality");
		gem.attribute = JsonUtil.getRegistryObject(json, "attribute", ForgeRegistries.ATTRIBUTES);
		gem.operation = GemManager.GSON.fromJson(json.get("operation"), Operation.class);
		JsonObject value = GsonHelper.getAsJsonObject(json, "value");
		gem.value = AffixHelper.step(GsonHelper.getAsFloat(value, "min"), GsonHelper.getAsInt(value, "steps"), GsonHelper.getAsFloat(value, "step"));
		return gem;
	}

	public JsonObject toJson() {
		return new JsonObject(); // TODO: Implement serializer
	}
}
