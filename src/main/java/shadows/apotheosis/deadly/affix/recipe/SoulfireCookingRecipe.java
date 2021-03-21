package shadows.apotheosis.deadly.affix.recipe;

import net.minecraft.block.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CampfireCookingRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import shadows.apotheosis.deadly.asm.DeadlyHooks.CampfireInventory;

public class SoulfireCookingRecipe extends CampfireCookingRecipe {

	public SoulfireCookingRecipe(ResourceLocation id, String group, Ingredient input, ItemStack output, float exp, int time) {
		super(id, group, input, output, exp, time);
	}

	@Override
	public boolean matches(IInventory inv, World world) {
		if (inv instanceof CampfireInventory) {
			CampfireInventory cInv = (CampfireInventory) inv;
			if (world == null || cInv.getTile() == null) return false;
			return cInv.getTile().getBlockState().getBlock() == Blocks.SOUL_CAMPFIRE && this.matches(inv.getStackInSlot(0));
		}
		return this.matches(inv.getStackInSlot(0));
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Blocks.SOUL_CAMPFIRE);
	}

	protected boolean matches(ItemStack stack) {
		return this.ingredient.test(stack);
	}

	@Override
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}

	/**
	 * Returns the actual output of this recipe.
	 * Used to trick JEI into hiding it.
	 */
	public ItemStack getSecretOutput() {
		return super.getRecipeOutput();
	}

}
