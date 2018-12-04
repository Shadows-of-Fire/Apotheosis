package shadows.spawn.compat;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.config.Constants;
import net.minecraft.client.resources.I18n;
import shadows.Apotheosis;

public class SpawnerCategory implements IRecipeCategory<SpawnerWrapper> {

	IDrawable bg;

	public SpawnerCategory(IGuiHelper helper) {
		bg = helper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 0, 168, 125, 18).addPadding(0, 24, 24, 24).build();
	}

	@Override
	public String getUid() {
		return SpawnerJEIPlugin.SPAWNER;
	}

	@Override
	public String getTitle() {
		return I18n.format("spw.jei.title");
	}

	@Override
	public String getModName() {
		return Apotheosis.MODID;
	}

	@Override
	public IDrawable getBackground() {
		return bg;
	}

	@Override
	public void setRecipe(IRecipeLayout layout, SpawnerWrapper wrap, IIngredients ing) {
		IGuiItemStackGroup stacks = layout.getItemStacks();
		stacks.init(0, true, 24, 0);
		stacks.init(1, true, 49 + 24, 0);
		stacks.init(2, false, 107 + 24, 0);
		stacks.set(ing);
	}

}
