package dev.shadowsoffire.apotheosis.compat.gateways;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.StandardWaveEntity;
import dev.shadowsoffire.gateways.gate.WaveEntity;
import dev.shadowsoffire.gateways.gate.WaveModifier;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item.TooltipContext;

public record PassengerWaveModifier(WaveEntity entity) implements WaveModifier {

    public static final Codec<PassengerWaveModifier> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            WaveEntity.CODEC.fieldOf("entity").forGetter(PassengerWaveModifier::entity))
        .apply(inst, PassengerWaveModifier::new));

    @Override
    public Codec<? extends WaveModifier> getCodec() {
        return CODEC;
    }

    @Override
    public void apply(LivingEntity entity, GatewayEntity gate) {
        LivingEntity passenger = this.entity.createEntity((ServerLevel) gate.level(), gate);
        if (passenger != null) {
            passenger.startRiding(entity, true);
        }
    }

    @Override
    public void appendHoverText(TooltipContext ctx, Consumer<MutableComponent> list) {

    }

    public static PassengerWaveModifier create(EntityType<?> type, UnaryOperator<StandardWaveEntity.Builder> config) {
        return new PassengerWaveModifier(config.apply(StandardWaveEntity.builder(type)).build());
    }
}
