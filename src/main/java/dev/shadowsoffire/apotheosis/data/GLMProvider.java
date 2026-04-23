package dev.shadowsoffire.apotheosis.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.advancements.predicates.MonsterPredicate;
import dev.shadowsoffire.apotheosis.loot.conditions.KilledByRealPlayerCondition;
import dev.shadowsoffire.apotheosis.loot.modifiers.AffixConvertLootModifier;
import dev.shadowsoffire.apotheosis.loot.modifiers.AffixConvertLootModifier.AffixConversionEntry;
import dev.shadowsoffire.apotheosis.loot.modifiers.AffixHookLootModifier;
import dev.shadowsoffire.apotheosis.loot.modifiers.AffixLootModifier;
import dev.shadowsoffire.apotheosis.loot.modifiers.AffixLootModifier.AffixTableEntry;
import dev.shadowsoffire.apotheosis.loot.modifiers.GemLootModifier;
import dev.shadowsoffire.apotheosis.loot.modifiers.GemLootModifier.GemTableEntry;
import dev.shadowsoffire.apotheosis.util.LootPatternMatcher;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.LootContext.EntityTarget;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

public class GLMProvider extends GlobalLootModifierProvider {

    public GLMProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries, Apotheosis.MODID);
    }

    @Override
    protected void start() {
        List<AffixConversionEntry> conversions = new ArrayList<>();
        conversions.add(new AffixConversionEntry(LootPatternMatcher.of(".*blocks.*"), 0, Set.of()));
        conversions.add(new AffixConversionEntry(LootPatternMatcher.of(".*"), 0.35F, Set.of()));
        this.add("affix_conversion", new AffixConvertLootModifier(new LootItemCondition[0], IGlobalLootModifier.DEFAULT_PRIORITY - 75, conversions));

        List<AffixTableEntry> affixLootRules = new ArrayList<>();
        affixLootRules.add(new AffixTableEntry(LootPatternMatcher.of("minecraft", "chests.*"), 0.35F, Set.of(), Set.of()));
        affixLootRules.add(new AffixTableEntry(LootPatternMatcher.of("chests.*"), 0.3F, Set.of(), Set.of()));
        affixLootRules.add(new AffixTableEntry(LootPatternMatcher.of("twilightforest", "structures.*"), 0.3F, Set.of(), Set.of()));
        this.add("affix_loot_injection", new AffixLootModifier(new LootItemCondition[0], IGlobalLootModifier.DEFAULT_PRIORITY + 50, affixLootRules));

        List<GemTableEntry> gemLootRules = new ArrayList<>();
        gemLootRules.add(new GemTableEntry(LootPatternMatcher.of("minecraft", "chests.*"), 0.25F, Set.of(), Set.of()));
        gemLootRules.add(new GemTableEntry(LootPatternMatcher.of("chests.*"), 0.2F, Set.of(), Set.of()));
        gemLootRules.add(new GemTableEntry(LootPatternMatcher.of("twilightforest", "structures.*"), 0.2F, Set.of(), Set.of()));
        this.add("gem_loot_injection", new GemLootModifier(new LootItemCondition[0], IGlobalLootModifier.DEFAULT_PRIORITY + 50, gemLootRules));

        // This modifier allows for fake player kills, as well as real player kills, so the chance is much lower.
        List<GemTableEntry> gemPlayerKillRules = new ArrayList<>();
        gemPlayerKillRules.add(new GemTableEntry(LootPatternMatcher.of(".*"), 0.005F, Set.of(), Set.of()));

        List<LootItemCondition> gemPlayerKillConditions = new ArrayList<>();
        gemPlayerKillConditions.add(LootItemEntityPropertyCondition.hasProperties(EntityTarget.THIS, EntityPredicate.Builder.entity().subPredicate(MonsterPredicate.INSTANCE)).build());
        gemPlayerKillConditions.add(LootItemKilledByPlayerCondition.killedByPlayer().build());
        this.add("gem_entity_drops", new GemLootModifier(gemPlayerKillConditions.toArray(new LootItemCondition[0]), IGlobalLootModifier.DEFAULT_PRIORITY + 50, gemPlayerKillRules));

        // This modifier allows only for "real" player kills. However, it's independent of the other one (both can roll),
        // so the chance is reduced slightly compared to 1.20's base value.
        List<GemTableEntry> gemRealPlayerKillRules = new ArrayList<>();
        gemRealPlayerKillRules.add(new GemTableEntry(LootPatternMatcher.of(".*"), 0.04F, Set.of(), Set.of()));

        List<LootItemCondition> gemRealPlayerKillConditions = new ArrayList<>();
        gemRealPlayerKillConditions.add(LootItemEntityPropertyCondition.hasProperties(EntityTarget.THIS, EntityPredicate.Builder.entity().subPredicate(MonsterPredicate.INSTANCE)).build());
        gemRealPlayerKillConditions.add(KilledByRealPlayerCondition.INSTANCE);
        this.add("gem_entity_drops_from_real_players", new GemLootModifier(gemRealPlayerKillConditions.toArray(new LootItemCondition[0]), IGlobalLootModifier.DEFAULT_PRIORITY + 50, gemRealPlayerKillRules));

        this.add("affix_hook", new AffixHookLootModifier(new LootItemCondition[0], IGlobalLootModifier.DEFAULT_PRIORITY - 100));
    }

}
