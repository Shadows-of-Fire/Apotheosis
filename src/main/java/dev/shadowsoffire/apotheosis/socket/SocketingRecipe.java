package dev.shadowsoffire.apotheosis.socket;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.util.ApothSmithingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Handles the socketing of items in the Smithing Table.
 */
public class SocketingRecipe extends ApothSmithingRecipe {

    public SocketingRecipe() {
        super(BASE_PLACEHOLDER, Ingredient.of(Apoth.Items.GEM.value()), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(SmithingRecipeInput inv, Level pLevel) {
        ItemStack input = inv.getItem(BASE);
        ItemStack gemStack = inv.getItem(ADDITION);
        return SocketHelper.canSocketGemInItem(input, gemStack);
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput inv) {
        ItemStack input = inv.getItem(BASE);
        ItemStack gemStack = inv.getItem(ADDITION);
        return SocketHelper.socketGemInItem(input, gemStack);
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<? extends net.minecraft.world.item.crafting.SmithingRecipe> getSerializer() {
        return (RecipeSerializer<? extends net.minecraft.world.item.crafting.SmithingRecipe>) Apoth.RecipeSerializers.SOCKETING.value();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
