package dev.shadowsoffire.apotheosis.data;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import org.spongepowered.include.com.google.common.base.Preconditions;

import dev.shadowsoffire.apotheosis.Apoth.BuiltInRegs;
import dev.shadowsoffire.apotheosis.Apoth.LootCategories;
import dev.shadowsoffire.apotheosis.affix.AffixType;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.LootRule;
import dev.shadowsoffire.apotheosis.loot.LootRule.AffixLootRule;
import dev.shadowsoffire.apotheosis.loot.LootRule.ComponentLootRule;
import dev.shadowsoffire.apotheosis.loot.LootRule.DurabilityLootRule;
import dev.shadowsoffire.apotheosis.loot.LootRule.SocketLootRule;
import dev.shadowsoffire.apotheosis.loot.RarityOverride;
import dev.shadowsoffire.apotheosis.loot.RarityOverrideRegistry;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.data.PackOutput;

public class RarityOverrideProvider extends DynamicRegistryProvider<RarityOverride> {

    public RarityOverrideProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries, RarityOverrideRegistry.INSTANCE);
    }

    @Override
    public String getName() {
        return "rarity_overrides";
    }

    @Override
    public void generate() {
        LootRarity common = rarity("common");
        LootRarity uncommon = rarity("uncommon");
        LootRarity rare = rarity("rare");
        LootRarity epic = rarity("epic");
        LootRarity mythic = rarity("mythic");

        addOverride(LootCategories.SHEARS, b -> b
            .override(common, c -> c
                .rule(new SocketLootRule(0, 1))
                .rule(new AffixLootRule(AffixType.STAT))
                .rule(new DurabilityLootRule(0.05F, 0.10F)))
            .override(uncommon, c -> c
                .rule(new SocketLootRule(1, 1))
                .rule(new AffixLootRule(AffixType.STAT))
                .rule(new DurabilityLootRule(0.10F, 0.15F)))
            .override(rare, c -> c
                .rule(new SocketLootRule(1, 1))
                .rule(new AffixLootRule(AffixType.STAT))
                .rule(new AffixLootRule(AffixType.BASIC_EFFECT))
                .rule(new DurabilityLootRule(0.1F, 0.25F)))
            .override(epic, c -> c
                .rule(new SocketLootRule(1, 1))
                .rule(new AffixLootRule(AffixType.STAT))
                .rule(new AffixLootRule(AffixType.BASIC_EFFECT))
                .rule(new AffixLootRule(AffixType.BASIC_EFFECT))
                .rule(new DurabilityLootRule(0.25F, 0.55F)))
            .override(mythic, c -> c
                .rule(new SocketLootRule(1, 2))
                .rule(new AffixLootRule(AffixType.STAT))
                .rule(new AffixLootRule(AffixType.BASIC_EFFECT))
                .rule(new AffixLootRule(AffixType.BASIC_EFFECT))
                .rule(new DurabilityLootRule(0.45F, 0.75F))));
    }

    private static LootRarity rarity(String path) {
        return Preconditions.checkNotNull(RarityRegistry.INSTANCE.getValue(Apotheosis.loc(path)));
    }

    private static <T> LootRule componentRule(DataComponentType<T> type, T value) {
        return new ComponentLootRule(DataComponentPatch.builder().set(type, value).build());
    }

    private void addOverride(LootCategory category, UnaryOperator<RarityOverride.Builder> config) {
        this.add(Apotheosis.loc(BuiltInRegs.LOOT_CATEGORY.getKey(category).toString().replace(':', '/')), config.apply(RarityOverride.builder(category)).build());
    }

}
