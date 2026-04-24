package dev.shadowsoffire.apotheosis.compat.jei;

import dev.shadowsoffire.apotheosis.util.SizedUpgradeRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

public class SizedUpgradeRecipeExtension implements ISmithingCategoryExtension<SizedUpgradeRecipe> {

    @Override
    public <T extends IIngredientAcceptor<T>> void setTemplate(SizedUpgradeRecipe recipe, T acc) {
        acc.add(recipe.template());
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setBase(SizedUpgradeRecipe recipe, T acc) {
        acc.add(recipe.base());
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setAddition(SizedUpgradeRecipe recipe, T acc) {
        int count = recipe.addition().count();
        ContextMap ctx = SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
        acc.addIngredients(VanillaTypes.ITEM_STACK, recipe.addition().ingredient().display().resolveForStacks(ctx).stream().map(s -> s.copyWithCount(count)).toList());
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setOutput(SizedUpgradeRecipe recipe, T acc) {
        acc.add(recipe.result());
    }

}
