package dev.shadowsoffire.apotheosis.data;

import java.util.concurrent.CompletableFuture;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.AffixLootEntry;
import dev.shadowsoffire.apotheosis.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

/**
 * Haven spawns Stone/Leather and Gold/Chain items.
 * Frontier spawns Iron and Gold/Chain items with a low chance for Diamond items.
 * Ascent spawns Iron and Diamond items.
 * Summit spawns Diamond items, with a low chance for Iron or Netherite.
 * Pinnacle spawns mostly Netherite items, with a chance for Diamond.
 */
public class AffixLootEntryProvider extends DynamicRegistryProvider<AffixLootEntry> {

    protected static final TieredWeights STONE = TieredWeights.onlyFor(WorldTier.HAVEN, 25, 0);
    protected static final TieredWeights LEATHER = TieredWeights.onlyFor(WorldTier.HAVEN, 25, 0);
    protected static final TieredWeights CHAIN = TieredWeights.builder()
        .with(WorldTier.HAVEN, 10, 2)
        .with(WorldTier.FRONTIER, 10, 0)
        .build();
    protected static final TieredWeights GOLD = TieredWeights.builder()
        .with(WorldTier.HAVEN, 5, 0)
        .with(WorldTier.FRONTIER, 10, 2)
        .with(WorldTier.ASCENT, 5, 0.25F)
        .build();
    protected static final TieredWeights IRON = TieredWeights.builder()
        .with(WorldTier.FRONTIER, 25, 1)
        .with(WorldTier.ASCENT, 10, 1)
        .with(WorldTier.SUMMIT, 10, 0.25F)
        .build();
    protected static final TieredWeights DIAMOND = TieredWeights.builder()
        .with(WorldTier.ASCENT, 25, 1.25F)
        .with(WorldTier.SUMMIT, 25, 0.25F)
        .with(WorldTier.PINNACLE, 5, 1)
        .build();
    protected static final TieredWeights NETHERITE = TieredWeights.builder()
        .with(WorldTier.SUMMIT, 5, 0.75F)
        .with(WorldTier.PINNACLE, 25, 2)
        .build();
    protected static final TieredWeights TRIDENT = TieredWeights.builder()
        .with(WorldTier.ASCENT, 5, 1)
        .with(WorldTier.SUMMIT, 7, 1)
        .with(WorldTier.PINNACLE, 7, 1)
        .build();

    public AffixLootEntryProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries, AffixLootRegistry.INSTANCE);
    }

    @Override
    public String getName() {
        return "Affix Loot Entries";
    }

    @Override
    public void generate() {
        this.addEntry(new AffixLootEntry(TieredWeights.forAllTiers(5, 1), new ItemStackTemplate(Items.SHEARS)));
        this.addEntry(new AffixLootEntry(TieredWeights.forAllTiers(15, 1), new ItemStackTemplate(Items.BOW)));
        this.addEntry(new AffixLootEntry(TieredWeights.forAllTiers(10, 1), new ItemStackTemplate(Items.CROSSBOW)));
        this.addEntry(new AffixLootEntry(TieredWeights.forAllTiers(10, 1), new ItemStackTemplate(Items.SHIELD)));
        this.addEntry(new AffixLootEntry(TRIDENT, new ItemStackTemplate(Items.TRIDENT)));
        this.addEntry(new AffixLootEntry(TieredWeights.forAllTiers(2, 0), new ItemStackTemplate(Items.TURTLE_HELMET)));

        this.addTools(STONE, Items.STONE_SWORD, Items.STONE_AXE, Items.STONE_PICKAXE, Items.STONE_SHOVEL);
        this.addTools(GOLD, Items.GOLDEN_SWORD, Items.GOLDEN_AXE, Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL);
        this.addTools(IRON, Items.IRON_SWORD, Items.IRON_AXE, Items.IRON_PICKAXE, Items.IRON_SHOVEL);
        this.addTools(DIAMOND, Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL);
        this.addTools(NETHERITE, Items.NETHERITE_SWORD, Items.NETHERITE_AXE, Items.NETHERITE_PICKAXE, Items.NETHERITE_SHOVEL);

        this.addArmor(LEATHER, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
        this.addArmor(CHAIN, Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS);
        this.addArmor(GOLD, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);
        this.addArmor(IRON, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
        this.addArmor(DIAMOND, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
        this.addArmor(NETHERITE, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);
    }

    protected void addTools(TieredWeights weights, Item... tools) {
        for (Item tool : tools) {
            this.addEntry(new AffixLootEntry(weights, new ItemStackTemplate(tool)));
        }
    }

    protected void addArmor(TieredWeights weights, Item... pieces) {
        for (Item piece : pieces) {
            this.addEntry(new AffixLootEntry(weights, new ItemStackTemplate(piece)));
        }
    }

    protected void addEntry(AffixLootEntry entry) {
        this.add(Apotheosis.loc(BuiltInRegistries.ITEM.getKey(entry.stackTemplate().item().value()).getPath()), entry);
    }
}
