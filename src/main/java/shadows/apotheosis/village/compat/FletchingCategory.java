package shadows.apotheosis.village.compat;

import java.util.Arrays;
import java.util.List;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.village.fletching.FletchingRecipe;
import shadows.apotheosis.village.fletching.FletchingScreen;

public class FletchingCategory implements IRecipeCategory<FletchingRecipe> {

	public static final ResourceLocation UID = new ResourceLocation(Apotheosis.MODID, "fletching");
	public static final RecipeType<FletchingRecipe> TYPE = RecipeType.create(Apotheosis.MODID, "fletching", FletchingRecipe.class);

	private final IDrawable background;
	private final IDrawable icon;
	private final Component localizedName;

	public FletchingCategory(IGuiHelper guiHelper) {
		ResourceLocation location = FletchingScreen.TEXTURES;
		this.background = guiHelper.createDrawable(location, 6, 16, 139, 54);
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(Blocks.FLETCHING_TABLE));
		this.localizedName = new TranslatableComponent("apotheosis.recipes.fletching");
	}

	@Override
	public IDrawable getBackground() {
		return this.background;
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public Class<FletchingRecipe> getRecipeClass() {
		return FletchingRecipe.class;
	}

	@Override
	public Component getTitle() {
		return this.localizedName;
	}

	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public RecipeType<FletchingRecipe> getRecipeType() {
		return TYPE;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, FletchingRecipe recipe, IFocusGroup focuses) {
		List<List<ItemStack>> inputs = recipe.getIngredients().stream().map(i -> Arrays.asList(i.getItems())).toList();
		for (int i = 0; i < 3; i++) {
			builder.addSlot(RecipeIngredientRole.INPUT, 42, 1 + i * 18).addIngredients(VanillaTypes.ITEM, inputs.get(i));
		}
		builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 19).addIngredient(VanillaTypes.ITEM, recipe.getResultItem());
	}

}