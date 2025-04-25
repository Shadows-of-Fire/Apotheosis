package dev.shadowsoffire.apotheosis.data;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import dev.shadowsoffire.apotheosis.Apoth.DataMaps;
import dev.shadowsoffire.apotheosis.Apoth.LootCategories;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.mobs.InvaderSpawnRules;
import dev.shadowsoffire.apotheosis.mobs.util.SurfaceType;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.data.DataMapProvider;

public class ApothDataMapProvider extends DataMapProvider {

    private static final ResourceLocation TWILIGHT_FOREST = ResourceLocation.fromNamespaceAndPath("twilightforest", "twilight_forest_type");

    public ApothDataMapProvider(PackOutput packOutput, CompletableFuture<Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        Builder<InvaderSpawnRules, DimensionType> invaderRules = builder(DataMaps.INVADER_SPAWN_RULES);

        invaderRules.add(BuiltinDimensionTypes.OVERWORLD, new InvaderSpawnRules(
            Map.of(
                WorldTier.HAVEN, 0F,
                WorldTier.FRONTIER, 0.018F,
                WorldTier.ASCENT, 0.020F,
                WorldTier.SUMMIT, 0.025F,
                WorldTier.PINNACLE, 0.03F),
            Optional.empty(),
            SurfaceType.NEEDS_SKY_OR_SAME_VERTICAL_SLICE), false);

        invaderRules.add(BuiltinDimensionTypes.NETHER, new InvaderSpawnRules(
            Map.of(
                WorldTier.HAVEN, 0F,
                WorldTier.FRONTIER, 0.025F,
                WorldTier.ASCENT, 0.027F,
                WorldTier.SUMMIT, 0.03F,
                WorldTier.PINNACLE, 0.035F),
            Optional.empty(),
            SurfaceType.ANY), false);

        invaderRules.add(BuiltinDimensionTypes.END, new InvaderSpawnRules(
            Map.of(
                WorldTier.HAVEN, 0F,
                WorldTier.FRONTIER, 0.018F,
                WorldTier.ASCENT, 0.020F,
                WorldTier.SUMMIT, 0.025F,
                WorldTier.PINNACLE, 0.03F),
            Optional.empty(),
            SurfaceType.SURFACE_OUTER_END), false);

        invaderRules.add(TWILIGHT_FOREST, new InvaderSpawnRules(
            Map.of(
                WorldTier.HAVEN, 0F,
                WorldTier.FRONTIER, 0.05F,
                WorldTier.ASCENT, 0.053F,
                WorldTier.SUMMIT, 0.06F,
                WorldTier.PINNACLE, 0.063F),
            Optional.empty(),
            SurfaceType.NEEDS_SURFACE), false, new ModLoadedCondition(TWILIGHT_FOREST.getNamespace()));

        Builder<LootCategory, Item> catOverrides = builder(DataMaps.LOOT_CATEGORY_OVERRIDES);

        catOverrides.add(BuiltInRegistries.ITEM.wrapAsHolder(Items.IRON_SWORD), LootCategories.MELEE_WEAPON, false);
        catOverrides.add(BuiltInRegistries.ITEM.wrapAsHolder(Items.SHULKER_SHELL), LootCategories.NONE, false);
    }

}
