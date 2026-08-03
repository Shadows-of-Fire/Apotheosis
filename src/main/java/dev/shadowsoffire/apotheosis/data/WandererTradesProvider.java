package dev.shadowsoffire.apotheosis.data;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.trades.AutomaticAffixTrade;
import dev.shadowsoffire.apotheosis.compat.enchanting.ApothicEnchantingCompat;
import dev.shadowsoffire.apotheosis.compat.spawners.ApothicSpawnersCompat;
import dev.shadowsoffire.apotheosis.loot.functions.TierGatedTrade;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

public final class WandererTradesProvider {

    private WandererTradesProvider() {}

    public static void bootstrap(BootstrapContext<VillagerTrade> ctx) {
        HolderGetter<Enchantment> ench = ctx.lookup(Registries.ENCHANTMENT);

        basic(ctx, "blaze_powder", Items.EMERALD, 1, Items.BLAZE_POWDER, 3, 5, 5);
        basic(ctx, "diamond", Items.EMERALD, 3, Items.DIAMOND, 1, 5, 15);
        basic(ctx, "eye_of_ender", Items.EMERALD, 2, Items.ENDER_EYE, 1, 3, 15);
        basic(ctx, "gold_ingot", Items.EMERALD, 2, Items.GOLD_INGOT, 1, 8, 5);
        basic(ctx, "iron_ingot", Items.EMERALD, 1, Items.IRON_INGOT, 3, 15, 5);
        basic(ctx, "prismarine_shard", Items.EMERALD, 4, Items.PRISMARINE_SHARD, 5, 5, 15);
        basic(ctx, "saddle", Items.EMERALD, 2, Items.SADDLE, 1, 2, 15);
        basic(ctx, "iron_horse_armor", Items.EMERALD, 2, Items.IRON_HORSE_ARMOR, 1, 3, 15);
        basic(ctx, "golden_horse_armor", Items.EMERALD, 4, Items.GOLDEN_HORSE_ARMOR, 1, 2, 30);
        basic(ctx, "diamond_horse_armor", Items.EMERALD, 6, Items.DIAMOND_HORSE_ARMOR, 1, 1, 50);
        basic(ctx, "beacon", Items.EMERALD, 25, Items.BEACON, 1, 1, 50);
        basic(ctx, "enchanted_golden_apple", Items.EMERALD, 15, Items.ENCHANTED_GOLDEN_APPLE, 1, 1, 15);
        basic(ctx, "skeleton_skull", Items.EMERALD, 4, Items.SKELETON_SKULL, 1, 5, 15);
        basic(ctx, "wither_skeleton_skull", Items.EMERALD, 5, Items.WITHER_SKELETON_SKULL, 1, 5, 15);
        basic(ctx, "zombie_head", Items.EMERALD, 2, Items.ZOMBIE_HEAD, 1, 5, 15);
        basic(ctx, "totem_of_undying", Items.EMERALD, 10, Items.TOTEM_OF_UNDYING, 1, 1, 50);

        rareGear(ctx, ench, "rare_gear/arachnids_fear",
            new TradeCost(Items.DIAMOND_SWORD, 1), new TradeCost(Items.EMERALD, 45),
            Items.DIAMOND_SWORD, "arachnids_fear", 0xC11101,
            Map.of(
                "minecraft:bane_of_arthropods", 10,
                "minecraft:fire_aspect", 5,
                "minecraft:looting", 5,
                "minecraft:mending", 1,
                "minecraft:unbreaking", 5),
            500);
        rareGear(ctx, ench, "rare_gear/bonesplitter",
            new TradeCost(Items.DIAMOND_AXE, 1), new TradeCost(Items.EMERALD, 64),
            Items.DIAMOND_AXE, "bonesplitter", 0x9AB091,
            Map.of(
                "apothic_enchanting:scavenger", 2,
                "apothic_spawners:capturing", 2,
                "minecraft:looting", 5,
                "minecraft:mending", 1,
                "minecraft:sharpness", 10,
                "minecraft:unbreaking", 3),
            500);
        rareGear(ctx, ench, "rare_gear/captive_dreams",
            new TradeCost(Items.DIAMOND_SWORD, 1), new TradeCost(Items.EMERALD, 45),
            Items.DIAMOND_SWORD, "captive_dreams", 0xADD8E6,
            Map.of(
                "apothic_spawners:capturing", 5,
                "minecraft:looting", 4,
                "minecraft:mending", 1,
                "minecraft:sharpness", 5,
                "minecraft:unbreaking", 5),
            500);
        rareGear(ctx, ench, "rare_gear/eternal_vigilance",
            new TradeCost(Items.DIAMOND, 64), new TradeCost(Items.PHANTOM_MEMBRANE, 32),
            Items.DIAMOND_SWORD, "eternal_vigilance", 0x1ABBE0,
            Map.of(
                "apothic_enchanting:life_mending", 5,
                "apothic_enchanting:scavenger", 5,
                "minecraft:looting", 5,
                "minecraft:sharpness", 10,
                "minecraft:unbreaking", 5),
            1000);
        rareGear(ctx, ench, "rare_gear/greatplate_of_eternity",
            new TradeCost(Items.DIAMOND_CHESTPLATE, 1), new TradeCost(Items.EMERALD, 55),
            Items.DIAMOND_CHESTPLATE, "greatplate_of_eternity", 0x1ABBE0,
            Map.of(
                "apothic_enchanting:berserkers_fury", 2,
                "minecraft:mending", 1,
                "minecraft:projectile_protection", 5,
                "minecraft:protection", 5,
                "minecraft:unbreaking", 5),
            500);
        rareGear(ctx, ench, "rare_gear/rune_forged_greaves",
            new TradeCost(Items.DIAMOND_BOOTS, 1), new TradeCost(Items.EMERALD, 45),
            Items.DIAMOND_BOOTS, "rune_forged_greaves", 0x1ABBE0,
            Map.of(
                "apothic_enchanting:stable_footing", 1,
                "minecraft:feather_falling", 5,
                "minecraft:mending", 1,
                "minecraft:protection", 5,
                "minecraft:unbreaking", 5),
            500);
        rareGear(ctx, ench, "rare_gear/stonebreaker",
            new TradeCost(Items.DIAMOND_PICKAXE, 1), new TradeCost(Items.EMERALD, 45),
            Items.DIAMOND_PICKAXE, "stonebreaker", 0x1ABBE0,
            Map.of(
                "apothic_enchanting:boon_of_the_earth", 4,
                "minecraft:efficiency", 5,
                "minecraft:fortune", 4,
                "minecraft:mending", 1,
                "minecraft:unbreaking", 5),
            500);
        rareGear(ctx, ench, "rare_gear/thunder_forged_legguards",
            new TradeCost(Items.DIAMOND_LEGGINGS, 1), new TradeCost(Items.EMERALD, 55),
            Items.DIAMOND_LEGGINGS, "thunder_forged_legguards", 0x1ABBE0,
            Map.of(
                "apothic_enchanting:rebounding", 10,
                "minecraft:mending", 1,
                "minecraft:protection", 5,
                "minecraft:unbreaking", 5),
            500);
        rareGear(ctx, ench, "rare_gear/timeworn_visage",
            new TradeCost(Items.DIAMOND_HELMET, 1), new TradeCost(Items.EMERALD, 45),
            Items.DIAMOND_HELMET, "timeworn_visage", 0x1ABBE0,
            Map.of(
                "minecraft:aqua_affinity", 1,
                "minecraft:mending", 1,
                "minecraft:protection", 5,
                "minecraft:respiration", 5,
                "minecraft:unbreaking", 5),
            500);
        rareGear(ctx, ench, "rare_gear/treecapitator",
            new TradeCost(Items.DIAMOND_AXE, 1), new TradeCost(Items.EMERALD, 45),
            Items.DIAMOND_AXE, "treecapitator", 0x608F07,
            Map.of(
                "apothic_enchanting:chainsaw", 1,
                "minecraft:efficiency", 10,
                "minecraft:mending", 1,
                "minecraft:silk_touch", 1,
                "minecraft:unbreaking", 5),
            500);

        for (int i = 1; i <= 10; i++) {
            affixTrade(ctx, "affix/automatic_" + i);
        }
    }

