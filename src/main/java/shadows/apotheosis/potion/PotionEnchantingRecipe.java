package shadows.apotheosis.potion;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ench.table.EnchantingRecipe;
import shadows.apotheosis.ench.table.EnchantingStatManager.Stats;

public class PotionEnchantingRecipe extends EnchantingRecipe {

	public static final Serializer SERIALIZER = new Serializer();

	public PotionEnchantingRecipe(Stats requirements, Stats maxRequirements) {
		super(new ResourceLocation(Apotheosis.MODID, "potion_charm_enchanting"), charm(), potion(), requirements, maxRequirements);
	}

	private static ItemStack charm() {
		ItemStack out = new ItemStack(Apoth.Items.POTION_CHARM);
		out.getOrCreateTag().putBoolean("Unbreakable", true);
		return out;
	}

	private static Ingredient potion() {
		List<ItemStack> potionStacks = new ArrayList<>();
		for (Potion p : ForgeRegistries.POTIONS) {
			if (p.getEffects().size() != 1 || p.getEffects().get(0).getEffect().isInstantenous()) continue;
			ItemStack potion = new ItemStack(Apoth.Items.POTION_CHARM);
			PotionUtils.setPotion(potion, p);
			potionStacks.add(potion);
		}
		return Ingredient.of(potionStacks.toArray(new ItemStack[0]));
	}

	@Override
	public boolean matches(ItemStack input, float eterna, float quanta, float arcana) {
		if (input.hasTag() && input.getTag().getBoolean("Unbreakable")) return false;
		return super.matches(input, eterna, quanta, arcana);
	}

	@Override
	public ItemStack assemble(ItemStack input, float eterna, float quanta, float arcana) {
		ItemStack out = input.copy();
		out.setDamageValue(0);
		out.getOrCreateTag().putBoolean("Unbreakable", true);
		return out;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return PotionEnchantingRecipe.SERIALIZER;
	}

	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<PotionEnchantingRecipe> {

		protected static final Gson GSON = new GsonBuilder().create();

		@Override
		public PotionEnchantingRecipe fromJson(ResourceLocation id, JsonObject obj) {
			Stats stats = GSON.fromJson(obj.get("requirements"), Stats.class);
			Stats maxStats = obj.has("max_requirements") ? GSON.fromJson(obj.get("max_requirements"), Stats.class) : NO_MAX;
			if (maxStats.eterna != -1 && stats.eterna > maxStats.eterna) throw new JsonParseException("An enchanting recipe (" + id + ") has invalid min/max eterna bounds (min > max).");
			if (maxStats.quanta != -1 && stats.quanta > maxStats.quanta) throw new JsonParseException("An enchanting recipe (" + id + ") has invalid min/max quanta bounds (min > max).");
			if (maxStats.arcana != -1 && stats.arcana > maxStats.arcana) throw new JsonParseException("An enchanting recipe (" + id + ") has invalid min/max arcana bounds (min > max).");
			return new PotionEnchantingRecipe(stats, maxStats);
		}

		@Override
		public PotionEnchantingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
			Stats stats = Stats.read(buf);
			Stats maxStats = buf.readBoolean() ? Stats.read(buf) : NO_MAX;
			return new PotionEnchantingRecipe(stats, maxStats);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buf, PotionEnchantingRecipe recipe) {
			recipe.requirements.write(buf);
			buf.writeBoolean(recipe.maxRequirements != NO_MAX);
			if (recipe.maxRequirements != NO_MAX) {
				recipe.maxRequirements.write(buf);
			}
		}

	}

}
