package dev.shadowsoffire.apotheosis.compat.jei;

import dev.shadowsoffire.apotheosis.compat.jei.GemCuttingCategory.GemCuttingExtension;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.BasicGemCuttingRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class BasicGemCuttingExtension implements GemCuttingExtension<BasicGemCuttingRecipe> {

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BasicGemCuttingRecipe recipe, IFocusGroup focuses) {

        builder.addSlot(RecipeIngredientRole.INPUT, 48, 37).addIngredients(VanillaTypes.ITEM_STACK, recipe.base().items().map(h -> new ItemStack(h)).toList());

        builder.addSlot(RecipeIngredientRole.INPUT, 48, 4).addIngredients(VanillaTypes.ITEM_STACK, recipe.top().stream().flatMap(BasicGemCuttingExtension::sizedStacks).toList());
        builder.addSlot(RecipeIngredientRole.INPUT, 19, 56).addIngredients(VanillaTypes.ITEM_STACK, recipe.left().stream().flatMap(BasicGemCuttingExtension::sizedStacks).toList());
        builder.addSlot(RecipeIngredientRole.INPUT, 76, 56).addIngredients(VanillaTypes.ITEM_STACK, recipe.right().stream().flatMap(BasicGemCuttingExtension::sizedStacks).toList());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 35).addIngredient(VanillaTypes.ITEM_STACK, recipe.output());

    }

    private static java.util.stream.Stream<ItemStack> sizedStacks(SizedIngredient si) {
        return si.ingredient().items().map(h -> new ItemStack(h, si.count()));
    }

}
