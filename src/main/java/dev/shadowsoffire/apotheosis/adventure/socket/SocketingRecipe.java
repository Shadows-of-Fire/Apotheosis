package dev.shadowsoffire.apotheosis.adventure.socket;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import dev.shadowsoffire.apotheosis.adventure.Adventure;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule.ApothSmithingRecipe;
import dev.shadowsoffire.apotheosis.adventure.event.ItemSocketingEvent;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event.Result;

public class SocketingRecipe extends ApothSmithingRecipe {

    private static final ResourceLocation ID = new ResourceLocation("apotheosis:socketing");

    public SocketingRecipe() {
        super(ID, Ingredient.EMPTY, Ingredient.of(Adventure.Items.GEM.get()), ItemStack.EMPTY);
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @Override
    public boolean matches(Container inv, Level pLevel) {
        ItemStack input = inv.getItem(BASE);
        ItemStack gemStack = inv.getItem(ADDITION);
        GemInstance gem = GemInstance.unsocketed(gemStack);
        if (!gem.isValidUnsocketed()) return false;
        if (!SocketHelper.hasEmptySockets(input)) return false;
        var event = new ItemSocketingEvent.CanSocket(input, gemStack);
        MinecraftForge.EVENT_BUS.post(event);
        Result res = event.getResult();
        return res == Result.ALLOW ? true : res == Result.DEFAULT && gem.canApplyTo(input);
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    @Override
    public ItemStack assemble(Container inv, RegistryAccess regs) {
        ItemStack input = inv.getItem(BASE);
        ItemStack gemStack = inv.getItem(ADDITION);

        ItemStack result = input.copy();
        result.setCount(1);
        int socket = SocketHelper.getFirstEmptySocket(result);
        List<GemInstance> gems = new ArrayList<>(SocketHelper.getGems(result).gems());
        ItemStack gemToInsert = gemStack.copy();
        gemToInsert.setCount(1);
        gems.set(socket, GemInstance.socketed(result, gemStack.copy()));
        SocketHelper.setGems(result, new SocketedGems(gems));

        var event = new ItemSocketingEvent.ModifyResult(input, gemToInsert, result);
        MinecraftForge.EVENT_BUS.post(event);
        result = event.getOutput();
        if (result.isEmpty()) throw new IllegalArgumentException("ItemSocketingEvent$ModifyResult produced an empty output stack.");
        return result;
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     */
    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= 2;
    }

    /**
     * Get the result of this recipe, usually for display purposes (e.g. recipe book). If your recipe has more than one
     * possible result (e.g. it's dynamic and depends on its inputs), then return an empty stack.
     */
    @Override
    public ItemStack getResultItem(RegistryAccess regs) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.SMITHING_TABLE);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
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

    public static class Serializer implements RecipeSerializer<SocketingRecipe> {

        public static Serializer INSTANCE = new Serializer();

        @Override
        public SocketingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
            return new SocketingRecipe();
        }

        @Override
        public SocketingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            return new SocketingRecipe();
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, SocketingRecipe pRecipe) {

        }
    }
}
