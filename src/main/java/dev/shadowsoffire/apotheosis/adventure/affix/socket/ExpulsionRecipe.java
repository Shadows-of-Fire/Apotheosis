package dev.shadowsoffire.apotheosis.adventure.affix.socket;

import java.util.Collections;

import com.google.gson.JsonObject;

import dev.shadowsoffire.apotheosis.adventure.Adventure.Items;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule.ApothSmithingRecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class ExpulsionRecipe extends ApothSmithingRecipe {

    private static final ResourceLocation ID = new ResourceLocation("apotheosis:expulsion");

    public ExpulsionRecipe() {
        super(ID, Ingredient.EMPTY, Ingredient.of(Items.VIAL_OF_EXPULSION.get()), ItemStack.EMPTY);
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @Override
    public boolean matches(Container pInv, Level pLevel) {
        return pInv.getItem(ADDITION).getItem() == Items.VIAL_OF_EXPULSION.get() && SocketHelper.getGems(pInv.getItem(BASE)).stream().anyMatch(i -> !i.isEmpty());
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    @Override
    public ItemStack assemble(Container pInv, RegistryAccess regs) {
        ItemStack out = pInv.getItem(BASE).copy();
        if (out.isEmpty()) return ItemStack.EMPTY;
        SocketHelper.setGems(out, Collections.emptyList());
        return out;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<ExpulsionRecipe> {

        public static Serializer INSTANCE = new Serializer();

        @Override
        public ExpulsionRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
            return new ExpulsionRecipe();
        }

        @Override
        public ExpulsionRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            return new ExpulsionRecipe();
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, ExpulsionRecipe pRecipe) {

        }
    }
}
