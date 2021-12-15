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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.SpawnData;

public class WeightedSpawnerEntityAdapter implements JsonDeserializer<SpawnData>, JsonSerializer<SpawnData> {

	@Override
	public JsonElement serialize(SpawnData src, Type typeOfSrc, JsonSerializationContext context) {
		String id = src.getTag().getString("id");
		int weight = src.weight;
		CompoundTag nbt = src.getTag();
		JsonObject obj = new JsonObject();
		obj.addProperty("entity", id);
		obj.addProperty("weight", weight);
		if (nbt.size() > 1) obj.add("nbt", context.serialize(nbt));
		return obj;
	}

	@Override
	public SpawnData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj = json.getAsJsonObject();
		String id = obj.get("entity").getAsString();
		if (Strings.isNullOrEmpty(id)) throw new JsonParseException("WeightedSpawnerEntity missing \"entity\" attribute!");
		int weight = obj.get("weight").getAsInt();
		CompoundTag nbt = new CompoundTag();
		if (obj.has("nbt")) {
			CompoundTag entityNbt = context.deserialize(obj.get("nbt"), CompoundTag.class);
			nbt = entityNbt;
		}
		nbt.putString("id", id);
		return new SpawnData(weight, nbt);
	}

}
