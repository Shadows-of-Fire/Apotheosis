package shadows.apotheosis.spawn.compat;

import com.mojang.blaze3d.matrix.MatrixStack;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.config.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import shadows.apotheosis.Apotheosis;

public class SpawnerCategory implements IRecipeCategory<SpawnerWrapper> {

	IDrawable bg;
	IDrawable icon;

	public SpawnerCategory(IGuiHelper helper) {
		this.bg = helper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 0, 168, 125, 18).addPadding(0, 24, 24, 24).build();
		this.icon = helper.createDrawableIngredient(new ItemStack(Items.SPAWNER));
	}

	@Override
	public ResourceLocation getUid() {
		return new ResourceLocation(Apotheosis.MODID, SpawnerJEIPlugin.SPAWNER);
	}

	@Override
	public String getTitle() {
		return I18n.format("jei.spw.title");
	}

	@Override
	public IDrawable getBackground() {
		return this.bg;
	}

	@Override
	public void setRecipe(IRecipeLayout layout, SpawnerWrapper wrap, IIngredients ing) {
		IGuiItemStackGroup stacks = layout.getItemStacks();
		stacks.init(0, true, 24, 0);
		stacks.init(1, true, 49 + 24, 0);
		stacks.init(2, false, 107 + 24, 0);
		stacks.set(ing);
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public Class<? extends SpawnerWrapper> getRecipeClass() {
		return SpawnerWrapper.class;
	}

	@Override
	public void setIngredients(SpawnerWrapper wrapper, IIngredients ing) {
		wrapper.getIngredients(ing);
	}

	@Override
	public void draw(SpawnerWrapper recipe, MatrixStack stack, double mouseX, double mouseY) {
		IRecipeCategory.super.draw(recipe, stack, mouseX, mouseY);
		recipe.drawInfo(Minecraft.getInstance(), stack, 0, 40, mouseX, mouseY);
	}

}