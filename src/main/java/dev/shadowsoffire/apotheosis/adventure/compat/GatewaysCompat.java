package dev.shadowsoffire.apotheosis.adventure.compat;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.apotheosis.adventure.boss.ApothBoss;
import dev.shadowsoffire.apotheosis.adventure.boss.BossRegistry;
import dev.shadowsoffire.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixLootEntry;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootController;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.Reward;
import dev.shadowsoffire.gateways.gate.WaveEntity;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.IDimensional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class GatewaysCompat {

    public static void register() {
        WaveEntity.CODEC.register(Apotheosis.loc("boss"), BossWaveEntity.CODEC);
        Reward.CODEC.register(Apotheosis.loc("affix"), RarityAffixItemReward.CODEC);
    }

    public static class BossWaveEntity implements WaveEntity {

        public static Codec<BossWaveEntity> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                PlaceboCodecs.nullableField(ResourceLocation.CODEC, "boss").forGetter(b -> b.bossId))
            .apply(inst, BossWaveEntity::new));

        private final Optional<ResourceLocation> bossId;
        private final Supplier<ApothBoss> boss;

        public BossWaveEntity(Optional<ResourceLocation> bossId) {
            this.bossId = bossId;
            this.boss = Suppliers.memoize(() -> bossId.map(BossRegistry.INSTANCE::getValue).orElse(null));
        }

        @Override
        public LivingEntity createEntity(Level level) {
            ApothBoss realBoss = this.bossId.isEmpty() ? BossRegistry.INSTANCE.getRandomItem(level.random) : this.boss.get();
            if (realBoss == null) return null; // error condition
            return realBoss.createBoss((ServerLevelAccessor) level, BlockPos.ZERO, level.random, 0);
        }

        @Override
        public MutableComponent getDescription() {
            return Component.translatable("misc.apotheosis.boss", Component.translatable(this.bossId.isEmpty() ? "misc.apotheosis.random" : this.boss.get().getEntity().getDescriptionId()));
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
            return 1;
        }
    }

    /**
     * Provides a random affix item as a reward.
     */
    public static record RarityAffixItemReward(DynamicHolder<LootRarity> rarity) implements Reward {

        public static Codec<RarityAffixItemReward> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                RarityRegistry.INSTANCE.holderCodec().fieldOf("rarity").forGetter(RarityAffixItemReward::rarity))
            .apply(inst, RarityAffixItemReward::new));

        @Override
        public void generateLoot(ServerLevel level, GatewayEntity gate, Player summoner, Consumer<ItemStack> list) {
            AffixLootEntry entry = AffixLootRegistry.INSTANCE.getRandomItem(level.random, summoner.getLuck(), IDimensional.matches(level), IStaged.matches(summoner));
            if (entry == null) {
                AdventureModule.LOGGER.error("Failed to find an affix loot item for a RarityAffixItemReward executing in dimension {} with rarity {}.", level.dimension(), this.rarity.getId());
                return;
            }
            list.accept(LootController.createLootItem(entry.getStack(), this.rarity.get(), level.random));
        }

        @Override
        public void appendHoverText(Consumer<MutableComponent> list) {
            list.accept(Component.translatable("reward.apotheosis.affix", this.rarity.get().toComponent()));
        }

        @Override
        public Codec<? extends Reward> getCodec() {
            return CODEC;
        }

    }
}
