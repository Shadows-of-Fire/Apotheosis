package dev.shadowsoffire.apotheosis.advancements.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.affix.ItemAffixes;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;

public class AffixItemPredicate implements SingleComponentItemPredicate<ItemAffixes>, TypeAwareDCP<AffixItemPredicate> {

    public static final AffixItemPredicate INSTANCE = new AffixItemPredicate();
    public static final Codec<AffixItemPredicate> CODEC = MapCodec.unit(INSTANCE).codec();

    @Override
    public DataComponentType<ItemAffixes> componentType() {
        return Components.AFFIXES;
    }

    @Override
    public boolean matches(ItemAffixes value) {
        return !value.isEmpty();
    }

    @Override
    public DataComponentPredicate.Type<AffixItemPredicate> type() {
        return Apoth.DataComponentPredicates.AFFIXED_ITEM;
    }

}
