package dev.shadowsoffire.apotheosis.compat.jei;

import java.util.ArrayList;
import java.util.List;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.compat.enchanting.CharmInfusionRecipe;
import dev.shadowsoffire.apotheosis.item.PotionCharmItem;
import dev.shadowsoffire.apothic_enchanting.compat.InfusionRecipeCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

public class CharmInfusionExtension implements InfusionRecipeCategory.Extension<CharmInfusionRecipe> {

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IRecipeSlotBuilder input, IRecipeSlotBuilder output, CharmInfusionRecipe recipe, IFocusGroup focuses) {
        ItemStack stack = focuses.getFocuses(VanillaTypes.ITEM_STACK).findFirst().map(IFocus::getTypedValue).map(ITypedIngredient::getIngredient).orElse(ItemStack.EMPTY);
        if (PotionCharmItem.hasEffect(stack)) {
            ItemStack in = stack.copy();
            in.remove(DataComponents.UNBREAKABLE);
            ItemStack out = stack.copy();
            out.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
            input.add(VanillaTypes.ITEM_STACK, in);
            output.add(VanillaTypes.ITEM_STACK, out);
        }
        else {
            List<ItemStack> potionStacks = new ArrayList<>();
            List<ItemStack> unbreakable = new ArrayList<>();

            BuiltInRegistries.POTION.listElements()
                .filter(PotionCharmItem::isValidPotion)
                .forEach(p -> {
                    ItemStack charm = PotionContents.createItemStack(Apoth.Items.POTION_CHARM.value(), p);
                    potionStacks.add(charm);
                    charm = charm.copy();
                    charm.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
                    unbreakable.add(charm);
                });

            input.addIngredients(VanillaTypes.ITEM_STACK, potionStacks);
            output.addIngredients(VanillaTypes.ITEM_STACK, unbreakable);
        }
        builder.createFocusLink(input, output);
    }

}
