package dev.shadowsoffire.apotheosis.socket;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.util.ApothSmithingRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

public class AddSocketsRecipe extends ApothSmithingRecipe {

    public static final MapCodec<AddSocketsRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        Ingredient.CODEC.fieldOf("input").forGetter(AddSocketsRecipe::getInput),
        Codec.intRange(0, 16).fieldOf("max_sockets").forGetter(AddSocketsRecipe::getMaxSockets))
        .apply(inst, AddSocketsRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AddSocketsRecipe> STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, AddSocketsRecipe::getInput,
        ByteBufCodecs.VAR_INT, AddSocketsRecipe::getMaxSockets,
        AddSocketsRecipe::new);

    private final Ingredient input;
    private final int maxSockets;

    public AddSocketsRecipe(Ingredient input, int maxSockets) {
        super(BASE_PLACEHOLDER, input, ItemStack.EMPTY);
        this.input = input;
        this.maxSockets = maxSockets;
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @Override
    public boolean matches(SmithingRecipeInput inv, Level level) {
        ItemStack in = inv.getItem(BASE);
        return !LootCategory.forItem(in).isNone() && SocketHelper.getSockets(in) < this.getMaxSockets() && this.getInput().test(inv.getItem(ADDITION));
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    @Override
    public ItemStack assemble(SmithingRecipeInput inv) {
        ItemStack out = inv.getItem(BASE).copy();
        if (out.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int sockets = SocketHelper.getSockets(out) + 1;
        SocketHelper.setSockets(out, sockets);
        return out;
    }

    @Override
    public RecipeSerializer<? extends net.minecraft.world.item.crafting.SmithingRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public Ingredient getInput() {
        return this.input;
    }

    public int getMaxSockets() {
        return this.maxSockets;
    }

    public static final RecipeSerializer<AddSocketsRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
}
