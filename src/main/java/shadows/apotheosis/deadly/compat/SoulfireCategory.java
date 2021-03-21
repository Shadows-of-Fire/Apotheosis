package shadows.apotheosis.deadly.compat;

import com.mojang.blaze3d.matrix.MatrixStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.config.Constants;
import mezz.jei.plugins.vanilla.cooking.AbstractCookingCategory;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.affix.recipe.SoulfireCookingRecipe;

public class SoulfireCategory extends AbstractCookingCategory<SoulfireCookingRecipe> {

	public static final ResourceLocation UID = new ResourceLocation(Apotheosis.MODID, "soulfire");

	private final IDrawable background;

	public SoulfireCategory(IGuiHelper guiHelper) {
		super(guiHelper, Blocks.SOUL_CAMPFIRE, "gui.apotheosis.category.soulfire", 400);
		this.background = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 0, 186, 82, 34).addPadding(0, 10, 0, 0).build();
	}

	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public Class<? extends SoulfireCookingRecipe> getRecipeClass() {
		return SoulfireCookingRecipe.class;
	}

	@Override
	public IDrawable getBackground() {
		return this.background;
	}

	@Override
	public void draw(SoulfireCookingRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		this.animatedFlame.draw(matrixStack, 1, 20);
		IDrawableAnimated arrow = this.getArrow(recipe);
		arrow.draw(matrixStack, 24, 8);
		this.drawCookTime(recipe, matrixStack, 35);
	}

	@Override
	public void setIngredients(SoulfireCookingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getSecretOutput());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, SoulfireCookingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(inputSlot, true, 0, 0);
		guiItemStacks.init(outputSlot, false, 60, 8);

		guiItemStacks.set(ingredients);
	}
}