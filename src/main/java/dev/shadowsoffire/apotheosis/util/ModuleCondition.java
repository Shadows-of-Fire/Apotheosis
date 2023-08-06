package dev.shadowsoffire.apotheosis.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class ModuleCondition implements ICondition {

    static ResourceLocation id = new ResourceLocation(Apotheosis.MODID, "module");

    static Map<String, Supplier<Boolean>> types = new HashMap<>();
    static {
        types.put("spawner", () -> Apotheosis.enableSpawner);
        types.put("garden", () -> Apotheosis.enableGarden);
        types.put("deadly", () -> Apotheosis.enableAdventure); // Deprecated
        types.put("adventure", () -> Apotheosis.enableAdventure);
        types.put("enchantment", () -> Apotheosis.enableEnch);
        types.put("potion", () -> Apotheosis.enablePotion);
        types.put("village", () -> Apotheosis.enableVillage);
        types.put("book", () -> Apotheosis.giveBook);
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
    public boolean test(IContext context) {
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
