package dev.shadowsoffire.apotheosis.adventure.socket;

import com.google.gson.JsonObject;

import dev.shadowsoffire.apotheosis.adventure.Adventure.Items;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule.ApothSmithingRecipe;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class WithdrawalRecipe extends ApothSmithingRecipe implements ReactiveSmithingRecipe {

    private static final ResourceLocation ID = new ResourceLocation("apotheosis:widthdrawal");

    public WithdrawalRecipe() {
        super(ID, Ingredient.EMPTY, Ingredient.of(Items.SIGIL_OF_WITHDRAWAL.get()), ItemStack.EMPTY);
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @Override
    public boolean matches(Container pInv, Level pLevel) {
        ItemStack base = pInv.getItem(BASE);
        ItemStack sigils = pInv.getItem(ADDITION);
        return base.getCount() == 1 && sigils.getItem() == Items.SIGIL_OF_WITHDRAWAL.get() && SocketHelper.getGems(base).stream().anyMatch(GemInstance::isValid);
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    @Override
    public ItemStack assemble(Container pInv, RegistryAccess regs) {
        ItemStack out = pInv.getItem(BASE).copy();
        if (out.isEmpty()) {
            return ItemStack.EMPTY;
        }
        SocketHelper.setGems(out, SocketedGems.EMPTY);
        return out;
    }

    @Override
    public void onCraft(Container inv, Player player, ItemStack output) {
        ItemStack base = inv.getItem(BASE);
        SocketedGems gems = SocketHelper.getGems(base);
        for (int i = 0; i < gems.size(); i++) {
            ItemStack stack = gems.get(i).gemStack();
            if (!stack.isEmpty()) {
                stack.removeTagKey(GemItem.UUID_ARRAY);
                if (!player.addItem(stack)) Block.popResource(player.level(), player.blockPosition(), stack);
            }
        }
        SocketHelper.setGems(base, SocketedGems.EMPTY); // shouldn't be necessary, since base will be deleted, but we do this anyway to safeguard against infinite loops.
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

    public static class Serializer implements RecipeSerializer<WithdrawalRecipe> {

        public static Serializer INSTANCE = new Serializer();

        @Override
        public WithdrawalRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
            return new WithdrawalRecipe();
        }

        @Override
        public WithdrawalRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            return new WithdrawalRecipe();
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, WithdrawalRecipe pRecipe) {

        }
    }

}
