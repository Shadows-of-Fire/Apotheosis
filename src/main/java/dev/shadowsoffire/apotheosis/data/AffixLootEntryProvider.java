package dev.shadowsoffire.apotheosis.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.AffixLootEntry;
import dev.shadowsoffire.apotheosis.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;

public class AffixLootEntryProvider extends DynamicRegistryProvider<AffixLootEntry> {

    protected static final TieredWeights STONE = TieredWeights.onlyFor(WorldTier.HAVEN, 25, 0);
    protected static final TieredWeights LEATHER = TieredWeights.onlyFor(WorldTier.HAVEN, 25, 0);
    protected static final TieredWeights CHAIN = TieredWeights.builder()
        .with(WorldTier.HAVEN, 10, 0)
        .with(WorldTier.FRONTIER, 10, 1)
        .build();
    protected static final TieredWeights GOLD = TieredWeights.builder()
        .with(WorldTier.HAVEN, 5, 0)
        .with(WorldTier.FRONTIER, 10, 0)
        .with(WorldTier.ASCENT, 5, 0)
        .build();
    protected static final TieredWeights IRON = TieredWeights.builder()
        .with(WorldTier.FRONTIER, 25, 1)
        .with(WorldTier.ASCENT, 10, 0)
        .with(WorldTier.SUMMIT, 10, 0)
        .build();
    protected static final TieredWeights DIAMOND = TieredWeights.builder()
        .with(WorldTier.ASCENT, 25, 0)
        .with(WorldTier.SUMMIT, 25, 0)
        .with(WorldTier.PINNACLE, 5, 0)
        .build();
    protected static final TieredWeights NETHERITE = TieredWeights.builder()
        .with(WorldTier.SUMMIT, 5, 1)
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
        Map<Holder<ArmorMaterial>, TieredWeights> armorWeights = new HashMap<>();
        armorWeights.put(ArmorMaterials.LEATHER, LEATHER);
        armorWeights.put(ArmorMaterials.CHAIN, CHAIN);
        armorWeights.put(ArmorMaterials.GOLD, GOLD);
        armorWeights.put(ArmorMaterials.IRON, IRON);
        armorWeights.put(ArmorMaterials.DIAMOND, DIAMOND);
        armorWeights.put(ArmorMaterials.NETHERITE, NETHERITE);

        // Haven spawns Stone/Leather and Gold/Chain items.
        // Frontier spawns Iron and Gold/Chain items with a low chance for Diamond items.
        // Ascent spawns Iron and Diamond items.
        // Summit spawns Diamond items, with a low chance for Iron or Netherite.
        // Apotheosis spawns mostly Netherite items, with a chance for Diamond.
        for (Item i : BuiltInRegistries.ITEM) {
            if (!"minecraft".equals(BuiltInRegistries.ITEM.getKey(i).getNamespace())) {
                continue; // We only want vanilla items for this pass, since mod items need conditions.
            }

            LootCategory cat = LootCategory.forItem(i.getDefaultInstance());
            if (cat.isNone()) {
                continue; // Can't generate an ALE for non-affixable items.
            }

            if (i instanceof TieredItem t && t.getTier() instanceof Tiers tier) {
                switch (tier) {
                    case STONE -> this.addEntry(new AffixLootEntry(STONE, new ItemStack(i)));
                    case GOLD -> this.addEntry(new AffixLootEntry(GOLD, new ItemStack(i)));
                    case IRON -> this.addEntry(new AffixLootEntry(IRON, new ItemStack(i)));
                    case DIAMOND -> this.addEntry(new AffixLootEntry(DIAMOND, new ItemStack(i)));
                    case NETHERITE -> this.addEntry(new AffixLootEntry(NETHERITE, new ItemStack(i)));
                    default -> {}
                }
            }
            else if (i instanceof ArmorItem a && a.getType() != ArmorItem.Type.BODY) {
                TieredWeights weights = armorWeights.get(a.getMaterial());
                if (weights != null) {
                    this.addEntry(new AffixLootEntry(weights, new ItemStack(i)));
                }
            }
        }

        this.addEntry(new AffixLootEntry(TieredWeights.forAllTiers(5, 1), new ItemStack(Items.SHEARS)));
        this.addEntry(new AffixLootEntry(TieredWeights.forAllTiers(15, 1), new ItemStack(Items.BOW)));
        this.addEntry(new AffixLootEntry(TieredWeights.forAllTiers(10, 1), new ItemStack(Items.CROSSBOW)));
        this.addEntry(new AffixLootEntry(TieredWeights.forAllTiers(10, 1), new ItemStack(Items.SHIELD)));
        this.addEntry(new AffixLootEntry(TRIDENT, new ItemStack(Items.TRIDENT)));
        this.addEntry(new AffixLootEntry(TieredWeights.forAllTiers(2, 0), new ItemStack(Items.TURTLE_HELMET)));

    }

    protected void addEntry(AffixLootEntry entry) {
        this.add(Apotheosis.loc(BuiltInRegistries.ITEM.getKey(entry.stack().getItem()).getPath()), entry);
    }
}
