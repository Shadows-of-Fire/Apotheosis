package dev.shadowsoffire.apotheosis.util;

import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class SingletonRecipeSerializer {

    private SingletonRecipeSerializer() {}

    public static <T extends Recipe<?>> RecipeSerializer<T> create(Supplier<T> factory) {
        T instance = factory.get();
        MapCodec<T> codec = MapCodec.unit(instance);
        StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = StreamCodec.unit(instance);
        return new RecipeSerializer<>(codec, streamCodec);
    }
}
