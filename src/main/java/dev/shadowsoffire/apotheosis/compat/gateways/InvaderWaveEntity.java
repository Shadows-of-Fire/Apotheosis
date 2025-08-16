package dev.shadowsoffire.apotheosis.compat.gateways;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.mobs.registries.InvaderRegistry;
import dev.shadowsoffire.apotheosis.mobs.types.Invader;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.gateways.Gateways;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.WaveEntity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * The InvaderWaveEntity spawns one or more Apothic Invaders as a gateway entity.
 * <p>
 * If a specific invader is not provided, a random invader will be chosen based on the world tier.
 */
public record InvaderWaveEntity(DynamicHolder<Invader> invader, int count, Optional<String> desc) implements WaveEntity {

    public static Codec<InvaderWaveEntity> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            InvaderRegistry.INSTANCE.holderCodec().optionalFieldOf("invader", InvaderRegistry.INSTANCE.emptyHolder()).forGetter(InvaderWaveEntity::invader),
            Codec.intRange(1, 256).optionalFieldOf("count", 1).forGetter(InvaderWaveEntity::count),
            Codec.STRING.optionalFieldOf("desc").forGetter(InvaderWaveEntity::desc))
        .apply(inst, InvaderWaveEntity::new));

    @Override
    public LivingEntity createEntity(ServerLevel level, GatewayEntity gate) {
        GenContext ctx = GenContext.forPlayer(gate.summonerOrClosest());
        Invader realBoss = resolveInvader(ctx);
        if (realBoss == null) {
            if (usingRandomInvader()) {
                Apotheosis.LOGGER.error("Failed to resolve a random invader when generating an InvaderWaveEntity!");
            }
            else {
                String type = this.invader.getId().toString();
                Apotheosis.LOGGER.error("Failed to resolve the invader '{}' when generating an InvaderWaveEntity!", type);
            }
            return null;
        }
        return realBoss.createBoss(level, BlockPos.ZERO, ctx);
    }

    @Override
    public MutableComponent getDescription() {
        Component desc = Apotheosis.lang("wave_entity", "invader", Component.translatable(this.desc.orElse(resolveInvaderDesc(this.invader))));
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

    protected boolean usingRandomInvader() {
        return this.invader.equals(InvaderRegistry.INSTANCE.emptyHolder());
    }

    @Nullable
    protected Invader resolveInvader(GenContext ctx) {
        if (this.usingRandomInvader()) {
            return InvaderRegistry.INSTANCE.getRandomItem(ctx);
        }

        return this.invader().getOptional().orElse(null);
    }

    public static InvaderWaveEntity create(DynamicHolder<Invader> invader, int count, @Nullable String desc) {
        return new InvaderWaveEntity(invader, count, Optional.ofNullable(desc));
    }

    public static InvaderWaveEntity createRandom(int count) {
        return new InvaderWaveEntity(InvaderRegistry.INSTANCE.emptyHolder(), count, Optional.empty());
    }

    private static String resolveInvaderDesc(DynamicHolder<Invader> invader) {
        return invader.isBound() ? invader.get().entity().getDescriptionId() : "misc.apotheosis.random";
    }
}
