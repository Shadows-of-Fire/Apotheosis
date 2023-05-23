package shadows.apotheosis.adventure.affix.socket;

import java.util.List;

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

public class SocketingRecipe extends ApothUpgradeRecipe {

	private static final ResourceLocation ID = new ResourceLocation("apotheosis:socketing");

	public SocketingRecipe() {
		super(ID, Ingredient.EMPTY, Ingredient.EMPTY, ItemStack.EMPTY);
	}

	/**
	 * Used to check if a recipe matches current crafting inventory
	 */
	@Override
	public boolean matches(Container pInv, Level pLevel) {
		ItemStack gemStack = pInv.getItem(1);
		Gem gem = GemItem.getGem(gemStack);
		if (gem == null) return false;
		if (!SocketHelper.hasEmptySockets(pInv.getItem(0))) return false;
		return gem.canApplyTo(pInv.getItem(0), gemStack, GemItem.getLootRarity(gemStack));
	}

	/**
	 * Returns an Item that is the result of this recipe
	 */
	@Override
	public ItemStack assemble(Container pInv) {
		ItemStack out = pInv.getItem(0).copy();
		if (out.isEmpty()) return ItemStack.EMPTY;
		out.setCount(1);
		int sockets = SocketHelper.getSockets(out);
		List<ItemStack> gems = SocketHelper.getGems(out, sockets);
		for (int idx = 0; idx < gems.size(); idx++) {
			Gem gem = GemItem.getGem(gems.get(idx));
			if (gem == null) {
				gems.set(idx, pInv.getItem(1));
				break;
			}
		}
		SocketHelper.setGems(out, gems);
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

	public static class Serializer implements RecipeSerializer<SocketingRecipe> {

		public static Serializer INSTANCE = new Serializer();

		@Override
		public SocketingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
			return new SocketingRecipe();
		}

		@Override
		public SocketingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
			return new SocketingRecipe();
		}

		@Override
		public void toNetwork(FriendlyByteBuf pBuffer, SocketingRecipe pRecipe) {

		}
	}
}