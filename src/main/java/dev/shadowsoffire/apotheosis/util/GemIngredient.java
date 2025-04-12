package dev.shadowsoffire.apotheosis.util;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.UnsocketedGem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

public record GemIngredient(Purity purity) implements ICustomIngredient {

    public static final MapCodec<GemIngredient> CODEC = Purity.CODEC.fieldOf("purity").xmap(GemIngredient::new, GemIngredient::purity);
    public static final StreamCodec<ByteBuf, GemIngredient> STREAM_CODEC = Purity.STREAM_CODEC.map(GemIngredient::new, GemIngredient::purity);
    public static final IngredientType<GemIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

    @Override
    public boolean test(ItemStack stack) {
        UnsocketedGem inst = UnsocketedGem.of(stack);
        return inst.isValid() && inst.purity() == this.purity;
    }

    @Override
    public Stream<ItemStack> getItems() {
        if (GemRegistry.INSTANCE.getValues().size() == 0) {
            return Stream.of(ItemStack.EMPTY);
        }
        return GemRegistry.INSTANCE.getValues().stream()
            .filter(g -> this.purity.isAtLeast(g.getMinPurity()))
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
