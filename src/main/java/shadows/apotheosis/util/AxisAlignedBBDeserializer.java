package shadows.apotheosis.util;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.world.phys.AABB;

public class AxisAlignedBBDeserializer implements JsonDeserializer<AABB> {

	@Override
	public AABB deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj = json.getAsJsonObject();
		float width = obj.get("width").getAsFloat();
		float height = obj.get("height").getAsFloat();
		return new AABB(0, 0, 0, width, height, width);
	}

}
