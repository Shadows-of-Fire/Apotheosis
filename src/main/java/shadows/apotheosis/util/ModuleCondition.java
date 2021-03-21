package shadows.apotheosis.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import shadows.apotheosis.Apotheosis;

public class ModuleCondition implements ICondition {

	static ResourceLocation id = new ResourceLocation(Apotheosis.MODID, "module");

	static Map<String, Supplier<Boolean>> types = new HashMap<>();
	static {
		types.put("spawner", () -> Apotheosis.enableSpawner);
		types.put("garden", () -> Apotheosis.enableGarden);
		types.put("deadly", () -> Apotheosis.enableDeadly);
		types.put("enchantment", () -> Apotheosis.enableEnch);
		types.put("potion", () -> Apotheosis.enablePotion);
		types.put("village", () -> Apotheosis.enableVillage);
	}

	final String name;

	public ModuleCondition(String name) {
		this.name = name;
	}

	@Override
	public ResourceLocation getID() {
		return id;
	}

	@Override
	public boolean test() {
		return types.get(this.name).get();
	}

	public static class Serializer implements IConditionSerializer<ModuleCondition> {

		@Override
		public void write(JsonObject json, ModuleCondition value) {
			json.addProperty("field", value.name);
		}

		@Override
		public ModuleCondition read(JsonObject json) {
			if (!json.has("module") || !types.containsKey(json.get("module").getAsString())) throw new JsonParseException("Invalid module condition!");
			return new ModuleCondition(json.get("module").getAsString());
		}

		@Override
		public ResourceLocation getID() {
			return id;
		}

	}

}