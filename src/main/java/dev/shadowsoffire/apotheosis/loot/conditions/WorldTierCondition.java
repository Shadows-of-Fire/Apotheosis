package dev.shadowsoffire.apotheosis.loot.conditions;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

/**
 * Checks the {@link WorldTier} of the player in context, and returns true if the tier is in the target set.
 */
public record WorldTierCondition(Set<WorldTier> tiers) implements LootItemCondition {

    public static final MapCodec<WorldTierCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst
        .group(
            PlaceboCodecs.setOf(WorldTier.CODEC).fieldOf("tiers").forGetter(WorldTierCondition::tiers))
        .apply(inst, WorldTierCondition::new));

    @Override
    public LootItemConditionType getType() {
        return Apoth.LootConditions.HAS_WORLD_TIER;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.ATTACKING_ENTITY);
    }

    @Override
    public boolean test(LootContext ctx) {
        var gCtx = GenContext.forLoot(ctx);
        return gCtx != null && this.tiers.contains(gCtx.tier());
    }

    public static LootItemCondition.Builder onlyInTiers(WorldTier... tiers) {
        var set = new LinkedHashSet<WorldTier>();
        for (WorldTier tier : tiers) {
            set.add(tier);
        }
        return () -> new WorldTierCondition(set);
    }
}