    private static void basic(BootstrapContext<VillagerTrade> ctx, String path, Item cost, int costCount, Item result, int resultCount, int maxUses, int xp) {
        ctx.register(
            trade(path),
            new VillagerTrade(
                new TradeCost(cost, costCount),
                new ItemStackTemplate(result, resultCount),
                maxUses,
                xp,
                0.05F,
                Optional.empty(),
                List.<LootItemFunction>of()));
    }

    private static void affixTrade(BootstrapContext<VillagerTrade> ctx, String path) {
        ctx.register(
            trade(path),
            new VillagerTrade(
                new TradeCost(Items.EMERALD, 1),
                new ItemStackTemplate(Items.EMERALD),
                1,
                100,
                1.0F,
                Optional.empty(),
                List.<LootItemFunction>of(new AutomaticAffixTrade(List.of(), Set.of(), List.of()))));
    }

    private static void rareGear(BootstrapContext<VillagerTrade> ctx, HolderGetter<Enchantment> enchantments,
        String path, TradeCost wants, TradeCost additionalWants, Item givesItem,
        String nameKey, int color, Map<String, Integer> enchants, int xp) {

        ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchants.forEach((id, level) -> {
            Holder<Enchantment> holder = enchantments.getOrThrow(
                ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse(id)));
            mut.set(holder, level);
        });

