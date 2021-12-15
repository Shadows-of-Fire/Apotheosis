package shadows.apotheosis.deadly.affix.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import shadows.apotheosis.deadly.asm.DeadlyHooks.CampfireInventory;

public class SoulfireCookingRecipe extends CampfireCookingRecipe {

	public static final RecipeSerializer<?> SERIALIZER = new SimpleCookingSerializer<>(SoulfireCookingRecipe::new, 100);

	public SoulfireCookingRecipe(ResourceLocation id, String group, Ingredient input, ItemStack output, float exp, int time) {
		super(id, group, input, output, exp, time);
	}

	@Override
	public boolean matches(Container inv, Level world) {
		if (inv instanceof CampfireInventory) {
			CampfireInventory cInv = (CampfireInventory) inv;
			if (world == null || cInv.getTile() == null) return false;
			return cInv.getTile().getBlockState().getBlock() == Blocks.SOUL_CAMPFIRE && this.matches(inv.getItem(0));
		}
		return this.matches(inv.getItem(0));
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(Blocks.SOUL_CAMPFIRE);
	}

	protected boolean matches(ItemStack stack) {
		return this.ingredient.test(stack);
	}

	@Override
	public ItemStack getResultItem() {
		return ItemStack.EMPTY;
	}

	/**
	 * Returns the actual output of this recipe.
	 * Used to trick JEI into hiding it.
	 */
	public ItemStack getSecretOutput() {
		return super.getResultItem();
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

}
