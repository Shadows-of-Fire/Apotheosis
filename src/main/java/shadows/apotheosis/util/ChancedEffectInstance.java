package shadows.apotheosis.util;

import java.lang.reflect.Type;
import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Represents a potion with a chance to receive this potion.
*/
public class ChancedEffectInstance {
	protected final float chance;
	protected final Effect effect;
	protected final RandomIntRange amp;
	protected final boolean ambient;
	protected final boolean visible;

	/**
	 * Creates a Chanced Effect Instance.
	 * @param chance The chance this potion is received.
	 * @param effect The effect.
	 * @param amp A random range of possible amplifiers.
	 */
	public ChancedEffectInstance(float chance, Effect effect, RandomIntRange amp, boolean ambient, boolean visible) {
		this.chance = chance;
		this.effect = effect;
		this.amp = amp;
		this.ambient = ambient;
		this.visible = visible;
	}

	public float getChance() {
		return this.chance;
	}

	public Effect getEffect() {
		return this.effect;
	}

	public EffectInstance createInstance(Random rand, int duration) {
		return new EffectInstance(this.effect, duration, this.amp.generateInt(rand), this.ambient, this.visible);
	}

	public static class Deserializer implements JsonDeserializer<ChancedEffectInstance> {

		@Override
		public ChancedEffectInstance deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			float chance = obj.get("chance").getAsFloat();
			String _effect = obj.get("effect").getAsString();
			Effect effect = ForgeRegistries.POTIONS.getValue(new ResourceLocation(_effect));
			boolean ambient = obj.has("ambient") ? obj.get("ambient").getAsBoolean() : true;
			boolean visible = obj.has("visible") ? obj.get("visible").getAsBoolean() : false;
			if (obj.has("amplifier")) {
				JsonObject range = obj.get("amplifier").getAsJsonObject();
				int min = range.get("min").getAsInt();
				int max = range.get("max").getAsInt();
				return new ChancedEffectInstance(chance, effect, new RandomIntRange(min, max), ambient, visible);
			}
			return new ChancedEffectInstance(chance, effect, RandomIntRange.ZERO, ambient, visible);
		}
	}
}