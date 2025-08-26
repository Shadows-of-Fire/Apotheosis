package dev.shadowsoffire.apotheosis.compat.gateways;

import java.util.Set;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.Reward;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;

public record TrueRandomGemReward(Set<Purity> purities) implements Reward {

    public static final Codec<TrueRandomGemReward> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        PlaceboCodecs.setOf(Purity.CODEC).optionalFieldOf("purities", Set.of()).forGetter(a -> a.purities))
        .apply(inst, TrueRandomGemReward::new));

    @Override
    public Codec<? extends Reward> getCodec() {
        return CODEC;
    }

    @Override
    public void generateLoot(ServerLevel level, GatewayEntity gate, Player summoner, Consumer<ItemStack> list) {
        GenContext gCtx = GenContext.forPlayer(summoner);
        Gem gem = TrueRandomInvaderWaveEntity.getTrulyRandomItem(GemRegistry.INSTANCE, gCtx);
        if (gem == null) {
            Apotheosis.LOGGER.error("Failed to resolve a random gem when generating a TrueRandomGemReward!");
            return;
        }

        Purity purity = Purity.random(gCtx, this.purities);
        ItemStack stack = gem.toStack(purity);
        list.accept(stack);
    }

    @Override
    public void appendHoverText(TooltipContext ctx, Consumer<MutableComponent> list) {
        list.accept(Apotheosis.lang("reward", "true_random_gem"));
    }

    public static TrueRandomGemReward create(Purity... purities) {
        return new TrueRandomGemReward(ApothMiscUtil.linkedSet(purities));
    }

    public static TrueRandomGemReward create() {
        return new TrueRandomGemReward(Set.of());
    }

}