        Component name = Component.translatable("name.apotheosis." + nameKey)
            .withStyle(Style.EMPTY.withItalic(false).withColor(color));

        DataComponentPatch patch = DataComponentPatch.builder()
            .set(DataComponents.CUSTOM_NAME, name)
            .set(DataComponents.ENCHANTMENTS, mut.toImmutable())
            .build();

        ItemStackTemplate gives = new ItemStackTemplate(givesItem, 1, patch);

        ctx.register(
            trade(path),
            new VillagerTrade(
                wants,
                Optional.of(additionalWants),
                gives,
                1,
                xp,
                0.05F,
                Optional.empty(),
                List.of(new TierGatedTrade(List.of(), WorldTier.SUMMIT))));
    }

    /**
     * Conditions for the trades that reference Apothic Enchanting or Apothic Spawners enchantments. These are attached
     * to the generated files through the DataGenBuilder so the trades vanish when the corresponding mod is absent.
     * The tag entries for these trades in {@code villager_trade/wandering_trader/uncommon.json} must remain optional.
     */
    public static Map<ResourceKey<?>, List<ICondition>> conditions() {
        ICondition enchLoaded = new ModLoadedCondition(ApothicEnchantingCompat.MODID);
        ICondition spawnersLoaded = new ModLoadedCondition(ApothicSpawnersCompat.MODID);
        return Map.of(
            trade("rare_gear/bonesplitter"), List.of(enchLoaded, spawnersLoaded),
            trade("rare_gear/captive_dreams"), List.of(spawnersLoaded),
            trade("rare_gear/eternal_vigilance"), List.of(enchLoaded),
            trade("rare_gear/greatplate_of_eternity"), List.of(enchLoaded),
            trade("rare_gear/rune_forged_greaves"), List.of(enchLoaded),
            trade("rare_gear/stonebreaker"), List.of(enchLoaded),
            trade("rare_gear/thunder_forged_legguards"), List.of(enchLoaded),
            trade("rare_gear/treecapitator"), List.of(enchLoaded));
    }

    private static ResourceKey<VillagerTrade> trade(String path) {
        return ResourceKey.create(Registries.VILLAGER_TRADE, Apotheosis.loc(path));
    }

}
