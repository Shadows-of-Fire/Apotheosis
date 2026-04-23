package dev.shadowsoffire.apotheosis.loot.conditions;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;

import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.util.FakePlayer;

/**
 * Checks that the {@link LootContextParams#ATTACKING_ENTITY attacker} in a loot context is a real player.
 * <p>
 * Fake players (instances of {@link FakePlayer}) will fail this check.
 */
public class KilledByRealPlayerCondition implements LootItemCondition {

    public static final KilledByRealPlayerCondition INSTANCE = new KilledByRealPlayerCondition();
    public static final MapCodec<KilledByRealPlayerCondition> CODEC = MapCodec.unit(INSTANCE);

    private KilledByRealPlayerCondition() {}

    @Override
    public MapCodec<KilledByRealPlayerCondition> codec() {
        return CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.ATTACKING_ENTITY);
    }

    @Override
    public boolean test(LootContext context) {
        Entity attacker = context.getOptionalParameter(LootContextParams.ATTACKING_ENTITY);
        return attacker instanceof Player && !(attacker instanceof FakePlayer);
    }

    public static LootItemCondition.Builder killedByPlayer() {
        return () -> INSTANCE;
    }
}
