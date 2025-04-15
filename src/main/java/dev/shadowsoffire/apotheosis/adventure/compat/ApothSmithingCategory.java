package dev.shadowsoffire.apotheosis.adventure.compat;

import java.util.IdentityHashMap;
import java.util.Map;

import dev.shadowsoffire.apotheosis.Apotheosis;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.block.Blocks;

public class ApothSmithingCategory implements IRecipeCategory<SmithingRecipe> {

    public static final ResourceLocation RECIPE_GUI_VANILLA = Apotheosis.loc("textures/gui/smithing_jei.png");

    private static final Map<Class<? extends SmithingRecipe>, Extension<SmithingRecipe>> EXTENSIONS = new IdentityHashMap<>();

    private final Component title = Component.translatable("title.apotheosis.smithing");
    private final IDrawable background;
    private final IDrawable icon;

    public ApothSmithingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(RECIPE_GUI_VANILLA, 0, 0, 108, 18).setTextureSize(108, 18).addPadding(0, 16, 16, 16).build();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.SMITHING_TABLE));
    }

    @Override
    public RecipeType<SmithingRecipe> getRecipeType() {
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
    public void draw(SmithingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx, double mouseX, double mouseY) {
        EXTENSIONS.get(recipe.getClass()).draw(recipe, recipeSlotsView, gfx, mouseX, mouseY);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SmithingRecipe recipe, IFocusGroup focuses) {
        EXTENSIONS.get(recipe.getClass()).setRecipe(builder, recipe, focuses);
    }

    @Override
    public boolean isHandled(SmithingRecipe recipe) {
        return EXTENSIONS.containsKey(recipe.getClass());
    }

    public static interface Extension<R extends SmithingRecipe> extends IRecipeCategoryExtension {

        public void setRecipe(IRecipeLayoutBuilder builder, R recipe, IFocusGroup focuses);

        public void draw(R recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx, double mouseX, double mouseY);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <R extends SmithingRecipe> void registerExtension(Class<R> clazz, Extension<R> ext) {
        EXTENSIONS.put(clazz, (Extension) ext);
    }
}
