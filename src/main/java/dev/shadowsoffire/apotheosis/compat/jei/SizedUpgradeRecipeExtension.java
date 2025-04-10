package dev.shadowsoffire.apotheosis.compat.jei;

import java.util.Arrays;

import dev.shadowsoffire.apotheosis.util.SizedUpgradeRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;

public class SizedUpgradeRecipeExtension implements ISmithingCategoryExtension<SizedUpgradeRecipe> {

    @Override
    public <T extends IIngredientAcceptor<T>> void setTemplate(SizedUpgradeRecipe recipe, T acc) {
        acc.addIngredients(recipe.template());
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setBase(SizedUpgradeRecipe recipe, T acc) {
        acc.addIngredients(recipe.base());
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setAddition(SizedUpgradeRecipe recipe, T acc) {
        acc.addIngredients(VanillaTypes.ITEM_STACK, Arrays.asList(recipe.addition().getItems()));
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setOutput(SizedUpgradeRecipe recipe, T acc) {
        acc.addItemStack(recipe.result());
    }

}
