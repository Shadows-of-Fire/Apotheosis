package dev.shadowsoffire.apotheosis.util;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.UnsocketedGem;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

/**
 * Matches unsocketed gems of the given {@link Purity}. When {@code gems} is non-empty, only the listed gems match.
 */
public record GemIngredient(List<DynamicHolder<Gem>> gems, Purity purity) implements ICustomIngredient {

    public static final MapCodec<GemIngredient> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        GemRegistry.INSTANCE.holderCodec().listOf().optionalFieldOf("gems", List.of()).forGetter(GemIngredient::gems),
        Purity.CODEC.fieldOf("purity").forGetter(GemIngredient::purity))
        .apply(inst, GemIngredient::new));

    public static final StreamCodec<ByteBuf, GemIngredient> STREAM_CODEC = StreamCodec.composite(
        GemRegistry.INSTANCE.holderStreamCodec().apply(ByteBufCodecs.list()), GemIngredient::gems,
        Purity.STREAM_CODEC, GemIngredient::purity,
        GemIngredient::new);

    public static final IngredientType<GemIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

    public GemIngredient(Purity purity) {
        this(List.of(), purity);
    }

    @Override
    public boolean test(ItemStack stack) {
        UnsocketedGem inst = UnsocketedGem.of(stack);
        return inst.isValid() && inst.purity() == this.purity && (this.gems.isEmpty() || this.gems.contains(inst.gem()));
    }

    @Override
    public Stream<ItemStack> getItems() {
        if (GemRegistry.INSTANCE.getValues().size() == 0) {
            return Stream.of(ItemStack.EMPTY);
        }
        return GemRegistry.INSTANCE.getValues().stream()
            .filter(g -> this.purity.isAtLeast(g.getMinPurity()))
            .filter(g -> this.gems.isEmpty() || this.gems.contains(GemRegistry.INSTANCE.holder(g)))
            .map(g -> g.toStack(this.purity));
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
