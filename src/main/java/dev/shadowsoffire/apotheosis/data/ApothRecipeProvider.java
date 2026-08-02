package dev.shadowsoffire.apotheosis.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Blocks;
import dev.shadowsoffire.apotheosis.Apoth.Items;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.UnnamingRecipe;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingRecipe;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipe;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipe.OutputData;
import dev.shadowsoffire.apotheosis.compat.enchanting.ApothicEnchantingCompat;
import dev.shadowsoffire.apotheosis.compat.enchanting.CharmInfusionRecipe;
import dev.shadowsoffire.apotheosis.compat.spawners.ApothicSpawnersCompat;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.recipe.MaliceRecipe;
import dev.shadowsoffire.apotheosis.recipe.PotionCharmRecipe;
import dev.shadowsoffire.apotheosis.recipe.SupremacyRecipe;
import dev.shadowsoffire.apotheosis.socket.AddSocketsRecipe;
import dev.shadowsoffire.apotheosis.socket.SocketingRecipe;
import dev.shadowsoffire.apotheosis.socket.WithdrawalRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.PurityUpgradeRecipe;
import dev.shadowsoffire.apotheosis.util.AffixItemIngredient;
import dev.shadowsoffire.apotheosis.util.GemIngredient;
import dev.shadowsoffire.apotheosis.util.SizedUpgradeRecipe;
import dev.shadowsoffire.apotheosis.util.SpawnEggIngredient;
import dev.shadowsoffire.apothic_attributes.api.ALObjects.Potions;
import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.apothic_enchanting.table.EnchantingStatRegistry.Stats;
import dev.shadowsoffire.apothic_enchanting.table.infusion.InfusionRecipe;
import dev.shadowsoffire.apothic_spawners.modifiers.SpawnerModifier;
import dev.shadowsoffire.apothic_spawners.modifiers.StatModifier;
import dev.shadowsoffire.apothic_spawners.modifiers.StatModifier.Mode;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStat;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import dev.shadowsoffire.gateways.GatewayObjects;
import dev.shadowsoffire.gateways.Gateways;
import dev.shadowsoffire.gateways.gate.GatewayRegistry;
import dev.shadowsoffire.gateways.item.GatePearlItem;
import dev.shadowsoffire.placebo.datagen.LegacyRecipeProvider;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.FalseCondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class ApothRecipeProvider extends LegacyRecipeProvider {

    /**
     * Names of every modifier recipe shipped by Apothic Spawners, used to override and disable
     * those recipes via {@link FalseCondition} so Apotheosis can supply world-tier-gated equivalents.
     */
    private static final List<String> AS_MODIFIER_NAMES = List.of(
        "min_delay", "max_delay", "spawn_count", "max_nearby", "player_range", "spawn_range",
        "initial_health", "ignore_players", "ignore_conditions", "redstone_control",
        "ignore_light", "no_ai", "silent", "youthful", "burning", "echoing");

    public ApothRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Apotheosis.MODID);
    }

    @Override
    protected void genRecipes(RecipeOutput out, HolderLookup.Provider registries) {
        out.accept(Apotheosis.loc("socketing"), new SocketingRecipe(), null);
        out.accept(Apotheosis.loc("unnaming"), new UnnamingRecipe(), null);
        out.accept(Apotheosis.loc("widthdrawal"), new WithdrawalRecipe(), null);
        out.accept(Apotheosis.loc("malice"), new MaliceRecipe(), null);
        out.accept(Apotheosis.loc("supremacy"), new SupremacyRecipe(), null);
        addSockets("sigil_add_sockets", ingredient(Items.SIGIL_OF_SOCKETING), 2);
        addAffixSalvaging("common", Items.MYSTERIOUS_SCRAP_METAL);
        addAffixSalvaging("uncommon", Items.TIMEWORN_FABRIC);
        addAffixSalvaging("rare", Items.LUMINOUS_CRYSTAL_SHARD);
        addAffixSalvaging("epic", Items.ARCANE_SANDS);
        addAffixSalvaging("mythic", Items.GODFORGED_PEARL);

        addGemSalvaging(Purity.CRACKED, 1, 2);
        addGemSalvaging(Purity.CHIPPED, 1, 3);
        addGemSalvaging(Purity.FLAWED, 2, 4);
        addGemSalvaging(Purity.NORMAL, 3, 5);
        addGemSalvaging(Purity.FLAWLESS, 4, 7);
        addGemSalvaging(Purity.PERFECT, 5, 10);

        addOtherSalvaging("leather_horse_armor", new OutputData(Items.LEATHER, 3, 8), Items.LEATHER_HORSE_ARMOR);
        addOtherSalvaging("iron_horse_armor", new OutputData(Items.IRON_INGOT, 3, 8), Items.IRON_HORSE_ARMOR);
        addOtherSalvaging("golden_horse_armor", new OutputData(Items.GOLD_INGOT, 3, 8), Items.GOLDEN_HORSE_ARMOR);
        addOtherSalvaging("diamond_horse_armor", new OutputData(Items.DIAMOND, 3, 8), Items.DIAMOND_HORSE_ARMOR);
        addOtherSalvaging("wolf_armor", new OutputData(Items.ARMADILLO_SCUTE, 1, 3), Items.WOLF_ARMOR);

        addOtherSalvaging("wooden_tools", new OutputData(Items.OAK_PLANKS, 0, 1), Items.WOODEN_SWORD, Items.WOODEN_PICKAXE, Items.WOODEN_AXE, Items.WOODEN_SHOVEL, Items.WOODEN_HOE);
        addOtherSalvaging("stone_tools", new OutputData(Items.COBBLESTONE, 0, 1), Items.STONE_SWORD, Items.STONE_PICKAXE, Items.STONE_AXE, Items.STONE_SHOVEL, Items.STONE_HOE);
        addOtherSalvaging("gold_tools", new OutputData(Items.GOLD_INGOT, 0, 1), Items.GOLDEN_SWORD, Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_HOE);
        addOtherSalvaging("iron_tools", new OutputData(Items.IRON_INGOT, 0, 1), Items.IRON_SWORD, Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_HOE);
        addOtherSalvaging("diamond_tools", new OutputData(Items.DIAMOND, 0, 1), Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE);
        addOtherSalvaging("netherite_tools", new OutputData(Items.NETHERITE_SCRAP, 0, 2), Items.NETHERITE_SWORD, Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_HOE);

        addOtherSalvaging("leather_armor", new OutputData(Items.LEATHER, 1, 3), Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
        addOtherSalvaging("chain_armor", new OutputData(Items.CHAIN, 1, 3), Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS);
        addOtherSalvaging("gold_armor", new OutputData(Items.GOLD_INGOT, 1, 3), Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);
        addOtherSalvaging("iron_armor", new OutputData(Items.IRON_INGOT, 1, 3), Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
        addOtherSalvaging("diamond_armor", new OutputData(Items.DIAMOND, 1, 3), Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
        addOtherSalvaging("netherite_armor", new OutputData(Items.NETHERITE_SCRAP, 0, 2), Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);

        addReforging("common", 1, 0, 2, Blocks.SIMPLE_REFORGING_TABLE, Blocks.REFORGING_TABLE);
        addReforging("uncommon", 2, 1, 5, Blocks.SIMPLE_REFORGING_TABLE, Blocks.REFORGING_TABLE);
        addReforging("rare", 2, 2, 15, Blocks.SIMPLE_REFORGING_TABLE, Blocks.REFORGING_TABLE);
        addReforging("epic", 2, 4, 30, Blocks.REFORGING_TABLE);
        addReforging("mythic", 3, 5, 50, Blocks.REFORGING_TABLE);

        addShaped(Blocks.AUGMENTING_TABLE, 3, 3, null, Items.NETHER_STAR, null, Items.GODFORGED_PEARL, Items.ENCHANTING_TABLE, Items.GODFORGED_PEARL, Items.POLISHED_BLACKSTONE, Items.POLISHED_BLACKSTONE, Items.POLISHED_BLACKSTONE);
        addShaped(Blocks.GEM_CUTTING_TABLE, 3, 3, Items.SMOOTH_STONE, Items.SHEARS, Items.SMOOTH_STONE, ItemTags.PLANKS, Items.GEM_DUST, ItemTags.PLANKS, ItemTags.PLANKS, null, ItemTags.PLANKS);
        addShaped(new ItemStack(Items.GEM_FUSED_SLATE, 8), 3, 3, Items.DEEPSLATE, Items.DEEPSLATE, Items.DEEPSLATE, Items.DEEPSLATE, Items.GEM_DUST, Items.DEEPSLATE, Items.DEEPSLATE, Items.DEEPSLATE, Items.DEEPSLATE);
        addShaped(Blocks.REFORGING_TABLE, 3, 3, null, Tags.Items.INGOTS_NETHERITE, null, Items.ARCANE_SANDS, Items.SIMPLE_REFORGING_TABLE, Items.ARCANE_SANDS, Items.NETHER_BRICKS, Items.NETHER_BRICKS, Items.NETHER_BRICKS);
        addShaped(Blocks.SALVAGING_TABLE, 3, 3, Tags.Items.INGOTS_COPPER, Tags.Items.INGOTS_COPPER, Tags.Items.INGOTS_COPPER, Items.IRON_PICKAXE, Items.SMITHING_TABLE, Items.IRON_AXE, Items.GEM_DUST, Items.LAVA_BUCKET, Items.GEM_DUST);
        addShaped(Blocks.SIMPLE_REFORGING_TABLE, 3, 3, null, Tags.Items.INGOTS_IRON, null, Items.GEM_DUST, Items.ENCHANTING_TABLE, Items.GEM_DUST, Items.SMOOTH_STONE, Items.SMOOTH_STONE, Items.SMOOTH_STONE);

        addShaped(Blocks.GEM_CASE, 3, 3, Tags.Items.GLASS_BLOCKS, Tags.Items.GLASS_BLOCKS, Tags.Items.GLASS_BLOCKS, Items.BASALT, Items.GEM_CUTTING_TABLE, Items.BASALT, Items.BASALT, Items.ENDER_CHEST, Items.BASALT);

        addShaped(new ItemStack(Items.SIGIL_OF_ENHANCEMENT, 4), 3, 3, Items.GEM_DUST, Items.GEM_FUSED_SLATE, Items.GEM_DUST, Items.GEM_FUSED_SLATE, Items.GODFORGED_PEARL, Items.GEM_FUSED_SLATE, Items.GEM_DUST, Items.GEM_FUSED_SLATE,
            Items.GEM_DUST);
        addShaped(new ItemStack(Items.SIGIL_OF_REBIRTH, 6), 3, 3, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE, Items.GEM_DUST, Items.GEM_DUST, Items.GEM_DUST, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE,
            Items.GEM_FUSED_SLATE);
        addShaped(new ItemStack(Items.SIGIL_OF_SOCKETING, 3), 3, 3, Items.GEM_DUST, Items.GUNPOWDER, Items.GEM_DUST, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE, Items.GEM_DUST, Items.AMETHYST_SHARD,
            Items.GEM_DUST);
        addShaped(new ItemStack(Items.SIGIL_OF_UNNAMING, 6), 3, 3, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE, Items.FLINT, Items.FLINT, Items.FLINT, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE,
            Items.GEM_FUSED_SLATE);
        addShaped(new ItemStack(Items.SIGIL_OF_WITHDRAWAL, 4), 3, 3, Items.GEM_FUSED_SLATE, Items.BLAZE_ROD, Items.GEM_FUSED_SLATE, Tags.Items.ENDER_PEARLS, Items.LAVA_BUCKET, Tags.Items.ENDER_PEARLS, Items.GEM_FUSED_SLATE,
            Items.GEM_DUST, Items.GEM_FUSED_SLATE);

        List<Holder<Item>> rarityMaterials = List.of(Items.MYSTERIOUS_SCRAP_METAL, Items.TIMEWORN_FABRIC, Items.LUMINOUS_CRYSTAL_SHARD, Items.ARCANE_SANDS, Items.GODFORGED_PEARL);
        for (int i = 0; i < Purity.values().length - 1; i++) {
            Purity purity = Purity.BY_ID.apply(i);
            List<Holder<Item>> materials = rarityMaterials.subList(Math.max(i - 2, 0), Math.min(i + 2, rarityMaterials.size()));
            int zeroCost = switch (purity) {
                case CRACKED -> 3;
                case CHIPPED -> 9;
                default -> 27;
            };
            addPurityUpgrade(purity, 1 + i * 2, materials, zeroCost);
        }

        out.accept(Apotheosis.loc("potion_charm"), new PotionCharmRecipe("", CraftingBookCategory.MISC, charmPattern()), null);

        addShaped(new ItemStack(Items.IRON_UPGRADE_SMITHING_TEMPLATE, 2), 3, 3, null, Items.MYSTERIOUS_SCRAP_METAL, null, Items.STONE, Items.GEM_FUSED_SLATE, Items.STONE, Items.STONE, Items.GEM_FUSED_SLATE, Items.STONE);
        addShaped(new ItemStack(Items.GOLD_UPGRADE_SMITHING_TEMPLATE, 2), 3, 3, null, Items.TIMEWORN_FABRIC, null, Items.STONE, Items.GEM_FUSED_SLATE, Items.STONE, Items.STONE, Items.GEM_FUSED_SLATE, Items.STONE);
        addShaped(new ItemStack(Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, 2), 3, 3, null, Items.LUMINOUS_CRYSTAL_SHARD, null, Items.STONE, Items.GEM_FUSED_SLATE, Items.STONE, Items.STONE, Items.GEM_FUSED_SLATE, Items.STONE);

        addSizedUpgrade(Apoth.Items.IRON_UPGRADE_SMITHING_TEMPLATE, Items.STONE_SWORD, Tags.Items.INGOTS_IRON, 4, Items.IRON_SWORD);
        addSizedUpgrade(Apoth.Items.IRON_UPGRADE_SMITHING_TEMPLATE, Items.STONE_PICKAXE, Tags.Items.INGOTS_IRON, 4, Items.IRON_PICKAXE);
        addSizedUpgrade(Apoth.Items.IRON_UPGRADE_SMITHING_TEMPLATE, Items.STONE_AXE, Tags.Items.INGOTS_IRON, 4, Items.IRON_AXE);
        addSizedUpgrade(Apoth.Items.IRON_UPGRADE_SMITHING_TEMPLATE, Items.STONE_SHOVEL, Tags.Items.INGOTS_IRON, 4, Items.IRON_SHOVEL);
        addSizedUpgrade(Apoth.Items.IRON_UPGRADE_SMITHING_TEMPLATE, Items.STONE_HOE, Tags.Items.INGOTS_IRON, 4, Items.IRON_HOE);
        addSizedUpgrade(Apoth.Items.IRON_UPGRADE_SMITHING_TEMPLATE, Items.CHAINMAIL_HELMET, Tags.Items.INGOTS_IRON, 4, Items.IRON_HELMET);
        addSizedUpgrade(Apoth.Items.IRON_UPGRADE_SMITHING_TEMPLATE, Items.CHAINMAIL_CHESTPLATE, Tags.Items.INGOTS_IRON, 4, Items.IRON_CHESTPLATE);
        addSizedUpgrade(Apoth.Items.IRON_UPGRADE_SMITHING_TEMPLATE, Items.CHAINMAIL_LEGGINGS, Tags.Items.INGOTS_IRON, 4, Items.IRON_LEGGINGS);
        addSizedUpgrade(Apoth.Items.IRON_UPGRADE_SMITHING_TEMPLATE, Items.CHAINMAIL_BOOTS, Tags.Items.INGOTS_IRON, 4, Items.IRON_BOOTS);

        addSizedUpgrade(Apoth.Items.GOLD_UPGRADE_SMITHING_TEMPLATE, Items.IRON_SWORD, Tags.Items.INGOTS_GOLD, 4, Items.GOLDEN_SWORD);
        addSizedUpgrade(Apoth.Items.GOLD_UPGRADE_SMITHING_TEMPLATE, Items.IRON_PICKAXE, Tags.Items.INGOTS_GOLD, 4, Items.GOLDEN_PICKAXE);
        addSizedUpgrade(Apoth.Items.GOLD_UPGRADE_SMITHING_TEMPLATE, Items.IRON_AXE, Tags.Items.INGOTS_GOLD, 4, Items.GOLDEN_AXE);
        addSizedUpgrade(Apoth.Items.GOLD_UPGRADE_SMITHING_TEMPLATE, Items.IRON_SHOVEL, Tags.Items.INGOTS_GOLD, 4, Items.GOLDEN_SHOVEL);
        addSizedUpgrade(Apoth.Items.GOLD_UPGRADE_SMITHING_TEMPLATE, Items.IRON_HOE, Tags.Items.INGOTS_GOLD, 4, Items.GOLDEN_HOE);
        addSizedUpgrade(Apoth.Items.GOLD_UPGRADE_SMITHING_TEMPLATE, Items.IRON_HELMET, Tags.Items.INGOTS_GOLD, 4, Items.GOLDEN_HELMET);
        addSizedUpgrade(Apoth.Items.GOLD_UPGRADE_SMITHING_TEMPLATE, Items.IRON_CHESTPLATE, Tags.Items.INGOTS_GOLD, 4, Items.GOLDEN_CHESTPLATE);
        addSizedUpgrade(Apoth.Items.GOLD_UPGRADE_SMITHING_TEMPLATE, Items.IRON_LEGGINGS, Tags.Items.INGOTS_GOLD, 4, Items.GOLDEN_LEGGINGS);
        addSizedUpgrade(Apoth.Items.GOLD_UPGRADE_SMITHING_TEMPLATE, Items.IRON_BOOTS, Tags.Items.INGOTS_GOLD, 4, Items.GOLDEN_BOOTS);

        addSizedUpgrade(Apoth.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, Items.GOLDEN_SWORD, Tags.Items.GEMS_DIAMOND, 4, Items.DIAMOND_SWORD);
        addSizedUpgrade(Apoth.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, Items.GOLDEN_PICKAXE, Tags.Items.GEMS_DIAMOND, 4, Items.DIAMOND_PICKAXE);
        addSizedUpgrade(Apoth.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, Items.GOLDEN_AXE, Tags.Items.GEMS_DIAMOND, 4, Items.DIAMOND_AXE);
        addSizedUpgrade(Apoth.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, Items.GOLDEN_SHOVEL, Tags.Items.GEMS_DIAMOND, 4, Items.DIAMOND_SHOVEL);
        addSizedUpgrade(Apoth.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, Items.GOLDEN_HOE, Tags.Items.GEMS_DIAMOND, 4, Items.DIAMOND_HOE);
        addSizedUpgrade(Apoth.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, Items.GOLDEN_HELMET, Tags.Items.GEMS_DIAMOND, 4, Items.DIAMOND_HELMET);
        addSizedUpgrade(Apoth.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, Items.GOLDEN_CHESTPLATE, Tags.Items.GEMS_DIAMOND, 4, Items.DIAMOND_CHESTPLATE);
        addSizedUpgrade(Apoth.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, Items.GOLDEN_LEGGINGS, Tags.Items.GEMS_DIAMOND, 4, Items.DIAMOND_LEGGINGS);
        addSizedUpgrade(Apoth.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, Items.GOLDEN_BOOTS, Tags.Items.GEMS_DIAMOND, 4, Items.DIAMOND_BOOTS);

        // This is a bit of a hack. This provider doesn't currently support conditions, so I wrap this thing to force it to emit them.
        RecipeOutput _out = this.recipeOutput;

        ModLoadedCondition gatewaysLoaded = new ModLoadedCondition(Gateways.MODID);
        ModLoadedCondition enchLoaded = new ModLoadedCondition(ApothicEnchantingCompat.MODID);
        ModLoadedCondition spawnersLoaded = new ModLoadedCondition(ApothicSpawnersCompat.MODID);
        NotCondition enchAbsent = new NotCondition(enchLoaded);

        this.recipeOutput = _out.withConditions(gatewaysLoaded);

        gateRecipe("tiered/frontier",
            Items.SPIDER_EYE, Tags.Items.INGOTS_IRON, Items.SPIDER_EYE,
            Tags.Items.BONES, Tags.Items.ENDER_PEARLS, Tags.Items.BONES,
            Items.ROTTEN_FLESH, Items.ROTTEN_FLESH, Items.ROTTEN_FLESH);

        gateRecipe("tiered/ascent",
            Tags.Items.INGOTS_GOLD, Items.LUMINOUS_CRYSTAL_SHARD, Tags.Items.INGOTS_GOLD,
            Items.LUMINOUS_CRYSTAL_SHARD, Tags.Items.ENDER_PEARLS, Items.LUMINOUS_CRYSTAL_SHARD,
            Items.GEM_DUST, Items.GEM_DUST, Items.GEM_DUST);

        gateRecipe("tiered/summit",
            Items.BLAZE_POWDER, Items.GHAST_TEAR, Items.BLAZE_POWDER,
            Items.ARCANE_SANDS, Items.ENDER_EYE, Items.ARCANE_SANDS,
            Items.GEM_DUST, Items.GEM_DUST, Items.GEM_DUST);

        this.recipeOutput = _out.withConditions(gatewaysLoaded, enchLoaded);

        gateRecipe("tiered/pinnacle",
            Items.SIGIL_OF_MALICE, Ench.Items.WARDEN_TENDRIL, Items.SIGIL_OF_MALICE,
            Ench.Items.INFUSED_BREATH, Items.GODFORGED_PEARL, Ench.Items.INFUSED_BREATH,
            Items.GEM_DUST, Items.GEM_DUST, Items.GEM_DUST);

        this.recipeOutput = _out.withConditions(gatewaysLoaded, enchAbsent);

        // Fallback pinnacle gate recipe for when Apothic Enchanting is not installed.
        gateRecipe(Apotheosis.loc("fallback/gateways/tiered/pinnacle"), "tiered/pinnacle",
            Items.SIGIL_OF_MALICE, Items.ECHO_SHARD, Items.SIGIL_OF_MALICE,
            Items.DRAGON_BREATH, Items.GODFORGED_PEARL, Items.DRAGON_BREATH,
            Items.GEM_DUST, Items.GEM_DUST, Items.GEM_DUST);

        this.recipeOutput = _out.withConditions(spawnersLoaded);
        this.addSpawnerRuneRecipes();
        this.addRuneCraftingRecipes();

        this.recipeOutput = _out.withConditions(spawnersLoaded, enchLoaded);
        this.addInfusedRuneRecipes();

        this.recipeOutput = _out.withConditions(spawnersLoaded, enchAbsent);
        this.addFallbackInfusedRuneRecipes();

        this.recipeOutput = _out.withConditions(enchLoaded);

        this.recipeOutput.accept(Apotheosis.loc("infusion/potion_charm"), new CharmInfusionRecipe(
            new Stats(30F, 100F, 8.5F, 32.5F, 0),
            new Stats(30F, 100F, 13.5F, 37.5F, 0)),
            null);

        this.addInfusion("god_fused_pearl", new ItemStack(Items.GOD_FUSED_PEARL), Items.GODFORGED_PEARL, req(100, 9.65F, 58.75F), req(100, 11F, 60F));
        this.addShaped(Ench.Items.RAVEN_ENCHANTING_TABLE, 3, 3,
            null, perfectRoyal(), null,
            Items.GOD_FUSED_PEARL, Ench.Items.APOTHIC_ENCHANTING_TABLE, Items.GOD_FUSED_PEARL,
            Tags.Items.OBSIDIANS, Tags.Items.OBSIDIANS, Tags.Items.OBSIDIANS);

        this.recipeOutput = _out;

        this.disableSpawnerModifierRecipes();
    }

    /**
     * Emits {@link SpawnerModifier} recipes that consume the rune items registered in {@link Items} and apply
     * stat changes to a spawner. Tier upgrade runes set the five spawn-rate stats to a tier-scaled value;
     * the per-stat runes apply a forward change with an inverse counterpart triggered by a quartz off-hand.
     */
    private void addSpawnerRuneRecipes() {
        // Tier upgrade runes — SET the five core spawn-rate stats. Pinnacle uses the maxes implied by Apothic
        // Spawners' progression advancements (min/max delay 20, spawn count 16, max nearby 32, player range 48).
        this.addTierUpgradeRune("frontier", Items.FRONTIER_SPAWNER_UPGRADE_RUNE, 150, 600, 7, 12, 24);
        this.addTierUpgradeRune("ascent", Items.ASCENT_SPAWNER_UPGRADE_RUNE, 100, 400, 10, 18, 32);
        this.addTierUpgradeRune("summit", Items.SUMMIT_SPAWNER_UPGRADE_RUNE, 50, 200, 13, 24, 40);
        this.addTierUpgradeRune("pinnacle", Items.PINNACLE_SPAWNER_UPGRADE_RUNE, 20, 20, 16, 32, 48);

        this.addRune("spawn_range", Items.SPAWN_RANGE_SPAWNER_RUNE, intChange(SpawnerStats.SPAWN_RANGE, 2, null, 32));
        this.addInverseRune("spawn_range", Items.SPAWN_RANGE_SPAWNER_RUNE, intChange(SpawnerStats.SPAWN_RANGE, -2, 1, null));

        this.addRune("initial_health", Items.INITIAL_HEALTH_SPAWNER_RUNE, floatChange(SpawnerStats.INITIAL_HEALTH, -0.05F, 0.20F, null));
        this.addInverseRune("initial_health", Items.INITIAL_HEALTH_SPAWNER_RUNE, floatChange(SpawnerStats.INITIAL_HEALTH, 0.05F, null, 1.0F));

        this.addRune("ignore_players", Items.IGNORE_PLAYERS_SPAWNER_RUNE, boolSet(SpawnerStats.IGNORE_PLAYERS, true));
        this.addInverseRune("ignore_players", Items.IGNORE_PLAYERS_SPAWNER_RUNE, boolSet(SpawnerStats.IGNORE_PLAYERS, false));

        this.addRune("ignore_conditions", Items.IGNORE_CONDITIONS_SPAWNER_RUNE, boolSet(SpawnerStats.IGNORE_CONDITIONS, true));
        this.addInverseRune("ignore_conditions", Items.IGNORE_CONDITIONS_SPAWNER_RUNE, boolSet(SpawnerStats.IGNORE_CONDITIONS, false));

        this.addRune("redstone_control", Items.REDSTONE_CONTROL_SPAWNER_RUNE, boolSet(SpawnerStats.REDSTONE_CONTROL, true));
        this.addInverseRune("redstone_control", Items.REDSTONE_CONTROL_SPAWNER_RUNE, boolSet(SpawnerStats.REDSTONE_CONTROL, false));

        this.addRune("ignore_light", Items.IGNORE_LIGHT_SPAWNER_RUNE, boolSet(SpawnerStats.IGNORE_LIGHT, true));
        this.addInverseRune("ignore_light", Items.IGNORE_LIGHT_SPAWNER_RUNE, boolSet(SpawnerStats.IGNORE_LIGHT, false));

        this.addRune("no_ai", Items.NO_AI_SPAWNER_RUNE, boolSet(SpawnerStats.NO_AI, true));
        this.addInverseRune("no_ai", Items.NO_AI_SPAWNER_RUNE, boolSet(SpawnerStats.NO_AI, false));

        this.addRune("silent", Items.SILENT_SPAWNER_RUNE, boolSet(SpawnerStats.SILENT, true));
        this.addInverseRune("silent", Items.SILENT_SPAWNER_RUNE, boolSet(SpawnerStats.SILENT, false));

        this.addRune("youthful", Items.YOUTHFUL_SPAWNER_RUNE, boolSet(SpawnerStats.YOUTHFUL, true));
        this.addInverseRune("youthful", Items.YOUTHFUL_SPAWNER_RUNE, boolSet(SpawnerStats.YOUTHFUL, false));

        this.addRune("burning", Items.BURNING_SPAWNER_RUNE, boolSet(SpawnerStats.BURNING, true));
        this.addInverseRune("burning", Items.BURNING_SPAWNER_RUNE, boolSet(SpawnerStats.BURNING, false));

        this.addRune("echoing", Items.ECHOING_SPAWNER_RUNE, intChange(SpawnerStats.ECHOING, 1, null, 3));
        this.addInverseRune("echoing", Items.ECHOING_SPAWNER_RUNE, intChange(SpawnerStats.ECHOING, -1, 0, null));
    }

    /**
     * Crafting recipes for the rune item line. The base {@link Items#SPAWNER_RUNE} is built from a Spawner Chain core,
     * Gem-Fused Slate, and Gem Dust; the Infused variant feeds advanced rune crafts (Summit/Pinnacle tier upgrade
     * runes plus the gameplay-warping stat runes — ignore_players/conditions/light, no_ai, echoing). The remaining stat
     * runes use ingredients reminiscent of Apothic Spawners' original modifier items (piston, dripstone, comparator, ...).
     */
    private void addRuneCraftingRecipes() {
        this.addShaped(new ItemStack(Items.SPAWNER_RUNE, 2), 3, 3,
            Items.GEM_DUST, Items.GEM_FUSED_SLATE, Items.GEM_DUST,
            Items.SPAWNER_CHAIN, Items.GEM_FUSED_SLATE, Items.SPAWNER_CHAIN,
            Items.SPAWNER_CHAIN, Items.SPAWNER_CHAIN, Items.SPAWNER_CHAIN);

        // Tier upgrade runes — rarity material in the corners, themed mats on the cardinals, rune in the center.
        this.addTierRuneRecipe(Items.FRONTIER_SPAWNER_UPGRADE_RUNE, Items.TIMEWORN_FABRIC, Items.SPAWNER_RUNE,
            Items.CLOCK, Items.FLINT, Items.FLINT, Items.CLOCK);

        this.addTierRuneRecipe(Items.ASCENT_SPAWNER_UPGRADE_RUNE, Items.LUMINOUS_CRYSTAL_SHARD, Items.SPAWNER_RUNE,
            new Ingredient(new SpawnEggIngredient()), Tags.Items.GEMS_QUARTZ, Tags.Items.GEMS_QUARTZ, Items.SIGIL_OF_SOCKETING);

        // Per-stat runes — basic Spawner Rune for low-impact stats, Infused Spawner Rune for the gameplay-bending ones.
        this.addStatRuneRecipe(Items.SPAWN_RANGE_SPAWNER_RUNE, Items.SPAWNER_RUNE, Items.PISTON);
        this.addStatRuneRecipe(Items.REDSTONE_CONTROL_SPAWNER_RUNE, Items.SPAWNER_RUNE, Items.COMPARATOR);
        this.addStatRuneRecipe(Items.IGNORE_LIGHT_SPAWNER_RUNE, Items.SPAWNER_RUNE, Items.SOUL_LANTERN);
        this.addStatRuneRecipe(Items.INITIAL_HEALTH_SPAWNER_RUNE, Items.SPAWNER_RUNE, Items.POINTED_DRIPSTONE);
        this.addStatRuneRecipe(Items.SILENT_SPAWNER_RUNE, Items.SPAWNER_RUNE, ItemTags.WOOL);
        this.addStatRuneRecipe(Items.YOUTHFUL_SPAWNER_RUNE, Items.SPAWNER_RUNE, Items.TURTLE_EGG);
        this.addStatRuneRecipe(Items.BURNING_SPAWNER_RUNE, Items.SPAWNER_RUNE, Items.CAMPFIRE);

        this.addShaped(Items.NO_AI_SPAWNER_RUNE, 3, 3,
            null, Items.CHORUS_FRUIT, null,
            Items.CHORUS_FRUIT, Items.INFUSED_SPAWNER_RUNE, Items.CHORUS_FRUIT,
            null, potionIngredient(Potions.FATIGUE), null);

        this.addShaped(Items.IGNORE_CONDITIONS_SPAWNER_RUNE, 3, 3,
            null, Items.CONDUIT, null,
            Tags.Items.ENDER_PEARLS, Items.INFUSED_SPAWNER_RUNE, Tags.Items.ENDER_PEARLS,
            null, Tags.Items.ENDER_PEARLS, null);
        this.addShaped(Items.IGNORE_PLAYERS_SPAWNER_RUNE, 3, 3,
            null, Items.NETHER_STAR, null,
            Items.PHANTOM_MEMBRANE, Items.INFUSED_SPAWNER_RUNE, Items.PHANTOM_MEMBRANE,
            null, Items.ENDER_EYE, null);
        this.addShaped(Items.ECHOING_SPAWNER_RUNE, 3, 3,
            Items.ECHO_SHARD, Items.ECHO_SHARD, Items.ECHO_SHARD,
            Items.ECHO_SHARD, Items.INFUSED_SPAWNER_RUNE, Items.ECHO_SHARD,
            Items.ECHO_SHARD, Items.ECHO_SHARD, Items.ECHO_SHARD);
    }

    /**
     * Rune recipes that involve Apothic Enchanting's infusion mechanic or ingredients, emitted only when both it and
     * Apothic Spawners are installed. {@link #addFallbackInfusedRuneRecipes()} supplies the counterparts.
     */
    private void addInfusedRuneRecipes() {
        this.addInfusion("infused_spawner_rune", new ItemStack(Items.INFUSED_SPAWNER_RUNE), Items.SPAWNER_RUNE, req(70, 30, 50));

        this.addTierRuneRecipe(Items.SUMMIT_SPAWNER_UPGRADE_RUNE, Items.ARCANE_SANDS, Items.INFUSED_SPAWNER_RUNE,
            Ench.Items.WARDEN_TENDRIL, Items.PHANTOM_MEMBRANE, Items.PHANTOM_MEMBRANE, Items.SIGIL_OF_MALICE);

        this.addTierRuneRecipe(Items.PINNACLE_SPAWNER_UPGRADE_RUNE, Items.GODFORGED_PEARL, Items.INFUSED_SPAWNER_RUNE,
            perfectEndersurge(), Ench.Items.INFUSED_BREATH, Ench.Items.INFUSED_BREATH, Items.NETHER_STAR);
    }

    /**
     * Crafting-table fallbacks for the infused rune line, used when Apothic Spawners is installed but Apothic
     * Enchanting is not.
     * 
     * Note that Exp Bottles are pretty hard to come by without AEnch, so this might actually be a harder set of recipes...
     */
    private void addFallbackInfusedRuneRecipes() {
        this.addShaped(Apotheosis.loc("fallback/infused_spawner_rune"), Items.INFUSED_SPAWNER_RUNE, 3, 3,
            Items.GEM_DUST, Items.EXPERIENCE_BOTTLE, Items.GEM_DUST,
            Items.EXPERIENCE_BOTTLE, Items.SPAWNER_RUNE, Items.EXPERIENCE_BOTTLE,
            Items.GEM_DUST, Items.EXPERIENCE_BOTTLE, Items.GEM_DUST);

        this.addTierRuneRecipe(Apotheosis.loc("fallback/summit_spawner_upgrade_rune"), Items.SUMMIT_SPAWNER_UPGRADE_RUNE, Items.ARCANE_SANDS, Items.INFUSED_SPAWNER_RUNE,
            Items.ECHO_SHARD, Items.PHANTOM_MEMBRANE, Items.PHANTOM_MEMBRANE, Items.SIGIL_OF_MALICE);

        this.addTierRuneRecipe(Apotheosis.loc("fallback/pinnacle_spawner_upgrade_rune"), Items.PINNACLE_SPAWNER_UPGRADE_RUNE, Items.GODFORGED_PEARL, Items.INFUSED_SPAWNER_RUNE,
            perfectEndersurge(), Items.DRAGON_BREATH, Items.DRAGON_BREATH, Items.NETHER_STAR);
    }

    private void addTierRuneRecipe(Holder<Item> output, Holder<Item> rarityMat, Holder<Item> rune, Object top, Object left, Object right, Object bottom) {
        this.addShaped(output, 3, 3,
            rarityMat, top, rarityMat,
            left, rune, right,
            rarityMat, bottom, rarityMat);
    }

    private void addTierRuneRecipe(ResourceLocation id, Holder<Item> output, Holder<Item> rarityMat, Holder<Item> rune, Object top, Object left, Object right, Object bottom) {
        this.addShaped(id, output, 3, 3,
            rarityMat, top, rarityMat,
            left, rune, right,
            rarityMat, bottom, rarityMat);
    }

    private void addStatRuneRecipe(Holder<Item> output, Holder<Item> rune, Object material) {
        this.addShaped(output, 3, 3,
            null, material, null,
            material, rune, material,
            null, material, null);
    }

    private void addTierUpgradeRune(String tierName, Holder<Item> rune, int minDelay, int maxDelay, int spawnCount, int maxNearby, int playerRange) {
        SpawnerModifier recipe = new SpawnerModifier(ingredient(rune), Ingredient.EMPTY, false, List.of(
            intSet(SpawnerStats.MIN_DELAY, minDelay),
            intSet(SpawnerStats.MAX_DELAY, maxDelay),
            intSet(SpawnerStats.SPAWN_COUNT, spawnCount),
            intSet(SpawnerStats.MAX_NEARBY_ENTITIES, maxNearby),
            intSet(SpawnerStats.REQ_PLAYER_RANGE, playerRange)));
        this.recipeOutput.accept(Apotheosis.loc("spawner_modifiers/tier/" + tierName), recipe, null);
    }

    private void addRune(String name, Holder<Item> rune, StatModifier<?> change) {
        SpawnerModifier recipe = new SpawnerModifier(ingredient(rune), Ingredient.EMPTY, false, List.of(change));
        this.recipeOutput.accept(Apotheosis.loc("spawner_modifiers/" + name), recipe, null);
    }

    private void addInverseRune(String name, Holder<Item> rune, StatModifier<?> change) {
        Ingredient offhand = Ingredient.of(Items.QUARTZ);
        SpawnerModifier recipe = new SpawnerModifier(ingredient(rune), offhand, false, List.of(change));
        this.recipeOutput.accept(Apotheosis.loc("spawner_modifiers/_inverse/" + name), recipe, null);
    }

    private static StatModifier<Integer> intChange(SpawnerStat<Integer> stat, int value, Integer min, Integer max) {
        return new StatModifier<>(stat, value, Optional.ofNullable(min), Optional.ofNullable(max), Mode.ADD);
    }

    private static StatModifier<Float> floatChange(SpawnerStat<Float> stat, float value, Float min, Float max) {
        return new StatModifier<>(stat, value, Optional.ofNullable(min), Optional.ofNullable(max), Mode.ADD);
    }

    private static StatModifier<Boolean> boolSet(SpawnerStat<Boolean> stat, boolean value) {
        return new StatModifier<>(stat, value, Optional.empty(), Optional.empty(), Mode.SET);
    }

    private static StatModifier<Integer> intSet(SpawnerStat<Integer> stat, int value) {
        return new StatModifier<>(stat, value, Optional.empty(), Optional.empty(), Mode.SET);
    }

    /**
     * Overrides every Apothic Spawners modifier recipe with a {@link FalseCondition}-wrapped placeholder so the
     * modifier system can be replaced by Apotheosis' world-tier-gated equivalents without touching Apothic Spawners.
     */
    private void disableSpawnerModifierRecipes() {
        RecipeOutput disabled = this.recipeOutput.withConditions(FalseCondition.INSTANCE);
        for (String name : AS_MODIFIER_NAMES) {
            ResourceLocation forward = ResourceLocation.fromNamespaceAndPath(ApothicSpawnersCompat.MODID, "spawner_modifiers/" + name);
            ResourceLocation inverse = ResourceLocation.fromNamespaceAndPath(ApothicSpawnersCompat.MODID, "spawner_modifiers/_inverse/" + name);
            // Lazily use the MaliceRecipe because it has no args and the underlying recipe type is irrelevant.
            disabled.accept(forward, new MaliceRecipe(), null);
            disabled.accept(inverse, new MaliceRecipe(), null);
        }
    }

    private static Stats req(float eterna, float quanta, float arcana) {
        return new Stats(30F, eterna, quanta, arcana, 0);
    }

    private void addInfusion(String path, ItemStack output, Object input, Stats requirements) {
        addInfusion(path, output, input, requirements, InfusionRecipe.NO_MAX);
    }

    private void addInfusion(String path, ItemStack output, Object input, Stats requirements, Stats maxRequirements) {
        Ingredient ingredient = createInput(false, input).get(0);
        InfusionRecipe recipe = new InfusionRecipe(output, ingredient, requirements, maxRequirements);
        this.recipeOutput.accept(Apotheosis.loc(path), recipe, null);
    }

    private Ingredient perfectEndersurge() {
        var list = List.of(GemRegistry.INSTANCE.holder(Apotheosis.loc("the_end/endersurge")));
        return new Ingredient(new GemIngredient(list, Purity.PERFECT));
    }

    private Ingredient perfectRoyal() {
        var list = List.of(GemRegistry.INSTANCE.holder(Apotheosis.loc("overworld/royalty")));
        return new Ingredient(new GemIngredient(list, Purity.PERFECT));
    }

    private ShapedRecipePattern charmPattern() {
        Map<Character, Ingredient> key = new HashMap<>();
        key.put('B', Ingredient.of(Items.BLAZE_POWDER));
        key.put('P', Ingredient.of(Items.POTION));
        return ShapedRecipePattern.of(key, "BBB", "PPP", "BBB");
    }

    private void addPurityUpgrade(Purity purity, int gemDust, List<Holder<Item>> materials, int zerothMatCost) {
        SizedIngredient dustIng = SizedIngredient.of(Items.GEM_DUST.value(), gemDust);
        List<SizedIngredient> materialIngs = new ArrayList<>();
        int matAmount = zerothMatCost;
        for (Holder<Item> mat : materials) {
            materialIngs.add(SizedIngredient.of(mat.value(), matAmount));
            matAmount /= 3;
        }
        var recipe = new PurityUpgradeRecipe(purity, List.of(dustIng), materialIngs);
        this.recipeOutput.accept(Apotheosis.loc("gem_cutting/" + purity.name().toLowerCase(Locale.ROOT)), recipe, null);
    }

    @SafeVarargs
    private void addReforging(String rarity, int mats, int sigils, int levels, Holder<Block>... tables) {
        DynamicHolder<LootRarity> lRarity = RarityRegistry.INSTANCE.holder(Apotheosis.loc(rarity));
        this.recipeOutput.accept(Apotheosis.loc("reforging/" + rarity), new ReforgingRecipe(lRarity, mats, sigils, levels, HolderSet.direct(tables)), null);
    }

    private void addGemSalvaging(Purity purity, int min, int max) {
        Ingredient input = new Ingredient(new GemIngredient(purity));
        OutputData output = new OutputData(new ItemStack(Items.GEM_DUST), min, max);
        addSalvaging("gem/" + purity.getSerializedName(), input, output);
    }

    private void addAffixSalvaging(String rarity, Holder<Item> material) {
        Ingredient input = new Ingredient(new AffixItemIngredient(RarityRegistry.INSTANCE.holder(Apotheosis.loc(rarity))));
        OutputData output = new OutputData(new ItemStack(material), 1, 4);
        addSalvaging("affix_item/" + rarity, input, output);
    }

    private void addOtherSalvaging(String path, OutputData output, Item... inputs) {
        addSalvaging("salvaging/other/" + path, Ingredient.of(inputs), List.of(output));
    }

    private void addSalvaging(String path, Ingredient input, OutputData output) {
        addSalvaging("salvaging/" + path, input, List.of(output));
    }

    private void addSalvaging(String path, Ingredient input, List<OutputData> outputs) {
        this.recipeOutput.accept(Apotheosis.loc(path), new SalvagingRecipe(input, outputs), null);
    }

    private void addSockets(String path, Ingredient input, int maxSockets) {
        this.recipeOutput.accept(Apotheosis.loc(path), new AddSocketsRecipe(input, maxSockets), null);
    }

    private static <T extends ItemLike> Ingredient ingredient(Holder<T> holder) {
        return Ingredient.of(holder.value());
    }

    private void addSizedUpgrade(Holder<Item> template, Item base, TagKey<Item> addition, int size, Item output) {
        String path1 = BuiltInRegistries.ITEM.getKey(base).getPath();
        String path2 = BuiltInRegistries.ITEM.getKey(output).getPath();
        this.recipeOutput.accept(Apotheosis.loc("smithing/upgrade_%s_to_%s".formatted(path1, path2)),
            new SizedUpgradeRecipe(Ingredient.of(template.value()), Ingredient.of(base), SizedIngredient.of(addition, size), output.getDefaultInstance()), null);
    }

    private void gateRecipe(String gatePath, Object... pattern) {
        this.gateRecipe(Apotheosis.loc("gateways/" + gatePath), gatePath, pattern);
    }

    private void gateRecipe(ResourceLocation id, String gatePath, Object... pattern) {
        ItemStack output = new ItemStack(GatewayObjects.GATE_PEARL);
        GatePearlItem.setGate(output, GatewayRegistry.INSTANCE.holder(Apotheosis.loc(gatePath)));
        addShaped(id, output, 3, 3, pattern);
    }

}
