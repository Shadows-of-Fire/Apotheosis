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
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;

public class GemReward implements Reward {

    public static final Codec<GemReward> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        PlaceboCodecs.setOf(Purity.CODEC).optionalFieldOf("purities", Set.of()).forGetter(a -> a.purities),
        PlaceboCodecs.setOf(GemRegistry.INSTANCE.holderCodec()).optionalFieldOf("gems", Set.of()).forGetter(a -> a.gems))
        .apply(inst, GemReward::new));

    private final Set<Purity> purities;
    private final Set<DynamicHolder<Gem>> gems;

    private transient boolean validated = false;

    protected GemReward(Set<Purity> purities, Set<DynamicHolder<Gem>> gems) {
        this.purities = purities;
        this.gems = gems;
    }

    @Override
    public Codec<? extends Reward> getCodec() {
        return CODEC;
    }

    @Override
    public void generateLoot(ServerLevel level, GatewayEntity gate, Player summoner, Consumer<ItemStack> list) {
        if (!this.validated) {
            this.gems.forEach(GemReward::checkBound);
            this.validated = true;
        }

        GenContext gCtx = GenContext.forPlayer(summoner);
        Gem gem;

        if (!this.gems.isEmpty()) {
            gem = GemRegistry.INSTANCE.getRandomItemFromHolders(gCtx, this.gems);
        }
        else {
            gem = GemRegistry.INSTANCE.getRandomItem(gCtx);
        }

        Purity purity = Purity.random(gCtx, this.purities);
        ItemStack stack = gem.toStack(purity);
        list.accept(stack);
    }

    @Override
    public void appendHoverText(TooltipContext ctx, Consumer<MutableComponent> list) {
        // TODO: Maybe interpret the translation key differently if only a single Gem is provided
        if (this.purities.isEmpty()) {
            list.accept(Apotheosis.lang("reward", "random_gem"));
        }
        else {
            // Filter the purities into (%s/.../%s) format
            MutableComponent rarities = this.purities.stream()
                .map(Purity::toComponent)
                .reduce((a, b) -> a.append("/").append(b)).get();
            MutableComponent text = Apotheosis.lang("reward", "gem", rarities);
            list.accept(text);
        }
    }

    public static GemReward create(Set<Purity> purities, Set<DynamicHolder<Gem>> gems) {
        return new GemReward(purities, gems);
    }

    public static GemReward create(Purity... purities) {
        return new GemReward(ApothMiscUtil.linkedSet(purities), Set.of());
    }

    public static GemReward create() {
        return new GemReward(Set.of(), Set.of());
    }

    private static void checkBound(DynamicHolder<?> holder) {
        if (!holder.isBound()) {
            Apotheosis.LOGGER.error("A GemReward failed to resolve {}!", holder.toString());
        }
    }

}
