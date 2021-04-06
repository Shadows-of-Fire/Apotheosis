package shadows.apotheosis.util;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import joptsimple.internal.Strings;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.WeightedSpawnerEntity;

public class WeightedSpawnerEntityAdapter implements JsonDeserializer<WeightedSpawnerEntity>, JsonSerializer<WeightedSpawnerEntity> {

	@Override
	public JsonElement serialize(WeightedSpawnerEntity src, Type typeOfSrc, JsonSerializationContext context) {
		String id = src.getNbt().getString("id");
		int weight = src.itemWeight;
		CompoundNBT nbt = src.getNbt();
		JsonObject obj = new JsonObject();
		obj.addProperty("entity", id);
		obj.addProperty("weight", weight);
		if (nbt.size() > 1) obj.add("nbt", context.serialize(nbt));
		return obj;
	}

	@Override
	public WeightedSpawnerEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj = json.getAsJsonObject();
		String id = obj.get("entity").getAsString();
		if (Strings.isNullOrEmpty(id)) throw new JsonParseException("WeightedSpawnerEntity missing \"entity\" attribute!");
		int weight = obj.get("weight").getAsInt();
		CompoundNBT nbt = new CompoundNBT();
		if (obj.has("nbt")) {
			CompoundNBT entityNbt = context.deserialize(obj.get("nbt"), CompoundNBT.class);
			nbt = entityNbt;
		}
		nbt.putString("id", id);
		return new WeightedSpawnerEntity(weight, nbt);
	}

}
