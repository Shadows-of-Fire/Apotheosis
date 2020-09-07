package shadows.apotheosis.util.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;

public class NBTAdapter implements JsonDeserializer<CompoundNBT>, JsonSerializer<CompoundNBT> {

	public static final NBTAdapter INSTANCE = new NBTAdapter();

	@Override
	public JsonElement serialize(CompoundNBT src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(src.toString());
	}

	@Override
	public CompoundNBT deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		try {
			return JsonToNBT.getTagFromJson(json.getAsString());
		} catch (CommandSyntaxException e) {
			throw new JsonParseException(e);
		}
	}

}
