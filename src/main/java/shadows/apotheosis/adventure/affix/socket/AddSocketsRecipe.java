package shadows.apotheosis.adventure.affix.socket;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import shadows.apotheosis.adventure.AdventureModule.ApothUpgradeRecipe;
import shadows.apotheosis.adventure.loot.LootCategory;

public class AddSocketsRecipe extends ApothUpgradeRecipe {

	private final Ingredient input;
	private final int maxSockets;

	public AddSocketsRecipe(ResourceLocation id, Ingredient input, int maxSockets) {
		super(id, Ingredient.EMPTY, input, ItemStack.EMPTY);
		this.input = input;
		this.maxSockets = maxSockets;
	}

	/**
	 * Used to check if a recipe matches current crafting inventory
	 */
	@Override
	public boolean matches(Container pInv, Level pLevel) {
		ItemStack in = pInv.getItem(0);
		return !LootCategory.forItem(in).isNone() && SocketHelper.getSockets(in) < this.getMaxSockets() && this.getInput().test(pInv.getItem(1));
	}

	/**
	 * Returns an Item that is the result of this recipe
	 */
	@Override
	public ItemStack assemble(Container pInv) {
		ItemStack out = pInv.getItem(0).copy();
		if (out.isEmpty()) return ItemStack.EMPTY;
		int sockets = SocketHelper.getSockets(out) + 1;
		SocketHelper.setSockets(out, sockets);
		return out;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	public Ingredient getInput() {
		return input;
	}

	public int getMaxSockets() {
		return maxSockets;
	}

	public static class Serializer implements RecipeSerializer<AddSocketsRecipe> {

		public static Serializer INSTANCE = new Serializer();

		@Override
		public AddSocketsRecipe fromJson(ResourceLocation id, JsonObject obj) {
			Ingredient item = CraftingHelper.getIngredient(GsonHelper.getAsJsonObject(obj, "input"));
			int maxSockets = obj.get("max_sockets").getAsInt();
			return new AddSocketsRecipe(id, item, maxSockets);
		}

		@Override
		public AddSocketsRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
			return new AddSocketsRecipe(id, Ingredient.fromNetwork(buf), buf.readInt());
		}

		@Override
		public void toNetwork(FriendlyByteBuf buf, AddSocketsRecipe recipe) {
			recipe.input.toNetwork(buf);
			buf.writeInt(recipe.getMaxSockets());
		}
	}
}