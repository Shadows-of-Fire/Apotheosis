package dev.shadowsoffire.apotheosis.compat.gateways;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.mobs.registries.InvaderRegistry;
import dev.shadowsoffire.apotheosis.mobs.types.Invader;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.Constraints.Constrained;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights.Weighted;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.gateways.Gateways;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.WaveEntity;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * As opposed to {@link InvaderWaveEntity}, this wave entity chooses a random invader ignoring the {@link GenContext}'s restrictions.
 */
public record TrueRandomInvaderWaveEntity(int count, Optional<String> desc) implements WaveEntity {

    public static Codec<TrueRandomInvaderWaveEntity> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.intRange(1, 256).optionalFieldOf("count", 1).forGetter(TrueRandomInvaderWaveEntity::count),
            Codec.STRING.optionalFieldOf("desc").forGetter(TrueRandomInvaderWaveEntity::desc))
        .apply(inst, TrueRandomInvaderWaveEntity::new));

    @Override
    public LivingEntity createEntity(ServerLevel level, GatewayEntity gate) {
        GenContext ctx = GenContext.forPlayer(gate.summonerOrClosest());
        Invader realBoss = getTrulyRandomItem(InvaderRegistry.INSTANCE, ctx);
        if (realBoss == null) {
            Apotheosis.LOGGER.error("Failed to resolve a random invader when generating a TrueRandomInvaderWaveEntity!");
            return null;
        }
        return realBoss.createBoss(level, BlockPos.ZERO, ctx);
    }

    @Override
    public MutableComponent getDescription() {
        Component desc = Apotheosis.lang("wave_entity", "true_random_invader");
        return Gateways.lang("tooltip", "with_count", getCount(), desc);
    }

    @Override
    public boolean shouldFinalizeSpawn() {
        return false;
    }

    @Override
    public Codec<? extends WaveEntity> getCodec() {
        return CODEC;
    }

    @Override
    public int getCount() {
        return this.count;
    }

    public static TrueRandomInvaderWaveEntity createRandom(int count) {
        return new TrueRandomInvaderWaveEntity(count, Optional.empty());
    }

    /**
     * Given a dynamic registry, returns a truly random item from it, ignoring the {@link GenContext}'s restrictions (except the world tier).
     * <p>
     * This method assigns equal weight to any items that have a non-zero weight for the given tier, and are not locked out of that tier by {@link Constraints}.
     */
    @Nullable
    public static <T extends CodecProvider<T> & Weighted & Constrained> T getTrulyRandomItem(DynamicRegistry<T> registry, GenContext ctx) {
        Collection<T> items = registry.getValues();
        List<T> list = new ArrayList<>(items.size());
        for (T item : items) {
            int weight = Math.max(0, item.weights().getWeight(ctx.tier(), ctx.luck()));
            Set<WorldTier> tiers = item.constraints().tiers();
            if (weight > 0 && (tiers.isEmpty() || tiers.contains(ctx.tier()))) {
                list.add(item);
            }
        }

        if (list.isEmpty()) {
            return null;
        }

        return list.get(ctx.rand().nextInt(list.size()));
    }

}
