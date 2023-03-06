package shadows.apotheosis.adventure.affix.socket;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import shadows.apotheosis.adventure.AdventureModule.ApothUpgradeRecipe;
import shadows.apotheosis.adventure.affix.socket.gem.Gem;
import shadows.apotheosis.adventure.affix.socket.gem.GemItem;
import shadows.apotheosis.adventure.loot.LootRarity;

public class GemUpgradeRecipe extends ApothUpgradeRecipe {

	private static final ResourceLocation ID = new ResourceLocation("apotheosis:gem_upgrade");

	public GemUpgradeRecipe() {
		super(ID, Ingredient.EMPTY, Ingredient.EMPTY, ItemStack.EMPTY);
	}

	/**
	 * Used to check if a recipe matches current crafting inventory
	 */
	@Override
	public boolean matches(Container pInv, Level pLevel) {
		ItemStack gemStack = pInv.getItem(0), gemStack2 = pInv.getItem(1);
		Gem gem = GemItem.getGem(gemStack), gem2 = GemItem.getGem(gemStack2);
		if (gem == null || gem != gem2) return false;
		int facets = GemItem.getFacets(gemStack), facets2 = GemItem.getFacets(gemStack2);
		if (facets != gem.getMaxFacets(GemItem.getLootRarity(gemStack)) || facets != facets2) return false;
		LootRarity rarity = GemItem.getLootRarity(gemStack), rarity2 = GemItem.getLootRarity(gemStack2);
		if (rarity == LootRarity.ANCIENT || rarity != rarity2) return false;
		return true;
	}

	/**
	 * Returns an Item that is the result of this recipe
	 */
	@Override
	public ItemStack assemble(Container pInv) {
		ItemStack out = pInv.getItem(0).copy();
		GemItem.setLootRarity(out, GemItem.getLootRarity(out).next());
		GemItem.setFacets(out, 0);
		return out;
	}

	/**
	 * Used to determine if this recipe can fit in a grid of the given width/height
	 */
	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return pWidth * pHeight >= 2;
	}

	/**
	 * Get the result of this recipe, usually for display purposes (e.g. recipe book). If your recipe has more than one
	 * possible result (e.g. it's dynamic and depends on its inputs), then return an empty stack.
	 */
	@Override
	public ItemStack getResultItem() {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(Blocks.SMITHING_TABLE);
	}

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeType.SMITHING;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	public static class Serializer implements RecipeSerializer<GemUpgradeRecipe> {

		public static Serializer INSTANCE = new Serializer();

		@Override
		public GemUpgradeRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
			return new GemUpgradeRecipe();
		}

		@Override
		public GemUpgradeRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
			return new GemUpgradeRecipe();
		}

		@Override
		public void toNetwork(FriendlyByteBuf pBuffer, GemUpgradeRecipe pRecipe) {

		}
	}
}