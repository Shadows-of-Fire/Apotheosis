package dev.shadowsoffire.apotheosis.data;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.loot.entry.AffixLootPoolEntry;
import dev.shadowsoffire.apotheosis.loot.entry.GemLootPoolEntry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.placebo.loot.StackLootEntry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class ApothLootProvider extends LootTableProvider {

    private ApothLootProvider(PackOutput output, Set<ResourceKey<LootTable>> requiredTables, List<SubProviderEntry> subProviders, CompletableFuture<Provider> registries) {
        super(output, requiredTables, subProviders, registries);
    }

    public static ApothLootProvider create(PackOutput output, CompletableFuture<Provider> registries) {
        return new ApothLootProvider(
            output,
            Set.of(),
            List.of(
                new LootTableProvider.SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(EntityLoot::new, LootContextParamSets.ENTITY),
                new LootTableProvider.SubProviderEntry(ChestLoot::new, LootContextParamSets.CHEST)),
            registries);
    }

    public static class BlockLoot extends BlockLootSubProvider {

        public static final Set<Item> EXPLOSION_RESISTANT = Set.of(
            Apoth.Items.REFORGING_TABLE.value(),
            Apoth.Items.AUGMENTING_TABLE.value());

        protected BlockLoot(Provider registries) {
            super(EXPLOSION_RESISTANT, FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            this.dropSelf(Apoth.Blocks.SIMPLE_REFORGING_TABLE);
            this.dropSelf(Apoth.Blocks.REFORGING_TABLE);
            this.dropSelf(Apoth.Blocks.SALVAGING_TABLE);
            this.dropSelf(Apoth.Blocks.GEM_CUTTING_TABLE);
            this.dropSelf(Apoth.Blocks.AUGMENTING_TABLE);
            this.dropSelf(Apoth.Blocks.GEM_CASE);
            this.dropSelf(Apoth.Blocks.ENDER_GEM_CASE);
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return BuiltInRegistries.BLOCK.holders().filter(h -> Apotheosis.MODID.equals(h.getKey().location().getNamespace())).map(Holder::value).toList();
        }

        protected void dropSelf(Holder<Block> block) {
            this.dropSelf(block.value());
        }

    }

    public static record EntityLoot(HolderLookup.Provider registries) implements LootTableSubProvider {

        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, Builder> output) {
            // Bonus Invader drops, which has a 30% chance to spawn in a gem (compared to the normal 4% chance)
            // Luck increases the drop chance slightly.
            output.accept(Apoth.LootTables.BONUS_BOSS_DROPS,
                LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(GemLootPoolEntry.builder(Set.of(), Set.of()).setWeight(30).setQuality(1))
                        .add(item(Apoth.Items.SIGIL_OF_MALICE.value(), 1, 1).setWeight(25).setQuality(1))
                        .add(EmptyLootItem.emptyItem().setWeight(45))));

            output.accept(Apoth.LootTables.BONUS_RARE_BOSS_DROPS,
                LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(GemLootPoolEntry.builder(Set.of(), Set.of()).setWeight(45).setQuality(1))
                        .add(item(Apoth.Items.SIGIL_OF_MALICE.value(), 1, 1).setWeight(30).setQuality(1))
                        .add(TagEntry.expandTag(Apoth.Tags.BOSS_MUSIC_DISCS).setWeight(5))
                        .add(EmptyLootItem.emptyItem().setWeight(20))));

            DynamicHolder<LootRarity> rare = RarityRegistry.INSTANCE.holder(Apotheosis.loc("rare"));
            DynamicHolder<LootRarity> epic = RarityRegistry.INSTANCE.holder(Apotheosis.loc("epic"));
            DynamicHolder<LootRarity> mythic = RarityRegistry.INSTANCE.holder(Apotheosis.loc("mythic"));

            output.accept(Apoth.LootTables.TREASURE_GOBLIN,
                LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(2, 3))
                        .setBonusRolls(UniformGenerator.between(0, 0.25F))
                        .add(potion(Potions.STRONG_REGENERATION).setWeight(20).setQuality(0))
                        .add(potion(Potions.STRONG_SWIFTNESS).setWeight(20).setQuality(0))
                        .add(potion(Potions.LONG_FIRE_RESISTANCE).setWeight(20).setQuality(0))
                        .add(potion(Potions.STRONG_HEALING).setWeight(20).setQuality(0))
                        .add(potion(Potions.LONG_NIGHT_VISION).setWeight(20).setQuality(0))
                        .add(potion(Potions.LONG_STRENGTH).setWeight(20).setQuality(0))
                        .add(potion(Potions.LONG_INVISIBILITY).setWeight(20).setQuality(0))
                        .add(potion(Potions.LONG_WATER_BREATHING).setWeight(20).setQuality(0))
                        .add(potion(ALObjects.Potions.LONG_KNOWLEDGE).setWeight(10).setQuality(1))
                        .add(potion(ALObjects.Potions.STRONG_RESISTANCE).setWeight(10).setQuality(1))
                        .add(potion(ALObjects.Potions.EXTRA_LONG_FLYING).setWeight(1).setQuality(5)))
                    .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(4, 7))
                        .setBonusRolls(UniformGenerator.between(0, 0.05F))
                        .add(item(Items.DIAMOND, 1, 3).setWeight(30).setQuality(4))
                        .add(item(Items.EMERALD, 1, 3).setWeight(30).setQuality(4))
                        .add(item(Items.IRON_INGOT, 2, 5).setWeight(100).setQuality(0))
                        .add(item(Items.GOLD_INGOT, 2, 5).setWeight(100).setQuality(0))
                        .add(item(Items.ENCHANTED_GOLDEN_APPLE, 1, 1).setWeight(1).setQuality(15))
                        .add(item(Items.NAME_TAG, 1, 2).setWeight(50).setQuality(0))
                        .add(item(Items.LEAD, 1, 2).setWeight(50).setQuality(0))
                        .add(item(Items.SADDLE, 1, 2).setWeight(40).setQuality(0))
                        .add(item(Items.DIAMOND_HORSE_ARMOR, 1, 1).setWeight(40).setQuality(3))
                        .add(enchanted(Items.BOOK, registries).setWeight(40).setQuality(5))
                        .add(GemLootPoolEntry.builder(Set.of(), Set.of()).setWeight(80).setQuality(3))
                        .add(GemLootPoolEntry.builder(Set.of(), Set.of()).setWeight(80).setQuality(3))
                        .add(AffixLootPoolEntry.builder(ApothMiscUtil.linkedSet(rare), Set.of()).setWeight(60).setQuality(2))
                        .add(AffixLootPoolEntry.builder(ApothMiscUtil.linkedSet(epic), Set.of()).setWeight(40).setQuality(3))
                        .add(AffixLootPoolEntry.builder(ApothMiscUtil.linkedSet(mythic), Set.of()).setWeight(10).setQuality(5))
                        .add(AffixLootPoolEntry.builder(Set.of(), Set.of()).setWeight(100).setQuality(2))));
        }

    }

    public static record ChestLoot(HolderLookup.Provider registries) implements LootTableSubProvider {

        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, Builder> output) {

            DynamicHolder<LootRarity> uncommon = RarityRegistry.INSTANCE.holder(Apotheosis.loc("uncommon"));
            DynamicHolder<LootRarity> rare = RarityRegistry.INSTANCE.holder(Apotheosis.loc("rare"));
            DynamicHolder<LootRarity> epic = RarityRegistry.INSTANCE.holder(Apotheosis.loc("epic"));

            output.accept(Apoth.LootTables.CHEST_VALUABLE,
                LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(3, 5))
                        .setBonusRolls(UniformGenerator.between(0, 0.05F))
                        .add(potion(Potions.STRONG_REGENERATION).setWeight(20).setQuality(0))
                        .add(potion(Potions.STRONG_SWIFTNESS).setWeight(20).setQuality(0))
                        .add(potion(Potions.LONG_FIRE_RESISTANCE).setWeight(20).setQuality(0))
                        .add(potion(Potions.STRONG_HEALING).setWeight(20).setQuality(0))
                        .add(potion(Potions.LONG_NIGHT_VISION).setWeight(20).setQuality(0))
                        .add(potion(Potions.LONG_STRENGTH).setWeight(20).setQuality(0))
                        .add(potion(Potions.LONG_INVISIBILITY).setWeight(20).setQuality(0))
                        .add(potion(Potions.LONG_WATER_BREATHING).setWeight(20).setQuality(0))
                        .add(potion(ALObjects.Potions.LONG_KNOWLEDGE).setWeight(10).setQuality(1))
                        .add(potion(ALObjects.Potions.STRONG_RESISTANCE).setWeight(10).setQuality(1))
                        .add(potion(ALObjects.Potions.EXTRA_LONG_FLYING).setWeight(1).setQuality(5)))
                    .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(3, 7))
                        .setBonusRolls(UniformGenerator.between(0, 0.05F))
                        .add(item(Items.DIAMOND, 1, 3).setWeight(30).setQuality(4))
                        .add(item(Items.EMERALD, 1, 3).setWeight(30).setQuality(4))
                        .add(item(Items.IRON_INGOT, 2, 5).setWeight(100).setQuality(0))
                        .add(item(Items.GOLD_INGOT, 2, 5).setWeight(100).setQuality(0))
                        .add(item(Items.ENCHANTED_GOLDEN_APPLE, 1, 1).setWeight(1).setQuality(15))
                        .add(item(Items.NAME_TAG, 1, 2).setWeight(50).setQuality(0))
                        .add(item(Items.LEAD, 1, 2).setWeight(50).setQuality(0))
                        .add(item(Items.SADDLE, 1, 2).setWeight(40).setQuality(0))
                        .add(item(Items.DIAMOND_HORSE_ARMOR, 1, 1).setWeight(40).setQuality(3))
                        .add(item(Items.SLIME_BALL, 3, 6).setWeight(50).setQuality(0))
                        .add(item(Items.BUCKET, 1, 2).setWeight(50).setQuality(0))
                        .add(enchanted(Items.DIAMOND_SWORD, registries).setWeight(30).setQuality(5))
                        .add(enchanted(Items.DIAMOND_AXE, registries).setWeight(30).setQuality(5))
                        .add(enchanted(Items.DIAMOND_PICKAXE, registries).setWeight(30).setQuality(5))
                        .add(enchanted(Items.DIAMOND_BOOTS, registries).setWeight(20).setQuality(5))
                        .add(enchanted(Items.DIAMOND_LEGGINGS, registries).setWeight(20).setQuality(5))
                        .add(enchanted(Items.DIAMOND_CHESTPLATE, registries).setWeight(20).setQuality(5))
                        .add(enchanted(Items.DIAMOND_HELMET, registries).setWeight(20).setQuality(5))
                        .add(enchanted(Items.BOOK, registries).setWeight(20).setQuality(5))
                        .add(GemLootPoolEntry.builder(Set.of(Purity.FLAWED), Set.of()).setWeight(20).setQuality(15))
                        .add(AffixLootPoolEntry.builder(ApothMiscUtil.linkedSet(rare, epic), Set.of()).setWeight(20).setQuality(15))));

            output.accept(Apoth.LootTables.SPAWNER_BRUTAL,
                LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(5, 8))
                        .setBonusRolls(UniformGenerator.between(0, 0.05F))
                        .add(item(Items.SKELETON_SKULL, 1, 1).setWeight(1).setQuality(2))
                        .add(item(Items.WITHER_SKELETON_SKULL, 1, 1).setWeight(1).setQuality(2))
                        .add(item(Items.CREEPER_HEAD, 1, 1).setWeight(1).setQuality(2))
                        .add(item(Items.ZOMBIE_HEAD, 1, 1).setWeight(1).setQuality(2))
                        .add(item(Items.PLAYER_HEAD, 1, 1).setWeight(1).setQuality(2))
                        .add(item(Items.TNT, 1, 4).setWeight(2).setQuality(0))
                        .add(item(Items.DIAMOND, 1, 3).setWeight(3).setQuality(5))
                        .add(item(Items.EMERALD, 1, 3).setWeight(3).setQuality(5))
                        .add(item(Items.IRON_INGOT, 2, 5).setWeight(10).setQuality(3))
                        .add(item(Items.GOLD_INGOT, 3, 6).setWeight(10).setQuality(3))
                        .add(item(Items.GOLDEN_APPLE, 1, 1).setWeight(1).setQuality(3))
                        .add(item(Items.NAME_TAG, 1, 1).setWeight(1).setQuality(3))
                        .add(item(Items.LEAD, 1, 1).setWeight(1).setQuality(3))
                        .add(item(Items.SADDLE, 1, 1).setWeight(1).setQuality(3))
                        .add(item(Items.DIAMOND_HORSE_ARMOR, 1, 1).setWeight(1).setQuality(5))
                        .add(item(Items.SLIME_BALL, 1, 3).setWeight(3).setQuality(3))
                        .add(item(Items.BUCKET, 1, 1).setWeight(3).setQuality(2))
                        .add(item(Items.ANVIL, 1, 1).setWeight(3).setQuality(5))
                        .add(item(Items.ENCHANTING_TABLE, 1, 1).setWeight(3).setQuality(5))
                        .add(item(Items.IRON_BLOCK, 1, 1).setWeight(3).setQuality(2))
                        .add(enchanted(Items.BOOK, registries).setWeight(2).setQuality(5))));

            output.accept(Apoth.LootTables.SPAWNER_SWARM,
                LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(5, 6))
                        .setBonusRolls(UniformGenerator.between(0, 0.05F))
                        .add(item(Items.CREEPER_SPAWN_EGG, 1, 3).setWeight(1).setQuality(2))
                        .add(item(Items.SKELETON_SPAWN_EGG, 1, 3).setWeight(1).setQuality(2))
                        .add(item(Items.SPIDER_SPAWN_EGG, 1, 3).setWeight(1).setQuality(2))
                        .add(item(Items.ZOMBIE_SPAWN_EGG, 1, 3).setWeight(1).setQuality(2))
                        .add(item(Items.SLIME_SPAWN_EGG, 1, 3).setWeight(1).setQuality(2))
                        .add(item(Items.ENDERMAN_SPAWN_EGG, 1, 3).setWeight(1).setQuality(2))
                        .add(item(Items.CAVE_SPIDER_SPAWN_EGG, 1, 3).setWeight(1).setQuality(2))
                        .add(item(Items.SILVERFISH_SPAWN_EGG, 1, 3).setWeight(1).setQuality(2))
                        .add(item(Items.DIAMOND, 1, 3).setWeight(3).setQuality(5))
                        .add(item(Items.EMERALD, 1, 3).setWeight(3).setQuality(5))
                        .add(item(Items.IRON_INGOT, 2, 5).setWeight(10).setQuality(3))
                        .add(item(Items.GOLD_INGOT, 2, 5).setWeight(10).setQuality(3))
                        .add(item(Items.GOLDEN_APPLE, 1, 1).setWeight(1).setQuality(3))
                        .add(item(Items.NAME_TAG, 1, 1).setWeight(1).setQuality(3))
                        .add(item(Items.LEAD, 1, 1).setWeight(1).setQuality(3))
                        .add(item(Items.SADDLE, 1, 1).setWeight(1).setQuality(3))
                        .add(item(Items.DIAMOND_HORSE_ARMOR, 1, 1).setWeight(1).setQuality(5))
                        .add(item(Items.SLIME_BALL, 1, 3).setWeight(3).setQuality(3))
                        .add(item(Items.BUCKET, 1, 1).setWeight(3).setQuality(2))
                        .add(item(Items.ANVIL, 1, 1).setWeight(3).setQuality(5))
                        .add(item(Items.OBSIDIAN, 3, 8).setWeight(3).setQuality(5))
                        .add(item(Items.IRON_BLOCK, 1, 1).setWeight(3).setQuality(2))
                        .add(enchanted(Items.BOOK, registries).setWeight(2).setQuality(5))));

            output.accept(Apoth.LootTables.TOME_TOWER,
                LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(5, 9))
                        .setBonusRolls(UniformGenerator.between(0, 0.05F))
                        .add(item(Items.DIAMOND, 1, 3).setWeight(30).setQuality(2))
                        .add(item(Items.EMERALD, 1, 3).setWeight(30).setQuality(2))
                        .add(item(Items.IRON_INGOT, 2, 5).setWeight(50).setQuality(0))
                        .add(item(Items.GOLD_INGOT, 2, 5).setWeight(50).setQuality(0))
                        .add(item(Items.NAME_TAG, 1, 2).setWeight(20).setQuality(0))
                        .add(item(Items.LEAD, 1, 2).setWeight(20).setQuality(0))
                        .add(item(Items.SADDLE, 1, 2).setWeight(20).setQuality(0))
                        .add(item(Items.DIAMOND_HORSE_ARMOR, 1, 1).setWeight(1).setQuality(5))
                        .add(item(Items.SLIME_BALL, 3, 6).setWeight(20).setQuality(0))
                        .add(item(Items.BUCKET, 1, 1).setWeight(3).setQuality(0))
                        .add(enchanted(Items.BOOK, registries).setWeight(400).setQuality(0))
                        .add(GemLootPoolEntry.builder(Set.of(Purity.FLAWED), Set.of()).setWeight(80).setQuality(5))
                        .add(AffixLootPoolEntry.builder(ApothMiscUtil.linkedSet(uncommon, rare, epic), Set.of()).setWeight(80).setQuality(5))));
        }

    }

    private static StackEntryBuilder enchanted(Item item, HolderLookup.Provider registries) {
        return new StackEntryBuilder(item.getDefaultInstance()).apply(EnchantRandomlyFunction.randomApplicableEnchantment(registries));
    }

    private static StackEntryBuilder item(Item item, int min, int max) {
        return new StackEntryBuilder(item.getDefaultInstance()).count(min, max);
    }

    private static StackEntryBuilder potion(Holder<Potion> potion) {
        return new StackEntryBuilder(PotionContents.createItemStack(Items.POTION, potion));
    }

    public static class StackEntryBuilder extends LootPoolSingletonContainer.Builder<StackEntryBuilder> {

        protected final ItemStack stack;
        protected int min = -1, max = -1;

        public StackEntryBuilder(ItemStack stack) {
            this.stack = stack;
        }

        public StackEntryBuilder count(int min, int max) {
            this.min = min;
            this.max = max;
            return this;
        }

        @Override
        protected StackEntryBuilder getThis() {
            return this;
        }

        @Override
        public LootPoolEntryContainer build() {
            if (this.min == -1) {
                this.min = stack.getCount();
            }
            if (this.max == -1) {
                this.max = stack.getCount();
            }
            return new StackLootEntry(this.stack, this.min, this.max, this.weight, this.quality, this.getConditions(), this.getFunctions());
        }

    }
}
