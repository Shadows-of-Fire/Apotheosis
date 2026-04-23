package dev.shadowsoffire.apotheosis.compat.jei;

import java.util.List;

import org.joml.Matrix3x2fStack;

import dev.shadowsoffire.apotheosis.Apoth.Items;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipe;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipe.OutputData;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;

@SuppressWarnings("removal")
public class SalvagingCategory implements IRecipeCategory<SalvagingRecipe> {

    public static final Identifier TEXTURES = Apotheosis.loc("textures/gui/salvage_jei.png");

    private final Component title = Component.translatable("title.apotheosis.salvaging");
    private final IDrawable background;
    private final IDrawable icon;

    public SalvagingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(TEXTURES, 0, 0, 98, 74).addPadding(0, 0, 0, 0).build();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.SALVAGING_TABLE));
    }

    @Override
    public IRecipeType<SalvagingRecipe> getRecipeType() {
        return AdventureJEIPlugin.SALVAGING;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public int getWidth() {
        return 98;
    }

    @Override
    public int getHeight() {
        return 74;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void draw(SalvagingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor gfx, double mouseX, double mouseY) {
        this.background.draw(gfx);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, SalvagingRecipe recipe, IFocusGroup focuses) {
        builder.addWidget(new OutputCountWidget(recipe.getOutputs()));
    }

    private static class OutputCountWidget implements IRecipeWidget {

        private final List<OutputData> outputs;

        OutputCountWidget(List<OutputData> outputs) {
            this.outputs = outputs;
        }

        @Override
        public ScreenPosition getPosition() {
            return new ScreenPosition(0, 0);
        }

        @Override
        public void drawWidget(GuiGraphicsExtractor gfx, double mouseX, double mouseY) {
            Font font = Minecraft.getInstance().font;
            Matrix3x2fStack pose = gfx.pose();
            int idx = 0;
            for (var d : this.outputs) {
                pose.pushMatrix();
                String text = String.format("%d-%d", d.min(), d.max());

                float x = 59 + 18 * (idx % 2) + (16 - font.width(text) * 0.5F);
                float y = 23F + 18 * (idx / 2);

                float scale = 0.5F;

                pose.scale(scale, scale);
                gfx.text(font, text, (int) (x / scale), (int) (y / scale), 0xFFFFFFFF);

                idx++;
                pose.popMatrix();
            }
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SalvagingRecipe recipe, IFocusGroup focuses) {
        ItemStack focusStack = focuses.getFocuses(VanillaTypes.ITEM_STACK).findFirst()
            .map(IFocus::getTypedValue).map(ITypedIngredient::getIngredient).orElse(ItemStack.EMPTY);

        List<ItemStack> input;
        if (!focusStack.isEmpty() && recipe.getInput().test(focusStack)) {
            input = List.of(focusStack);
        }
        else {
            Level level = Minecraft.getInstance().level;
            ContextMap ctx = level != null ? SlotDisplayContext.fromLevel(level) : ContextMap.EMPTY;
            input = recipe.getInput().display().resolveForStacks(ctx);
        }

        builder.addSlot(RecipeIngredientRole.INPUT, 5, 29).addIngredients(VanillaTypes.ITEM_STACK, input);
        List<OutputData> outputs = recipe.getOutputs();
        int idx = 0;
        for (var d : outputs) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 59 + 18 * (idx % 2), 11 + 18 * (idx / 2)).addIngredient(VanillaTypes.ITEM_STACK, d.stack().create());
            idx++;
        }
    }

}
