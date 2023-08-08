package dev.shadowsoffire.apotheosis.adventure.compat;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.boss.BossItem;
import dev.shadowsoffire.apotheosis.adventure.boss.BossItemManager;
import dev.shadowsoffire.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixLootManager;
import dev.shadowsoffire.apotheosis.adventure.loot.LootController;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.Reward;
import dev.shadowsoffire.gateways.gate.WaveEntity;
import dev.shadowsoffire.placebo.reload.WeightedJsonReloadListener.IDimensional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

public class GatewaysCompat {

    public static void register() {
        WaveEntity.CODECS.put(Apotheosis.loc("boss"), BossWaveEntity.CODEC);
        Reward.CODECS.put(Apotheosis.loc("affix"), RarityAffixItemReward.CODEC);
    }

    public static class BossWaveEntity implements WaveEntity {

        public static Codec<BossWaveEntity> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                ResourceLocation.CODEC.optionalFieldOf("boss").forGetter(b -> b.bossId))
            .apply(inst, BossWaveEntity::new));

        private final Optional<ResourceLocation> bossId;
        private final Supplier<BossItem> boss;

        public BossWaveEntity(Optional<ResourceLocation> bossId) {
            this.bossId = bossId;
            this.boss = Suppliers.memoize(() -> bossId.map(BossItemManager.INSTANCE::getValue).orElse(null));
        }

        @Override
        public LivingEntity createEntity(Level level) {
            BossItem realBoss = this.bossId.isEmpty() ? BossItemManager.INSTANCE.getRandomItem(level.random) : this.boss.get();
            if (realBoss == null) return null; // error condition
            return realBoss.createBoss((ServerLevelAccessor) level, BlockPos.ZERO, level.random, 0);
        }

        @Override
        public Component getDescription() {
            return Component.translatable("misc.apotheosis.boss", Component.translatable(this.bossId.isEmpty() ? "misc.apotheosis.random" : this.boss.get().getEntity().getDescriptionId()));
        }

        @Override
        public AABB getAABB(double x, double y, double z) {
            return this.bossId.isEmpty() ? new AABB(0, 0, 0, 2, 2, 2).move(x, y, z) : this.boss.get().getSize();
        }

        @Override
        public boolean shouldFinalizeSpawn() {
            return false;
        }

        @Override
        public Codec<? extends WaveEntity> getCodec() {
            return CODEC;
        }
    }

    /**
     * Provides a random affix item as a reward.
     */
    public static record RarityAffixItemReward(LootRarity rarity) implements Reward {

        public static Codec<RarityAffixItemReward> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                LootRarity.CODEC.fieldOf("rarity").forGetter(RarityAffixItemReward::rarity))
            .apply(inst, RarityAffixItemReward::new));

        @Override
        public void generateLoot(ServerLevel level, GatewayEntity gate, Player summoner, Consumer<ItemStack> list) {
            list.accept(LootController.createLootItem(AffixLootManager.INSTANCE.getRandomItem(level.random, summoner.getLuck(), IDimensional.matches(level), IStaged.matches(summoner)).getStack(), this.rarity, level.random));
        }

        @Override
        public void appendHoverText(Consumer<Component> list) {
            list.accept(Component.translatable("reward.apotheosis.affix", this.rarity.toComponent()));
        }

        @Override
        public Codec<? extends Reward> getCodec() {
            return CODEC;
        }

    }
}
