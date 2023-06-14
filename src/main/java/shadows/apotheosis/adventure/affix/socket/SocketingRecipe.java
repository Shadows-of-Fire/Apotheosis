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
import net.minecraftforge.common.MinecraftForge;
import shadows.apotheosis.adventure.AdventureModule.ApothUpgradeRecipe;
import shadows.apotheosis.adventure.affix.socket.gem.Gem;
import shadows.apotheosis.adventure.affix.socket.gem.GemItem;
import shadows.apotheosis.adventure.event.ItemSocketingEvent;

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
	public ItemStack assemble(Container inventory) {
		var result = inventory.getItem(0).copy();
		if (result.isEmpty()) return ItemStack.EMPTY;
		result.setCount(1);
		var sockets = SocketHelper.getSockets(result);
		var gems = SocketHelper.getGems(result, sockets);
		var gemStack = inventory.getItem(1).copy();
		var event = new ItemSocketingEvent(result, gemStack);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled()) return ItemStack.EMPTY;
		result = event.getItemStack();
		gemStack = event.getGemStack();
		var socket = SocketHelper.getEmptySocket(result);
		gems.set(socket, gemStack);
		SocketHelper.setGems(result, gems);
		return result;
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