package dev.shadowsoffire.apotheosis.compat.jei;

import java.util.List;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.effect.StoneformingAffix;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/**
 * Displays the candidate block set of each {@link StoneformingAffix}, showing that any block in the set may be converted into any other.
 * <p>
 * One entry is registered per stoneforming-type affix. Candidates are resolved when the layout is built, since the underlying block tags
 * may change on reload.
 */
public class StoneformingCategory extends AbstractRecipeCategory<StoneformingCategory.StoneformingDisplay> {

    public static final int WIDTH = 140;
    public static final int HEIGHT = 86;
    public static final int GRID_COLUMNS = 4;
    public static final int GRID_VISIBLE_ROWS = 4;

    public StoneformingCategory(IGuiHelper guiHelper) {
        super(AdventureJEIPlugin.STONEFORMING, Apotheosis.lang("title", "stoneforming"), guiHelper.createDrawableItemLike(Blocks.STONE), WIDTH, HEIGHT);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, StoneformingDisplay display, IFocusGroup focuses) {
        List<ItemStack> candidates = display.candidates();
        Component hint = Apotheosis.lang("info", "stoneforming", display.name()).withStyle(ChatFormatting.GRAY);
        Component targetHint = Apotheosis.lang("info", "stoneforming.target").withStyle(ChatFormatting.GRAY);

        builder.addInputSlot(2, 42)
            .setStandardSlotBackground()
            .addItemStacks(candidates)
            .addRichTooltipCallback((view, tooltip) -> {
                tooltip.add(hint);
                tooltip.add(targetHint);
            });

        // The scroll grid positions and draws these slots, so they are created without positions or backgrounds.
        for (ItemStack stack : candidates) {
            builder.addOutputSlot()
                .addItemStack(stack);
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, StoneformingDisplay display, IFocusGroup focuses) {
        builder.addText(display.name(), WIDTH, 10).setPosition(0, 2).setTextAlignment(HorizontalAlignment.CENTER).setColor(0xFF404040);
        List<IRecipeSlotDrawable> outputs = builder.getRecipeSlots().getSlots(RecipeIngredientRole.OUTPUT);
        if (!outputs.isEmpty()) {
            builder.addRecipeArrow().setPosition(24, 42);
            builder.addScrollGridWidget(outputs, GRID_COLUMNS, GRID_VISIBLE_ROWS).setPosition(52, 14);
        }
    }

    @Override
    public ResourceLocation getRegistryName(StoneformingDisplay display) {
        return display.affix().getId();
    }

    /**
     * JEI display entry for a single stoneforming-type affix.
     * <p>
     * Only the affix reference is held, so that the candidate set is resolved from the live block tags when displayed.
     */
    public record StoneformingDisplay(DynamicHolder<Affix> affix) {

        public List<ItemStack> candidates() {
            if (!this.affix.isBound() || !(this.affix.get() instanceof StoneformingAffix stoneforming)) {
                return List.of();
            }
            return stoneforming.getCandidates().stream()
                .map(holder -> new ItemStack(holder.value()))
                .filter(stack -> !stack.isEmpty())
                .toList();
        }

        public Component name() {
            return this.affix.isBound() ? this.affix.get().getName(true) : Component.empty();
        }

    }

}
