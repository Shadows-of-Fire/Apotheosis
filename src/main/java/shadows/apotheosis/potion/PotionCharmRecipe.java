package shadows.apotheosis.potion;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.deadly.affix.AffixHelper;

public class PotionCharmRecipe extends ShapedRecipe {

	public PotionCharmRecipe() {
		super(new ResourceLocation(Apotheosis.MODID, "potion_charm"), "", 3, 3, makeIngredients(), new ItemStack(ApotheosisObjects.POTION_CHARM));
	}

	private static NonNullList<Ingredient> makeIngredients() {
		Ingredient blaze = Ingredient.fromStacks(new ItemStack(Items.BLAZE_POWDER));
		List<ItemStack> potionStacks = new ArrayList<>();
		for (Potion p : ForgeRegistries.POTION_TYPES) {
			if (p.getEffects() == null || p.getEffects().size() != 1) continue;
			ItemStack potion = new ItemStack(Items.POTION);
			PotionUtils.addPotionToItemStack(potion, p);
			AffixHelper.addLore(potion, new TranslationTextComponent("info.apotheosis.any_same_potion"));
			potionStacks.add(potion);
		}
		Ingredient potion = Ingredient.fromStacks(potionStacks.toArray(new ItemStack[0]));
		return NonNullList.from(Ingredient.EMPTY, blaze, blaze, blaze, potion, potion, potion, blaze, blaze, blaze);
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {
		ItemStack out = super.getCraftingResult(inv);
		PotionUtils.addPotionToItemStack(out, PotionUtils.getPotionFromItem(inv.getStackInSlot(4)));
		return out;
	}

	@Override
	public boolean matches(CraftingInventory inv, World world) {
		if (super.matches(inv, world)) {
			Potion left = PotionUtils.getPotionFromItem(inv.getStackInSlot(3));
			Potion mid = PotionUtils.getPotionFromItem(inv.getStackInSlot(4));
			Potion right = PotionUtils.getPotionFromItem(inv.getStackInSlot(5));
			if (left != null && mid != null && right != null) {
				if (mid.getEffects().size() != 1 || mid.getEffects().get(0).getPotion().isInstant()) return false;
				return left.getRegistryName().equals(mid.getRegistryName()) && mid.getRegistryName().equals(right.getRegistryName());
			}
		}
		return false;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
	}

	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<PotionCharmRecipe> {

		public static final Serializer INSTANCE = new Serializer();

		@Override
		public PotionCharmRecipe read(ResourceLocation recipeId, JsonObject json) {
			return new PotionCharmRecipe();
		}

		@Override
		public PotionCharmRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
			return new PotionCharmRecipe();
		}

		@Override
		public void write(PacketBuffer buffer, PotionCharmRecipe recipe) {
		}

	}

}