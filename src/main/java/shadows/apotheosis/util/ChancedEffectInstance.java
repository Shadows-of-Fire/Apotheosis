package shadows.apotheosis.util;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.placebo.util.StepFunction;

/**
 * Represents a potion with a chance to receive this potion.
 */
public class ChancedEffectInstance {

    public static Codec<ChancedEffectInstance> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.FLOAT.fieldOf("chance").forGetter(a -> a.chance),
            ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("effect").forGetter(a -> a.effect),
            StepFunction.CODEC.optionalFieldOf("amplifier", StepFunction.constant(0)).forGetter(a -> a.amp),
            Codec.BOOL.optionalFieldOf("ambient", true).forGetter(a -> a.ambient),
            Codec.BOOL.optionalFieldOf("visible", false).forGetter(a -> a.visible))
        .apply(inst, ChancedEffectInstance::new));

    protected final float chance;
    protected final MobEffect effect;
    protected final StepFunction amp;
    protected final boolean ambient;
    protected final boolean visible;

    /**
     * Creates a Chanced Effect Instance.
     *
     * @param chance The chance this potion is received.
     * @param effect The effect.
     * @param amp    A random range of possible amplifiers.
     */
    public ChancedEffectInstance(float chance, MobEffect effect, StepFunction amp, boolean ambient, boolean visible) {
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

    public MobEffectInstance createInstance(RandomSource rand, int duration) {
        return new MobEffectInstance(this.effect, duration, this.amp.getInt(rand.nextFloat()), this.ambient, this.visible);
    }

    public static class Deserializer implements JsonDeserializer<ChancedEffectInstance> {

        @Override
        public ChancedEffectInstance deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            float chance = obj.get("chance").getAsFloat();
            String _effect = obj.get("effect").getAsString();
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(_effect));
            if (effect == null) throw new JsonParseException("Attempted to construct a ChancedEffectInstance with invalid effect: " + _effect);
            boolean ambient = obj.has("ambient") ? obj.get("ambient").getAsBoolean() : true;
            boolean visible = obj.has("visible") ? obj.get("visible").getAsBoolean() : false;
            if (obj.has("amplifier")) {
                JsonObject range = obj.get("amplifier").getAsJsonObject();
                int min = range.get("min").getAsInt();
                int max = range.get("max").getAsInt();
                StepFunction func = min == max ? StepFunction.constant(min) : new StepFunction(min, max - min, 1);
                return new ChancedEffectInstance(chance, effect, func, ambient, visible);
            }
            return new ChancedEffectInstance(chance, effect, StepFunction.constant(0), ambient, visible);
        }
    }
}
