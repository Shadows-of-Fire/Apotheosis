//package dev.shadowsoffire.apotheosis.data.twilight;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//
//import dev.shadowsoffire.apotheosis.Apotheosis;
//import dev.shadowsoffire.apotheosis.data.AffixLootEntryProvider;
//import dev.shadowsoffire.apotheosis.loot.AffixLootEntry;
//import dev.shadowsoffire.apotheosis.loot.LootCategory;
//import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
//import dev.shadowsoffire.apotheosis.tiers.WorldTier;
//import net.minecraft.core.Holder;
//import net.minecraft.core.HolderLookup.Provider;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.data.PackOutput;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.ArmorItem;
//import net.minecraft.world.item.ArmorMaterial;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.Tier;
//import net.minecraft.world.item.TieredItem;
//import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
//import twilightforest.init.TFArmorMaterials;
//import twilightforest.init.TFItems;
//import twilightforest.util.TFToolMaterials;
//
//public class TwilightAffixLootProvider extends AffixLootEntryProvider {
//
//    protected static final TieredWeights IRONWOOD = TieredWeights.builder()
//        .with(WorldTier.FRONTIER, 25, 1)
//        .with(WorldTier.ASCENT, 10, 0)
//        .with(WorldTier.SUMMIT, 10, 0)
//        .build();
//    protected static final TieredWeights STEELEAF = TieredWeights.builder()
//        .with(WorldTier.FRONTIER, 25, 1)
//        .with(WorldTier.ASCENT, 10, 0)
//        .with(WorldTier.SUMMIT, 10, 0)
//        .build();
//    protected static final TieredWeights KNIGHTMETAL = TieredWeights.builder()
//        .with(WorldTier.ASCENT, 25, 0)
//        .with(WorldTier.SUMMIT, 25, 0)
//        .with(WorldTier.PINNACLE, 5, 0)
//        .build();
//    protected static final TieredWeights ARCTIC_FIERY = TieredWeights.builder()
//        .with(WorldTier.ASCENT, 25, 0)
//        .with(WorldTier.SUMMIT, 25, 0)
//        .with(WorldTier.PINNACLE, 5, 0)
//        .build();
//    protected static final TieredWeights YETI = TieredWeights.builder()
//        .with(WorldTier.SUMMIT, 5, 1)
//        .with(WorldTier.PINNACLE, 25, 2)
//        .build();
//    protected static final TieredWeights BOWS = TieredWeights.builder()
//        .with(WorldTier.SUMMIT, 7, 1)
//        .with(WorldTier.PINNACLE, 7, 1)
//        .build();
//
//    public TwilightAffixLootProvider(PackOutput output, CompletableFuture<Provider> registries) {
//        super(output, registries);
//    }
//
//    @Override
//    public String getName() {
//        return "Twilight Affix Loot Entries";
//    }
//
//    @Override
//    public void generate() {
//        Map<Holder<ArmorMaterial>, TieredWeights> armorWeights = new HashMap<>();
//        armorWeights.put(TFArmorMaterials.IRONWOOD, IRONWOOD);
//        armorWeights.put(TFArmorMaterials.STEELEAF, STEELEAF);
//        armorWeights.put(TFArmorMaterials.KNIGHTMETAL, KNIGHTMETAL);
//        armorWeights.put(TFArmorMaterials.ARCTIC, ARCTIC_FIERY);
//        armorWeights.put(TFArmorMaterials.FIERY, ARCTIC_FIERY);
//        armorWeights.put(TFArmorMaterials.YETI, YETI);
//
//        Map<Tier, TieredWeights> toolWeights = new HashMap<>();
//        toolWeights.put(TFToolMaterials.IRONWOOD, IRONWOOD);
//        toolWeights.put(TFToolMaterials.STEELEAF, STEELEAF);
//        toolWeights.put(TFToolMaterials.KNIGHTMETAL, KNIGHTMETAL);
//        toolWeights.put(TFToolMaterials.ICE, ARCTIC_FIERY);
//        toolWeights.put(TFToolMaterials.FIERY, ARCTIC_FIERY);
//        toolWeights.put(TFToolMaterials.GIANT, YETI);
//        toolWeights.put(TFToolMaterials.GLASS, YETI);
//
//        for (Item i : BuiltInRegistries.ITEM) {
//            if (!"twilightforest".equals(BuiltInRegistries.ITEM.getKey(i).getNamespace())) {
//                continue; // This file only handles twilight forest compat.
//            }
//
//            LootCategory cat = LootCategory.forItem(i.getDefaultInstance());
//            if (cat.isNone()) {
//                continue; // Can't generate an ALE for non-affixable items.
//            }
//
//            if (i instanceof TieredItem t) {
//                TieredWeights weights = toolWeights.get(t.getTier());
//                if (weights != null) {
//                    this.addEntry(new AffixLootEntry(weights, new ItemStack(i)));
//                }
//            }
//            else if (i instanceof ArmorItem a && a.getType() != ArmorItem.Type.BODY) {
//                TieredWeights weights = armorWeights.get(a.getMaterial());
//                if (weights != null) {
//                    this.addEntry(new AffixLootEntry(weights, new ItemStack(i)));
//                }
//            }
//        }
//
//        this.addEntry(new AffixLootEntry(BOWS, new ItemStack(TFItems.ENDER_BOW.get())));
//        this.addEntry(new AffixLootEntry(BOWS, new ItemStack(TFItems.ICE_BOW.get())));
//        this.addEntry(new AffixLootEntry(BOWS, new ItemStack(TFItems.SEEKER_BOW.get())));
//        this.addEntry(new AffixLootEntry(BOWS, new ItemStack(TFItems.TRIPLE_BOW.get())));
//        this.addEntry(new AffixLootEntry(TieredWeights.forTiersAbove(WorldTier.ASCENT, 5, 1), new ItemStack(TFItems.KNIGHTMETAL_SHIELD.get())));
//
//    }
//
//    @Override
//    protected void addEntry(AffixLootEntry entry) {
//        ResourceLocation key = Apotheosis.loc("twilight/" + BuiltInRegistries.ITEM.getKey(entry.stack().getItem()).getPath());
//        this.addConditionally(key, entry, new ModLoadedCondition("twilightforest"));
//    }
//
//}
