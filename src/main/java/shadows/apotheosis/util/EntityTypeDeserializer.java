package shadows.apotheosis.util;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityTypeDeserializer implements JsonDeserializer<EntityType<?>> {

	@Override
	public EntityType<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		String id = json.getAsString();
		return ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
	}

}
