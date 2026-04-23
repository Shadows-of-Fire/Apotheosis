package dev.shadowsoffire.apotheosis.advancements.predicates;

import java.util.Set;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;

public record PurityItemPredicate(Set<Purity> purities) implements SingleComponentItemPredicate<Purity>, TypeAwareDCP<PurityItemPredicate> {

    public static final Codec<PurityItemPredicate> CODEC = PlaceboCodecs.setOf(Purity.CODEC).fieldOf("purities").xmap(PurityItemPredicate::new, PurityItemPredicate::purities).codec();

    @Override
    public DataComponentType<Purity> componentType() {
        return Components.PURITY;
    }

    @Override
    public boolean matches(Purity value) {
        return this.purities.contains(value);
    }

    @Override
    public DataComponentPredicate.Type<PurityItemPredicate> type() {
        return Apoth.DataComponentPredicates.ITEM_WITH_PURITY;
    }
}
