package dev.shadowsoffire.apotheosis.adventure.compat;

import java.util.IdentityHashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.block.Blocks;

public class ApothSmithingCategory implements IRecipeCategory<UpgradeRecipe> {

    public static final ResourceLocation RECIPE_GUI_VANILLA = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");

    private static final Map<Class<? extends UpgradeRecipe>, Extension<UpgradeRecipe>> EXTENSIONS = new IdentityHashMap<>();

    private final Component title = Component.translatable("title.apotheosis.smithing");
    private final IDrawable background;
    private final IDrawable icon;

    public ApothSmithingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(RECIPE_GUI_VANILLA, 0, 168, 125, 18).addPadding(0, 16, 0, 0).build();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.SMITHING_TABLE));
    }

    @Override
    public RecipeType<UpgradeRecipe> getRecipeType() {
        return AdventureJEIPlugin.APO_SMITHING;
    }

    @Override
    public Component getTitle() {
        return this.title;
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
    public void draw(UpgradeRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        EXTENSIONS.get(recipe.getClass()).draw(recipe, recipeSlotsView, stack, mouseX, mouseY);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, UpgradeRecipe recipe, IFocusGroup focuses) {
        EXTENSIONS.get(recipe.getClass()).setRecipe(builder, recipe, focuses);
    }

    @Override
    public boolean isHandled(UpgradeRecipe recipe) {
        return EXTENSIONS.containsKey(recipe.getClass());
    }

    public static interface Extension<R extends UpgradeRecipe>extends IRecipeCategoryExtension {

        public void setRecipe(IRecipeLayoutBuilder builder, R recipe, IFocusGroup focuses);

        @Override
        @Deprecated
        default void drawInfo(int recipeWidth, int recipeHeight, PoseStack stack, double mouseX, double mouseY) {
            IRecipeCategoryExtension.super.drawInfo(recipeWidth, recipeHeight, stack, mouseX, mouseY);
        }

        public void draw(R recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <R extends UpgradeRecipe> void registerExtension(Class<R> clazz, Extension<R> ext) {
        EXTENSIONS.put(clazz, (Extension) ext);
    }
}
