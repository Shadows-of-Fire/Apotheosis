package dev.shadowsoffire.apotheosis.compat.gateways;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.mobs.registries.EliteRegistry;
import dev.shadowsoffire.apotheosis.mobs.types.Elite;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.WaveEntity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * An EliteWaveEntity is a wrapper wave entity that spawns a normal wave entity, and then applies a target Apothic Elite to that entity.
 * <p>
 * The specified elite does not need to be compatible with the entity type of the original wave entity, and will try to apply itself regardless.
 * <p>
 * However, elites can only be applied to {@link Mob}, so the base entity must be a mob or an error will occur.
 */
public record EliteWaveEntity(WaveEntity base, DynamicHolder<Elite> elite, Optional<String> desc) implements WaveEntity {

    public static Codec<EliteWaveEntity> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            WaveEntity.CODEC.fieldOf("base_entity").forGetter(EliteWaveEntity::base),
            EliteRegistry.INSTANCE.holderCodec().fieldOf("elite").forGetter(EliteWaveEntity::elite),
            Codec.STRING.optionalFieldOf("desc").forGetter(EliteWaveEntity::desc))
        .apply(inst, EliteWaveEntity::new));

    @Override
    public LivingEntity createEntity(ServerLevel level, GatewayEntity gate) {
        LivingEntity baseEntity = this.base.createEntity(level, gate);
        if (baseEntity == null) {
            return null;
        }

        if (!this.elite.isBound()) {
            Apotheosis.LOGGER.error("An EliteWaveEntity has an ubound elite holder {}!", this.elite);
            return null;
        }

        if (baseEntity instanceof Mob mob) {
            mob.getPersistentData().putString(Elite.MINIBOSS_KEY, this.elite.getId().toString());
            mob.getPersistentData().putString(Elite.PLAYER_KEY, gate.summonerOrClosest().getUUID().toString());
            return baseEntity;
        }
        else {
            Apotheosis.LOGGER.error("An EliteWaveEntity tried to apply an elite to a non-Mob entity: {}!", baseEntity);
            return baseEntity;
        }
    }

    @Override
    public MutableComponent getDescription() {
        if (this.desc.isPresent()) {
            return Component.translatable(desc.get(), this.base.getDescription());
        }
        return Apotheosis.lang("wave_entity", "elite", this.base.getDescription());
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
        return this.base.getCount();
    }

}
