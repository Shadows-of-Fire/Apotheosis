package shadows.apotheosis.adventure.affix.socket;

import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import shadows.apotheosis.Apoth;

public class ExtractionRecipe extends UpgradeRecipe implements IExtUpgradeRecipe {

	private static final ResourceLocation ID = new ResourceLocation("apotheosis:extraction");

	public ExtractionRecipe() {
		super(ID, Ingredient.EMPTY, Ingredient.of(Apoth.Items.VIAL_OF_EXTRACTION.get()), ItemStack.EMPTY);
	}

	/**
	 * Used to check if a recipe matches current crafting inventory
	 */
	@Override
	public boolean matches(Container pInv, Level pLevel) {
		List<ItemStack> sockets = SocketHelper.getGems(pInv.getItem(0));
		return pInv.getItem(1).getItem() == Apoth.Items.VIAL_OF_EXTRACTION.get() && !sockets.isEmpty() && !sockets.get(0).isEmpty();
	}

	/**
	 * Returns an Item that is the result of this recipe
	 */
	@Override
	public ItemStack assemble(Container pInv) {
		ItemStack out = pInv.getItem(0);
		return SocketHelper.getGems(out).get(0);
	}

	@Override
	public void onCraft(Container inv, Player player, ItemStack output) {
		ItemStack out = inv.getItem(0);
		List<ItemStack> gems = SocketHelper.getGems(out);
		for (int i = 1; i < gems.size(); i++) {
			ItemStack stack = gems.get(i);
			if (!stack.isEmpty()) {
				if (!player.addItem(stack)) Block.popResource(player.level, player.blockPosition(), stack);
			}
		}
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(Container pContainer) {
		return super.getRemainingItems(pContainer);
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

	public static class Serializer implements RecipeSerializer<ExtractionRecipe> {

		public static Serializer INSTANCE = new Serializer();

		@Override
		public ExtractionRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
			return new ExtractionRecipe();
		}

		@Override
		public ExtractionRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
			return new ExtractionRecipe();
		}

		@Override
		public void toNetwork(FriendlyByteBuf pBuffer, ExtractionRecipe pRecipe) {

		}
	}

}