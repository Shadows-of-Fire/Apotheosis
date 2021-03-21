package shadows.apotheosis.util;

import java.lang.reflect.Type;
import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class RandomAttributeModifier {

	protected final Attribute attribute;
	protected final Operation op;
	protected final RandomValueRange value;

	/**
	 * Creates a Chanced Effect Instance.
	 * @param chance The chance this potion is received.
	 * @param effect The effect.
	 * @param amp A random range of possible amplifiers.
	 */
	public RandomAttributeModifier(Attribute attribute, Operation op, RandomValueRange value) {
		this.attribute = attribute;
		this.op = op;
		this.value = value;
	}

	public void apply(Random rand, MobEntity entity) {
		AttributeModifier modif = new AttributeModifier("apoth_boss_" + this.attribute.getAttributeName(), this.value.generateFloat(rand), this.op);
		entity.getAttribute(this.attribute).applyPersistentModifier(modif);
	}

	public static class Deserializer implements JsonDeserializer<RandomAttributeModifier> {

		@Override
		public RandomAttributeModifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			String _attribute = obj.get("attribute").getAsString();
			Operation op = ctx.deserialize(obj.get("operation"), Operation.class);
			RandomValueRange value = ctx.deserialize(obj.get("value"), RandomValueRange.class);
			Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(_attribute));
			return new RandomAttributeModifier(attribute, op, value);
		}
	}
}
