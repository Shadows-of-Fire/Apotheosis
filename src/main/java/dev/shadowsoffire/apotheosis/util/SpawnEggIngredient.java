package dev.shadowsoffire.apotheosis.util;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

/**
 * Custom ingredient that matches any item that is an instance of {@link SpawnEggItem}, regardless of which entity type
 * the egg targets. Intended for recipes that consume "any spawn egg" — the captured entity type can be read off the
 * input stack at recipe-resolution time via {@link SpawnEggItem#getType(ItemStack)}.
 */
public record SpawnEggIngredient() implements ICustomIngredient {

    public static final SpawnEggIngredient INSTANCE = new SpawnEggIngredient();
    public static final MapCodec<SpawnEggIngredient> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<ByteBuf, SpawnEggIngredient> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final IngredientType<SpawnEggIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

    @Override
    public boolean test(ItemStack stack) {
        return stack.getItem() instanceof SpawnEggItem;
    }

    @Override
    public Stream<ItemStack> getItems() {
        return BuiltInRegistries.ITEM.stream()
            .filter(i -> i instanceof SpawnEggItem)
            .map(ItemStack::new);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }

}
