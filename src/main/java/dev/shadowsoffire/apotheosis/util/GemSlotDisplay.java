package dev.shadowsoffire.apotheosis.util;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apotheosis.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record GemSlotDisplay(Purity purity) implements SlotDisplay {

    public static final MapCodec<GemSlotDisplay> MAP_CODEC = Purity.CODEC.fieldOf("purity").xmap(GemSlotDisplay::new, GemSlotDisplay::purity);
    public static final StreamCodec<RegistryFriendlyByteBuf, GemSlotDisplay> STREAM_CODEC = Purity.STREAM_CODEC.map(GemSlotDisplay::new, GemSlotDisplay::purity).cast();
    public static final SlotDisplay.Type<GemSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        if (!(factory instanceof DisplayContentsFactory.ForStacks<T> stacks)) {
            return Stream.empty();
        }
        return GemRegistry.INSTANCE.getValues().stream()
            .map(gem -> GemItem.createStack(gem, this.purity, 1))
            .filter(gem -> GemItem.getPurity(gem) == this.purity) // Filter out gems whose minimum purity is above the specified one.
            .map(stacks::forStack);
    }

    @Override
    public SlotDisplay.Type<GemSlotDisplay> type() {
        return TYPE;
    }

}
