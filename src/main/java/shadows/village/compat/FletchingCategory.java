package shadows.village.compat;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.util.Translator;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import shadows.Apotheosis;
import shadows.village.fletching.FletchingRecipe;
import shadows.village.fletching.FletchingScreen;

public class FletchingCategory implements IRecipeCategory<FletchingRecipe> {

	public static final ResourceLocation UID = new ResourceLocation(Apotheosis.MODID, "fletching");

	private final IDrawable background;
	private final IDrawable icon;
	private final String localizedName;

	public FletchingCategory(IGuiHelper guiHelper) {
		ResourceLocation location = FletchingScreen.TEXTURES;
		background = guiHelper.createDrawable(location, 6, 16, 139, 54);
		icon = guiHelper.createDrawableIngredient(new ItemStack(Blocks.FLETCHING_TABLE));
		localizedName = Translator.translateToLocal("apotheosis.recipes.fletching");
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public Class<FletchingRecipe> getRecipeClass() {
		return FletchingRecipe.class;
	}

	@Override
	public String getTitle() {
		return localizedName;
	}

	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public void setIngredients(FletchingRecipe recipe, IIngredients ing) {
		ing.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
		ing.setInputIngredients(recipe.getInputs());
	}

	@Override
	public void setRecipe(IRecipeLayout layout, FletchingRecipe recipe, IIngredients ing) {
		IGuiItemStackGroup stacks = layout.getItemStacks();
		stacks.init(0, false, 116, 18);
		stacks.init(1, true, 41, 0);
		stacks.init(2, true, 41, 18);
		stacks.init(3, true, 41, 36);
		stacks.set(ing);
	}

}
