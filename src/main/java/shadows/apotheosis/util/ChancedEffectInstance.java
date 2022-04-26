package shadows.apotheosis.util;

import java.lang.reflect.Type;
import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Represents a potion with a chance to receive this potion.
*/
public class ChancedEffectInstance {
	protected final float chance;
	protected final MobEffect effect;
	protected final IntValueRange amp;
	protected final boolean ambient;
	protected final boolean visible;

	/**
	 * Creates a Chanced Effect Instance.
	 * @param chance The chance this potion is received.
	 * @param effect The effect.
	 * @param amp A random range of possible amplifiers.
	 */
	public ChancedEffectInstance(float chance, MobEffect effect, IntValueRange amp, boolean ambient, boolean visible) {
		this.chance = chance;
		this.effect = effect;
		this.amp = amp;
		this.ambient = ambient;
		this.visible = visible;
	}

	public float getChance() {
		return this.chance;
	}

	public MobEffect getEffect() {
		return this.effect;
	}

	public MobEffectInstance createInstance(Random rand, int duration) {
		return new MobEffectInstance(this.effect, duration, this.amp.getRandomValue(rand), this.ambient, this.visible);
	}

	public static class Deserializer implements JsonDeserializer<ChancedEffectInstance> {

		@Override
		public ChancedEffectInstance deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			float chance = obj.has("chance") ? obj.get("chance").getAsFloat() : 0f;
			String _effect = obj.get("effect").getAsString();
			MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(_effect));
			if (effect == null) throw new JsonParseException("Attempted to construct a ChancedEffectInstance with invalid effect: " + _effect);
			boolean ambient = obj.has("ambient") ? obj.get("ambient").getAsBoolean() : true;
			boolean visible = obj.has("visible") ? obj.get("visible").getAsBoolean() : false;
			if (obj.has("amplifier")) {
				JsonObject range = obj.get("amplifier").getAsJsonObject();
				int min = range.get("min").getAsInt();
				int max = range.get("max").getAsInt();
				return new ChancedEffectInstance(chance, effect, new IntValueRange(min, max), ambient, visible);
			}
			return new ChancedEffectInstance(chance, effect, IntValueRange.ZERO, ambient, visible);
		}
	}
}