package dev.shadowsoffire.apotheosis.compat.jei;

import java.util.IdentityHashMap;
import java.util.Map;

import dev.shadowsoffire.apotheosis.Apoth.Blocks;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.BasicGemCuttingRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingBlock;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.PurityUpgradeRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class GemCuttingCategory implements IRecipeCategory<GemCuttingRecipe> {

    public static final Identifier TEXTURES = Apotheosis.loc("textures/gui/gem_cutting_jei.png");

    private static final Map<Class<?>, GemCuttingExtension<?>> EXTENSIONS = new IdentityHashMap<>();

    private final IDrawable background;
    private final IDrawable icon;

    public GemCuttingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(TEXTURES, 0, 0, 148, 78).addPadding(0, 0, 0, 0).build();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.GEM_CUTTING_TABLE.value()));
    }

    @Override
    public IRecipeType<GemCuttingRecipe> getRecipeType() {
        return AdventureJEIPlugin.GEM_CUTTING;
    }

    @Override
    public Component getTitle() {
        return GemCuttingBlock.NAME;
    }

    @Override
    public int getWidth() {
        return 148;
    }

    @Override
    public int getHeight() {
        return 78;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void draw(GemCuttingRecipe recipe, mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView, net.minecraft.client.gui.GuiGraphicsExtractor gfx, double mouseX, double mouseY) {
        this.background.draw(gfx);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setRecipe(IRecipeLayoutBuilder builder, GemCuttingRecipe recipe, IFocusGroup focuses) {
        GemCuttingExtension ext = EXTENSIONS.get(recipe.getClass());
        if (ext != null) {
            ext.setRecipe(builder, recipe, focuses);
        }
    }

    public static <T extends GemCuttingRecipe> void registerExtension(Class<T> cls, GemCuttingExtension<T> ext) {
        EXTENSIONS.put(cls, ext);
    }

    public static interface GemCuttingExtension<T extends GemCuttingRecipe> {
        void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses);
    }

    static {
        registerExtension(PurityUpgradeRecipe.class, new PurityUpgradeExtension());
        registerExtension(BasicGemCuttingRecipe.class, new BasicGemCuttingExtension());
    }

}
