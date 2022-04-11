package shadows.apotheosis.ench.table;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import shadows.apotheosis.ench.table.EnchantingStatManager.Stats;

public class KeepNBTEnchantingRecipe extends EnchantingRecipe {

	public static final Serializer SERIALIZER = new Serializer();

	public KeepNBTEnchantingRecipe(ResourceLocation id, ItemStack output, Ingredient input, Stats requirements, Stats maxRequirements) {
		super(id, output, input, requirements, maxRequirements);
	}

	public ItemStack assemble(ItemStack input, float eterna, float quanta, float arcana) {
		ItemStack out = this.getResultItem().copy();
		if (input.hasTag()) out.setTag(input.getTag().copy());
		return out;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return KeepNBTEnchantingRecipe.SERIALIZER;
	}

	public static class Serializer extends EnchantingRecipe.Serializer {

		@Override
		public KeepNBTEnchantingRecipe fromJson(ResourceLocation id, JsonObject obj) {
			ItemStack output = CraftingHelper.getItemStack(obj.get("result").getAsJsonObject(), true);
			Ingredient input = Ingredient.fromJson(obj.get("input"));
			Stats stats = GSON.fromJson(obj.get("requirements"), Stats.class);
			Stats maxStats = obj.has("max_requirements") ? GSON.fromJson(obj.get("max_requirements"), Stats.class) : NO_MAX;
			if (maxStats.eterna != -1 && stats.eterna > maxStats.eterna) throw new JsonParseException("An enchanting recipe (" + id + ") has invalid min/max eterna bounds (min > max).");
			if (maxStats.quanta != -1 && stats.quanta > maxStats.quanta) throw new JsonParseException("An enchanting recipe (" + id + ") has invalid min/max quanta bounds (min > max).");
			if (maxStats.arcana != -1 && stats.arcana > maxStats.arcana) throw new JsonParseException("An enchanting recipe (" + id + ") has invalid min/max arcana bounds (min > max).");
			return new KeepNBTEnchantingRecipe(id, output, input, stats, maxStats);
		}

	}

}
