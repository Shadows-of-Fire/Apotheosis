package dev.shadowsoffire.apotheosis.compat.gateways;

import java.util.Set;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.AffixLootEntry;
import dev.shadowsoffire.apotheosis.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
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

public class AffixItemReward implements Reward {

    public static final Codec<AffixItemReward> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        PlaceboCodecs.setOf(RarityRegistry.INSTANCE.holderCodec()).optionalFieldOf("rarities", Set.of()).forGetter(a -> a.rarities),
        PlaceboCodecs.setOf(AffixLootRegistry.INSTANCE.holderCodec()).optionalFieldOf("entries", Set.of()).forGetter(a -> a.entries))
        .apply(inst, AffixItemReward::new));

    private final Set<DynamicHolder<LootRarity>> rarities;
    private final Set<DynamicHolder<AffixLootEntry>> entries;

    private transient boolean validated = false;

    protected AffixItemReward(Set<DynamicHolder<LootRarity>> rarities, Set<DynamicHolder<AffixLootEntry>> entries) {
        this.rarities = rarities;
        this.entries = entries;
    }

    @Override
    public Codec<? extends Reward> getCodec() {
        return CODEC;
    }

    @Override
    public void generateLoot(ServerLevel level, GatewayEntity gate, Player summoner, Consumer<ItemStack> list) {
        if (!this.validated) {
            this.rarities.forEach(AffixItemReward::checkBound);
            this.entries.forEach(AffixItemReward::checkBound);
            this.validated = true;
        }

        GenContext gCtx = GenContext.forPlayer(summoner);
        ItemStack stack = LootController.createAffixItemFromPools(this.rarities, this.entries, gCtx);
        if (!stack.isEmpty()) {
            list.accept(stack);
        }
    }

    @Override
    public void appendHoverText(TooltipContext ctx, Consumer<MutableComponent> list) {
        if (this.rarities.isEmpty()) {
            list.accept(Apotheosis.lang("reward", "random_affix_item"));
        }
        else {
            // Filter the rarities into (%s/.../%s) format
            MutableComponent rarities = this.rarities.stream()
                .filter(DynamicHolder::isBound)
                .map(DynamicHolder::get)
                .map(LootRarity::toComponent)
                .reduce((a, b) -> a.append("/").append(b)).get();
            MutableComponent text = Apotheosis.lang("reward", "affix_item", rarities);
            list.accept(text);
        }
    }

    public static AffixItemReward create(Set<DynamicHolder<LootRarity>> rarities, Set<DynamicHolder<AffixLootEntry>> entries) {
        return new AffixItemReward(rarities, entries);
    }

    @SafeVarargs
    public static AffixItemReward create(DynamicHolder<LootRarity>... rarities) {
        return new AffixItemReward(ApothMiscUtil.linkedSet(rarities), Set.of());
    }

    public static AffixItemReward create() {
        return new AffixItemReward(Set.of(), Set.of());
    }

    private static void checkBound(DynamicHolder<?> holder) {
        if (!holder.isBound()) {
            Apotheosis.LOGGER.error("An AffixItemReward failed to resolve {}!", holder.toString());
        }
    }

}
