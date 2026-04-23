package dev.shadowsoffire.apotheosis.advancements.predicates;

import net.minecraft.core.component.predicates.DataComponentPredicate;

public interface TypeAwareDCP<T extends DataComponentPredicate> extends DataComponentPredicate {

    DataComponentPredicate.Type<T> type();

}
