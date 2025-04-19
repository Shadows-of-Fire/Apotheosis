package dev.shadowsoffire.apotheosis.affix;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apoth.Items;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.util.ApothSmithingRecipe;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

public class UnnamingRecipe extends ApothSmithingRecipe {

    public UnnamingRecipe() {
        super(Ingredient.EMPTY, Ingredient.of(Items.SIGIL_OF_UNNAMING.value()), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(SmithingRecipeInput pInv, Level pLevel) {
        ItemStack base = pInv.getItem(BASE);
        return base.has(Components.AFFIX_NAME) && pInv.getItem(ADDITION).is(Items.SIGIL_OF_UNNAMING);
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput pInv, HolderLookup.Provider regs) {
        ItemStack out = pInv.getItem(BASE).copy();
        DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(out);
        if (!rarity.isBound()) {
            return ItemStack.EMPTY;
        }
        // args[1] will be set to the item's underlying name. args[0] will be ignored.
        Component comp = Component.translatable("%2$s", "", "").withStyle(Style.EMPTY.withColor(rarity.get().color()).withItalic(false));
        AffixHelper.setName(out, comp);
        return out;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Apoth.RecipeSerializers.UNNAMING.value();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

}
