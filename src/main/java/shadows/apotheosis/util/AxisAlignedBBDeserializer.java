package shadows.apotheosis.util;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.util.math.AxisAlignedBB;

public class AxisAlignedBBDeserializer implements JsonDeserializer<AxisAlignedBB> {

	@Override
	public AxisAlignedBB deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj = json.getAsJsonObject();
		float width = obj.get("width").getAsFloat();
		float height = obj.get("height").getAsFloat();
		return new AxisAlignedBB(0, 0, 0, width, height, width);
	}

}
