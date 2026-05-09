package dev.shadowsoffire.apotheosis.util;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;

/**
 * Slot display companion for {@link SpawnEggIngredient}. Resolves to every registered {@link SpawnEggItem}, so JEI
 * (and other recipe viewers) can rotate through the full spawn-egg list when a recipe accepts the ingredient.
 */
public record SpawnEggSlotDisplay() implements SlotDisplay {

    public static final SpawnEggSlotDisplay INSTANCE = new SpawnEggSlotDisplay();
    public static final MapCodec<SpawnEggSlotDisplay> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, SpawnEggSlotDisplay> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final SlotDisplay.Type<SpawnEggSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        if (!(factory instanceof DisplayContentsFactory.ForStacks<T> stacks)) {
            return Stream.empty();
        }
        return BuiltInRegistries.ITEM.stream()
            .filter(item -> item instanceof SpawnEggItem)
            .map(ItemStack::new)
            .map(stacks::forStack);
    }

    @Override
    public SlotDisplay.Type<SpawnEggSlotDisplay> type() {
        return TYPE;
    }

}
