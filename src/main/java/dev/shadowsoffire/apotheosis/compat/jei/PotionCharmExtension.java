package dev.shadowsoffire.apotheosis.compat.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.item.PotionCharmItem;
import dev.shadowsoffire.apotheosis.recipe.PotionCharmRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

public class PotionCharmExtension implements ICraftingCategoryExtension<PotionCharmRecipe> {

    @Override
    public int getWidth(RecipeHolder<PotionCharmRecipe> recipeHolder) {
        return recipeHolder.value().getWidth();
    }

    @Override
    public int getHeight(RecipeHolder<PotionCharmRecipe> recipeHolder) {
        return recipeHolder.value().getHeight();
    }

    @Override
    public void setRecipe(RecipeHolder<PotionCharmRecipe> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        ItemStack focusStack = focuses.getFocuses(VanillaTypes.ITEM_STACK).findFirst().map(IFocus::getTypedValue).map(ITypedIngredient::getIngredient).orElse(ItemStack.EMPTY);
        Holder<Potion> potion = focusStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().orElse(Potions.WATER);

        List<List<ItemStack>> recipeInputs = recipeHolder.value().getIngredients().stream()
            .map(Ingredient::getItems)
            .map(a -> {
                // Copy the stacks, as we modify them later, and we don't want to modify the ingredients' cached stacks.
                List<ItemStack> list = new ArrayList<>(a.length);
                for (ItemStack s : a) {
                    list.add(s.copy());
                }
                return list;
            })
            .collect(Collectors.toCollection(ArrayList::new));

        // If we have a focus, we need to manipulate the potion-contents having input items to match that focus.
        if (PotionCharmItem.isValidPotion(potion)) {
            for (List<ItemStack> stacks : recipeInputs) {
                if (stacks.get(0).has(DataComponents.POTION_CONTENTS)) {
                    for (ItemStack s : stacks) {
                        s.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
                    }
                }
            }
        }
        else {
            // If we don't... well, we need to explode the potion-holding item lists into the full set of potion items.
            for (int i = 0; i < recipeInputs.size(); i++) {
                List<ItemStack> stacks = recipeInputs.get(i);
                if (stacks.get(0).has(DataComponents.POTION_CONTENTS)) {
                    Item mainItem = stacks.get(0).getItem();
                    List<ItemStack> potionStacks = new ArrayList<>();
                    BuiltInRegistries.POTION.holders()
                        .filter(PotionCharmItem::isValidPotion)
                        .forEach(p -> {
                            potionStacks.add(PotionContents.createItemStack(mainItem, p));
                        });
                    recipeInputs.set(i, potionStacks);
                }
            }
        }

        craftingGridHelper.createAndSetInputs(builder, VanillaTypes.ITEM_STACK, recipeInputs, this.getWidth(recipeHolder), this.getHeight(recipeHolder));

        if (PotionCharmItem.isValidPotion(potion)) {
            ItemStack output = PotionContents.createItemStack(Apoth.Items.POTION_CHARM.value(), potion);
            craftingGridHelper.createAndSetOutputs(builder, VanillaTypes.ITEM_STACK, Arrays.asList(output));
        }
        else {
            List<ItemStack> potionStacks = new ArrayList<>();
            BuiltInRegistries.POTION.holders()
                .filter(PotionCharmItem::isValidPotion)
                .forEach(p -> {
                    potionStacks.add(PotionContents.createItemStack(Apoth.Items.POTION_CHARM.value(), p));
                });
            craftingGridHelper.createAndSetOutputs(builder, VanillaTypes.ITEM_STACK, potionStacks);
        }
    }

    public static class PotionCharmSubtypes implements ISubtypeInterpreter<ItemStack> {

        public String apply(ItemStack stack, UidContext context) {
            if (context != UidContext.Recipe) {
                if (!PotionCharmItem.hasEffect(stack)) {
                    return "";
                }
                MobEffectInstance contained = PotionCharmItem.getEffect(stack);
                return contained.getEffect().getKey().location() + "@" + contained.getAmplifier() + "@" + contained.getDuration();
            }
            return "";
        }

        @Override
        public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
            String data = apply(ingredient, context);
            return data.isEmpty() ? null : data;
        }

        @Override
        public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
            return apply(ingredient, context);
        }

    }

}
