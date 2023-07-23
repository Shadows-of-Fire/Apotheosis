package shadows.apotheosis.adventure.affix.socket;

import java.util.Collections;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.adventure.AdventureModule.ApothUpgradeRecipe;

public class ExpulsionRecipe extends ApothUpgradeRecipe {

    private static final ResourceLocation ID = new ResourceLocation("apotheosis:expulsion");

    public ExpulsionRecipe() {
        super(ID, Ingredient.EMPTY, Ingredient.of(Apoth.Items.VIAL_OF_EXPULSION.get()), ItemStack.EMPTY);
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @Override
    public boolean matches(Container pInv, Level pLevel) {
        return pInv.getItem(1).getItem() == Apoth.Items.VIAL_OF_EXPULSION.get() && SocketHelper.getGems(pInv.getItem(0)).stream().anyMatch(i -> !i.isEmpty());
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    @Override
    public ItemStack assemble(Container pInv) {
        ItemStack out = pInv.getItem(0).copy();
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
