package dev.shadowsoffire.apotheosis.util;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.UnsocketedGem;
import dev.shadowsoffire.placebo.dynreg.tag.DynamicHolderSet;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

public record GemIngredient(DynamicHolderSet<Gem> gems, Purity purity) implements ICustomIngredient {

    public static final MapCodec<GemIngredient> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        DynamicHolderSet.codec(GemRegistry.INSTANCE).optionalFieldOf("gems", DynamicHolderSet.empty()).forGetter(GemIngredient::gems),
        Purity.CODEC.fieldOf("purity").forGetter(GemIngredient::purity))
        .apply(inst, GemIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GemIngredient> STREAM_CODEC = StreamCodec.composite(
        DynamicHolderSet.streamCodec(GemRegistry.INSTANCE), GemIngredient::gems,
        Purity.STREAM_CODEC, GemIngredient::purity,
        GemIngredient::new);

    public static final IngredientType<GemIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

    @Override
    public boolean test(ItemStack stack) {
        UnsocketedGem inst = UnsocketedGem.of(stack);
        return inst.isValid() && inst.purity() == this.purity && (gems.size() == 0 || gems.contains(inst.gem()));
    }

    @Override
    @SuppressWarnings("deprecation")
    public Stream<Holder<Item>> items() {
        return Stream.of(Apoth.Items.GEM.value().builtInRegistryHolder());
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }

    @Override
    public SlotDisplay display() {
        return new GemSlotDisplay(this.gems, this.purity);
    }

}
