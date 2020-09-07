package shadows.apotheosis.util.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.item.ItemStack;
import shadows.apotheosis.util.GearSet.WeightedItemStack;

public class WeightedStackAdapter implements JsonDeserializer<WeightedItemStack>, JsonSerializer<WeightedItemStack> {

	public static final WeightedStackAdapter INSTANCE = new WeightedStackAdapter();

	@Override
	public JsonElement serialize(WeightedItemStack src, Type typeOfSrc, JsonSerializationContext ctx) {
		JsonObject obj = new JsonObject();
		obj.add("weight", ctx.serialize(src.itemWeight));
		obj.add("stack", ctx.serialize(src.getStack()));
		return obj;
	}

	@Override
	public WeightedItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
		JsonObject obj = json.getAsJsonObject();
		int weight = obj.get("weight").getAsInt();
		ItemStack stack = ctx.deserialize(obj.get("stack"), ItemStack.class);
		return new WeightedItemStack(stack, weight);
	}

}