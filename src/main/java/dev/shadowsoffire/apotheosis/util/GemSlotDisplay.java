package dev.shadowsoffire.apotheosis.util;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.placebo.dynreg.tag.DynamicHolderSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record GemSlotDisplay(DynamicHolderSet<Gem> gems, Purity purity) implements SlotDisplay {

    public static final MapCodec<GemSlotDisplay> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        DynamicHolderSet.codec(GemRegistry.INSTANCE).optionalFieldOf("gems", DynamicHolderSet.empty()).forGetter(GemSlotDisplay::gems),
        Purity.CODEC.fieldOf("purity").forGetter(GemSlotDisplay::purity))
        .apply(inst, GemSlotDisplay::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GemSlotDisplay> STREAM_CODEC = StreamCodec.composite(
        DynamicHolderSet.streamCodec(GemRegistry.INSTANCE), GemSlotDisplay::gems,
        Purity.STREAM_CODEC, GemSlotDisplay::purity,
        GemSlotDisplay::new);

    public static final SlotDisplay.Type<GemSlotDisplay> TYPE = new SlotDisplay.Type<>(CODEC, STREAM_CODEC);

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        if (!(factory instanceof DisplayContentsFactory.ForStacks<T> stacks)) {
            return Stream.empty();
        }
        return GemRegistry.INSTANCE.getValues().stream()
            .filter(gem -> this.gems.size() == 0 || this.gems.contains(gem))
            .map(gem -> GemItem.createStack(gem, this.purity, 1))
            .filter(gem -> GemItem.getPurity(gem) == this.purity) // Filter out gems whose minimum purity is above the specified one.
            .map(stacks::forStack);
    }

    @Override
    public SlotDisplay.Type<GemSlotDisplay> type() {
        return TYPE;
    }

}
