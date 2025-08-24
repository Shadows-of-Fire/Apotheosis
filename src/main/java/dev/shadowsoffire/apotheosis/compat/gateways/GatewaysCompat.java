package dev.shadowsoffire.apotheosis.compat.gateways;

import java.util.function.Supplier;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.compat.gateways.tiered_gate.TieredGateway;
import dev.shadowsoffire.apotheosis.compat.gateways.tiered_gate.TieredGatewayEntity;
import dev.shadowsoffire.gateways.client.GatewayRenderer;
import dev.shadowsoffire.gateways.gate.GatewayRegistry;
import dev.shadowsoffire.gateways.gate.Reward;
import dev.shadowsoffire.gateways.gate.WaveEntity;
import dev.shadowsoffire.gateways.gate.WaveModifier;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;

public class GatewaysCompat {

    public static final DeferredHelper R = DeferredHelper.create(Apotheosis.MODID);

    public static final Supplier<EntityType<TieredGatewayEntity>> TIERED_GATEWAY = R.entity("tiered_gateway", () -> EntityType.Builder
        .<TieredGatewayEntity>of(TieredGatewayEntity::new, MobCategory.MISC)
        .setTrackingRange(5)
        .setUpdateInterval(20)
        .sized(2F, 3F)
        .build("gateway"));

    public static void register(IEventBus bus) {
        WaveEntity.CODEC.register(Apotheosis.loc("invader"), InvaderWaveEntity.CODEC);
        WaveEntity.CODEC.register(Apotheosis.loc("elite"), EliteWaveEntity.CODEC);
        WaveEntity.CODEC.register(Apotheosis.loc("true_random_invader"), TrueRandomInvaderWaveEntity.CODEC);
        Reward.CODEC.register(Apotheosis.loc("affix_item"), AffixItemReward.CODEC);
        Reward.CODEC.register(Apotheosis.loc("gem"), GemReward.CODEC);
        Reward.CODEC.register(Apotheosis.loc("true_random_gem"), TrueRandomGemReward.CODEC);
        WaveModifier.CODEC.register(Apotheosis.loc("affix"), AffixWaveModifier.CODEC);
        WaveModifier.CODEC.register(Apotheosis.loc("passenger"), PassengerWaveModifier.CODEC);
        GatewayRegistry.INSTANCE.registerCodec(Apotheosis.loc("tiered"), TieredGateway.CODEC);
        bus.register(R);
        if (FMLEnvironment.dist.isClient()) {
            bus.register(ClientInternal.class);
        }
    }

    private static class ClientInternal {

        @SubscribeEvent
        public static void eRenders(RegisterRenderers e) {
            e.registerEntityRenderer(TIERED_GATEWAY.get(), GatewayRenderer::new);
        }

    }

}
