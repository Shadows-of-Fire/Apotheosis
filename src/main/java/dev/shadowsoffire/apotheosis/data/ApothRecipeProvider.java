package dev.shadowsoffire.apotheosis.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Blocks;
import dev.shadowsoffire.apotheosis.Apoth.Items;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.UnnamingRecipe;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingRecipe;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipe;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipe.OutputData;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.recipe.CharmInfusionRecipe;
import dev.shadowsoffire.apotheosis.recipe.MaliceRecipe;
import dev.shadowsoffire.apotheosis.recipe.PotionCharmRecipe;
import dev.shadowsoffire.apotheosis.recipe.SupremacyRecipe;
import dev.shadowsoffire.apotheosis.socket.AddSocketsRecipe;
import dev.shadowsoffire.apotheosis.socket.SocketingRecipe;
import dev.shadowsoffire.apotheosis.socket.WithdrawalRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.PurityUpgradeRecipe;
import dev.shadowsoffire.apotheosis.util.AffixItemIngredient;
import dev.shadowsoffire.apotheosis.util.GemIngredient;
import dev.shadowsoffire.apotheosis.util.SizedUpgradeRecipe;
import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.apothic_enchanting.table.EnchantingStatRegistry.Stats;
import dev.shadowsoffire.gateways.GatewayObjects;
import dev.shadowsoffire.gateways.Gateways;
import dev.shadowsoffire.gateways.gate.GatewayRegistry;
import dev.shadowsoffire.placebo.datagen.LegacyRecipeProvider;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class ApothRecipeProvider extends LegacyRecipeProvider {

    public ApothRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Apotheosis.MODID);
    }

    @Override
    public String getName() {
        return "Apotheosis Recipes";
    }

    private static ResourceKey<Recipe<?>> key(Identifier id) {
        return ResourceKey.create(Registries.RECIPE, id);
    }

    @Override
    protected void genRecipes(RecipeOutput out, HolderLookup.Provider registries) {
        out.accept(key(Apotheosis.loc("socketing")), new SocketingRecipe(), null);
        out.accept(key(Apotheosis.loc("unnaming")), new UnnamingRecipe(), null);
        out.accept(key(Apotheosis.loc("widthdrawal")), new WithdrawalRecipe(), null);
        out.accept(key(Apotheosis.loc("malice")), new MaliceRecipe(), null);
        out.accept(key(Apotheosis.loc("supremacy")), new SupremacyRecipe(), null);
        this.addSockets("sigil_add_sockets", ingredient(Items.SIGIL_OF_SOCKETING), 2);
        this.addAffixSalvaging("common", Items.COMMON_MATERIAL);
        this.addAffixSalvaging("uncommon", Items.UNCOMMON_MATERIAL);
        this.addAffixSalvaging("rare", Items.RARE_MATERIAL);
        this.addAffixSalvaging("epic", Items.EPIC_MATERIAL);
        this.addAffixSalvaging("mythic", Items.MYTHIC_MATERIAL);

        this.addGemSalvaging(Purity.CRACKED, 1, 2);
        this.addGemSalvaging(Purity.CHIPPED, 1, 3);
        this.addGemSalvaging(Purity.FLAWED, 2, 4);
        this.addGemSalvaging(Purity.NORMAL, 3, 5);
        this.addGemSalvaging(Purity.FLAWLESS, 4, 7);
        this.addGemSalvaging(Purity.PERFECT, 5, 10);

        this.addOtherSalvaging("leather_horse_armor", new OutputData(Items.LEATHER, 3, 8), Items.LEATHER_HORSE_ARMOR);
        this.addOtherSalvaging("copper_horse_armor", new OutputData(Items.COPPER_INGOT, 3, 8), Items.COPPER_HORSE_ARMOR);
        this.addOtherSalvaging("iron_horse_armor", new OutputData(Items.IRON_INGOT, 3, 8), Items.IRON_HORSE_ARMOR);
        this.addOtherSalvaging("golden_horse_armor", new OutputData(Items.GOLD_INGOT, 3, 8), Items.GOLDEN_HORSE_ARMOR);
        this.addOtherSalvaging("diamond_horse_armor", new OutputData(Items.DIAMOND, 3, 8), Items.DIAMOND_HORSE_ARMOR);
        this.addOtherSalvaging("netherite_horse_armor", new OutputData(Items.NETHERITE_SCRAP, 1, 3), Items.NETHERITE_HORSE_ARMOR);
        this.addOtherSalvaging("wolf_armor", new OutputData(Items.ARMADILLO_SCUTE, 1, 3), Items.WOLF_ARMOR);

        this.addOtherSalvaging("wooden_tools", new OutputData(Items.OAK_PLANKS, 0, 1), Items.WOODEN_SWORD, Items.WOODEN_PICKAXE, Items.WOODEN_AXE, Items.WOODEN_SHOVEL, Items.WOODEN_HOE, Items.WOODEN_SPEAR);
        this.addOtherSalvaging("stone_tools", new OutputData(Items.COBBLESTONE, 0, 1), Items.STONE_SWORD, Items.STONE_PICKAXE, Items.STONE_AXE, Items.STONE_SHOVEL, Items.STONE_HOE, Items.STONE_SPEAR);
        this.addOtherSalvaging("copper_tools", new OutputData(Items.COPPER_INGOT, 0, 1), Items.COPPER_SWORD, Items.COPPER_PICKAXE, Items.COPPER_AXE, Items.COPPER_SHOVEL, Items.COPPER_HOE, Items.COPPER_SPEAR);
        this.addOtherSalvaging("gold_tools", new OutputData(Items.GOLD_INGOT, 0, 1), Items.GOLDEN_SWORD, Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_HOE, Items.GOLDEN_SPEAR);
        this.addOtherSalvaging("iron_tools", new OutputData(Items.IRON_INGOT, 0, 1), Items.IRON_SWORD, Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_HOE, Items.IRON_SPEAR);
        this.addOtherSalvaging("diamond_tools", new OutputData(Items.DIAMOND, 0, 1), Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE, Items.DIAMOND_SPEAR);
        this.addOtherSalvaging("netherite_tools", new OutputData(Items.NETHERITE_SCRAP, 0, 2), Items.NETHERITE_SWORD, Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_HOE, Items.NETHERITE_SPEAR);

        this.addOtherSalvaging("leather_armor", new OutputData(Items.LEATHER, 1, 3), Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
        this.addOtherSalvaging("chain_armor", new OutputData(Items.IRON_CHAIN, 1, 3), Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS);
        this.addOtherSalvaging("copper_armor", new OutputData(Items.COPPER_INGOT, 1, 3), Items.COPPER_HELMET, Items.COPPER_CHESTPLATE, Items.COPPER_LEGGINGS, Items.COPPER_BOOTS);
        this.addOtherSalvaging("gold_armor", new OutputData(Items.GOLD_INGOT, 1, 3), Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);
        this.addOtherSalvaging("iron_armor", new OutputData(Items.IRON_INGOT, 1, 3), Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
        this.addOtherSalvaging("diamond_armor", new OutputData(Items.DIAMOND, 1, 3), Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
        this.addOtherSalvaging("netherite_armor", new OutputData(Items.NETHERITE_SCRAP, 0, 2), Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);

        this.addOtherSalvaging("copper_nautilus_armor", new OutputData(Items.COPPER_INGOT, 1, 3), Items.COPPER_NAUTILUS_ARMOR);
        this.addOtherSalvaging("iron_nautilus_armor", new OutputData(Items.IRON_INGOT, 1, 3), Items.IRON_NAUTILUS_ARMOR);
        this.addOtherSalvaging("golden_nautilus_armor", new OutputData(Items.GOLD_INGOT, 1, 3), Items.GOLDEN_NAUTILUS_ARMOR);
        this.addOtherSalvaging("diamond_nautilus_armor", new OutputData(Items.DIAMOND, 1, 3), Items.DIAMOND_NAUTILUS_ARMOR);
        this.addOtherSalvaging("netherite_nautilus_armor", new OutputData(Items.NETHERITE_SCRAP, 0, 2), Items.NETHERITE_NAUTILUS_ARMOR);

        this.addReforging("common", 1, 0, 2, Blocks.SIMPLE_REFORGING_TABLE, Blocks.REFORGING_TABLE);
        this.addReforging("uncommon", 2, 1, 5, Blocks.SIMPLE_REFORGING_TABLE, Blocks.REFORGING_TABLE);
        this.addReforging("rare", 2, 2, 15, Blocks.SIMPLE_REFORGING_TABLE, Blocks.REFORGING_TABLE);
        this.addReforging("epic", 2, 4, 30, Blocks.REFORGING_TABLE);
        this.addReforging("mythic", 3, 5, 50, Blocks.REFORGING_TABLE);

        this.addShaped(Blocks.AUGMENTING_TABLE, 3, 3, null, Items.NETHER_STAR, null, Items.MYTHIC_MATERIAL, Items.ENCHANTING_TABLE, Items.MYTHIC_MATERIAL, Items.POLISHED_BLACKSTONE, Items.POLISHED_BLACKSTONE, Items.POLISHED_BLACKSTONE);
        this.addShaped(Blocks.GEM_CUTTING_TABLE, 3, 3, Items.SMOOTH_STONE, Items.SHEARS, Items.SMOOTH_STONE, ItemTags.PLANKS, Items.GEM_DUST, ItemTags.PLANKS, ItemTags.PLANKS, null, ItemTags.PLANKS);
        this.addShaped(new ItemStackTemplate(Items.GEM_FUSED_SLATE, 8), 3, 3, Items.DEEPSLATE, Items.DEEPSLATE, Items.DEEPSLATE, Items.DEEPSLATE, Items.GEM_DUST, Items.DEEPSLATE, Items.DEEPSLATE, Items.DEEPSLATE, Items.DEEPSLATE);
        this.addShaped(Blocks.REFORGING_TABLE, 3, 3, null, Tags.Items.INGOTS_NETHERITE, null, Items.EPIC_MATERIAL, Items.SIMPLE_REFORGING_TABLE, Items.EPIC_MATERIAL, Items.NETHER_BRICKS, Items.NETHER_BRICKS, Items.NETHER_BRICKS);
        this.addShaped(Blocks.SALVAGING_TABLE, 3, 3, Tags.Items.INGOTS_COPPER, Tags.Items.INGOTS_COPPER, Tags.Items.INGOTS_COPPER, Items.IRON_PICKAXE, Items.SMITHING_TABLE, Items.IRON_AXE, Items.GEM_DUST, Items.LAVA_BUCKET, Items.GEM_DUST);
        this.addShaped(Blocks.SIMPLE_REFORGING_TABLE, 3, 3, null, Tags.Items.INGOTS_IRON, null, Items.GEM_DUST, Items.ENCHANTING_TABLE, Items.GEM_DUST, Items.SMOOTH_STONE, Items.SMOOTH_STONE, Items.SMOOTH_STONE);

        this.addShaped(Blocks.GEM_CASE, 3, 3, Tags.Items.GLASS_BLOCKS, Tags.Items.GLASS_BLOCKS, Tags.Items.GLASS_BLOCKS, Items.BASALT, Items.GEM_CUTTING_TABLE, Items.BASALT, Items.BASALT, Items.ENDER_CHEST, Items.BASALT);

        this.addShaped(new ItemStackTemplate(Items.SIGIL_OF_ENHANCEMENT, 4), 3, 3, Items.GEM_DUST, Items.GEM_FUSED_SLATE, Items.GEM_DUST, Items.GEM_FUSED_SLATE, Items.MYTHIC_MATERIAL, Items.GEM_FUSED_SLATE, Items.GEM_DUST, Items.GEM_FUSED_SLATE,
            Items.GEM_DUST);
        this.addShaped(new ItemStackTemplate(Items.SIGIL_OF_REBIRTH, 6), 3, 3, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE, Items.GEM_DUST, Items.GEM_DUST, Items.GEM_DUST, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE,
            Items.GEM_FUSED_SLATE);
        this.addShaped(new ItemStackTemplate(Items.SIGIL_OF_SOCKETING, 3), 3, 3, Items.GEM_DUST, Items.GUNPOWDER, Items.GEM_DUST, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE, Items.GEM_DUST, Items.AMETHYST_SHARD,
            Items.GEM_DUST);
        this.addShaped(new ItemStackTemplate(Items.SIGIL_OF_UNNAMING, 6), 3, 3, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE, Items.FLINT, Items.FLINT, Items.FLINT, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE,
            Items.GEM_FUSED_SLATE);
        this.addShaped(new ItemStackTemplate(Items.SIGIL_OF_WITHDRAWAL, 4), 3, 3, Items.GEM_FUSED_SLATE, Items.BLAZE_ROD, Items.GEM_FUSED_SLATE, Tags.Items.ENDER_PEARLS, Items.LAVA_BUCKET, Tags.Items.ENDER_PEARLS, Items.GEM_FUSED_SLATE,
            Items.GEM_DUST, Items.GEM_FUSED_SLATE);

        List<Holder<Item>> rarityMaterials = List.of(Items.COMMON_MATERIAL, Items.UNCOMMON_MATERIAL, Items.RARE_MATERIAL, Items.EPIC_MATERIAL, Items.MYTHIC_MATERIAL);
        for (int i = 0; i < Purity.values().length - 1; i++) {
            Purity purity = Purity.BY_ID.apply(i);
            List<Holder<Item>> materials = rarityMaterials.subList(Math.max(i - 2, 0), Math.min(i + 2, rarityMaterials.size()));
            int zeroCost = switch (purity) {
                case CRACKED -> 3;
                case CHIPPED -> 9;
                default -> 27;
            };
            this.addPurityUpgrade(purity, 1 + i * 2, materials, zeroCost);
        }

        out.accept(key(Apotheosis.loc("potion_charm")), new PotionCharmRecipe(new net.minecraft.world.item.crafting.Recipe.CommonInfo(true), new net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""), this.charmPattern()), null);

        out.accept(key(Apotheosis.loc("infusion/potion_charm")), new CharmInfusionRecipe(
            new Stats(15F, 100F, 8.5F, 32.5F, 0),
            new Stats(15F, 100F, 13.5F, 37.5F, 0)),
            null);

        this.addShaped(new ItemStackTemplate(Items.IRON_UPGRADE_SMITHING_TEMPLATE, 2), 3, 3, null, Items.COMMON_MATERIAL, null, Items.STONE, Items.GEM_FUSED_SLATE, Items.STONE, Items.STONE, Items.GEM_FUSED_SLATE, Items.STONE);
        this.addShaped(new ItemStackTemplate(Items.GOLD_UPGRADE_SMITHING_TEMPLATE, 2), 3, 3, null, Items.UNCOMMON_MATERIAL, null, Items.STONE, Items.GEM_FUSED_SLATE, Items.STONE, Items.STONE, Items.GEM_FUSED_SLATE, Items.STONE);
        this.addShaped(new ItemStackTemplate(Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, 2), 3, 3, null, Items.RARE_MATERIAL, null, Items.STONE, Items.GEM_FUSED_SLATE, Items.STONE, Items.STONE, Items.GEM_FUSED_SLATE, Items.STONE);

        this.addSizedUpgrade(Apoth.Items.IRON_UPGRADE_SMITHING_TEMPLATE, Items.STONE_SWORD, Tags.Items.INGOTS_IRON, 4, Items.IRON_SWORD);
        this.addSizedUpgrade(Apoth.Items.IRON_UPGRADE_SMITHING_TEMPLATE, Items.STONE_PICKAXE, Tags.Items.INGOTS_IRON, 4, Items.IRON_PICKAXE);
        this.addSizedUpgrade(Apoth.Items.IRON_UPGRADE_SMITHING_TEMPLATE, Items.STONE_AXE, Tags.Items.INGOTS_IRON, 4, Items.IRON_AXE);
        this.addSizedUpgrade(Apoth.Items.IRON_UPGRADE_SMITHING_TEMPLATE, Items.STONE_SHOVEL, Tags.Items.INGOTS_IRON, 4, Items.IRON_SHOVEL);
        this.addSizedUpgrade(Apoth.Items.IRON_UPGRADE_SMITHING_TEMPLATE, Items.STONE_HOE, Tags.Items.INGOTS_IRON, 4, Items.IRON_HOE);
        this.addSizedUpgrade(Apoth.Items.IRON_UPGRADE_SMITHING_TEMPLATE, Items.CHAINMAIL_HELMET, Tags.Items.INGOTS_IRON, 4, Items.IRON_HELMET);
        this.addSizedUpgrade(Apoth.Items.IRON_UPGRADE_SMITHING_TEMPLATE, Items.CHAINMAIL_CHESTPLATE, Tags.Items.INGOTS_IRON, 4, Items.IRON_CHESTPLATE);
        this.addSizedUpgrade(Apoth.Items.IRON_UPGRADE_SMITHING_TEMPLATE, Items.CHAINMAIL_LEGGINGS, Tags.Items.INGOTS_IRON, 4, Items.IRON_LEGGINGS);
        this.addSizedUpgrade(Apoth.Items.IRON_UPGRADE_SMITHING_TEMPLATE, Items.CHAINMAIL_BOOTS, Tags.Items.INGOTS_IRON, 4, Items.IRON_BOOTS);

        this.addSizedUpgrade(Apoth.Items.GOLD_UPGRADE_SMITHING_TEMPLATE, Items.IRON_SWORD, Tags.Items.INGOTS_GOLD, 4, Items.GOLDEN_SWORD);
        this.addSizedUpgrade(Apoth.Items.GOLD_UPGRADE_SMITHING_TEMPLATE, Items.IRON_PICKAXE, Tags.Items.INGOTS_GOLD, 4, Items.GOLDEN_PICKAXE);
        this.addSizedUpgrade(Apoth.Items.GOLD_UPGRADE_SMITHING_TEMPLATE, Items.IRON_AXE, Tags.Items.INGOTS_GOLD, 4, Items.GOLDEN_AXE);
        this.addSizedUpgrade(Apoth.Items.GOLD_UPGRADE_SMITHING_TEMPLATE, Items.IRON_SHOVEL, Tags.Items.INGOTS_GOLD, 4, Items.GOLDEN_SHOVEL);
        this.addSizedUpgrade(Apoth.Items.GOLD_UPGRADE_SMITHING_TEMPLATE, Items.IRON_HOE, Tags.Items.INGOTS_GOLD, 4, Items.GOLDEN_HOE);
        this.addSizedUpgrade(Apoth.Items.GOLD_UPGRADE_SMITHING_TEMPLATE, Items.IRON_HELMET, Tags.Items.INGOTS_GOLD, 4, Items.GOLDEN_HELMET);
        this.addSizedUpgrade(Apoth.Items.GOLD_UPGRADE_SMITHING_TEMPLATE, Items.IRON_CHESTPLATE, Tags.Items.INGOTS_GOLD, 4, Items.GOLDEN_CHESTPLATE);
        this.addSizedUpgrade(Apoth.Items.GOLD_UPGRADE_SMITHING_TEMPLATE, Items.IRON_LEGGINGS, Tags.Items.INGOTS_GOLD, 4, Items.GOLDEN_LEGGINGS);
        this.addSizedUpgrade(Apoth.Items.GOLD_UPGRADE_SMITHING_TEMPLATE, Items.IRON_BOOTS, Tags.Items.INGOTS_GOLD, 4, Items.GOLDEN_BOOTS);

        this.addSizedUpgrade(Apoth.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, Items.GOLDEN_SWORD, Tags.Items.GEMS_DIAMOND, 4, Items.DIAMOND_SWORD);
        this.addSizedUpgrade(Apoth.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, Items.GOLDEN_PICKAXE, Tags.Items.GEMS_DIAMOND, 4, Items.DIAMOND_PICKAXE);
        this.addSizedUpgrade(Apoth.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, Items.GOLDEN_AXE, Tags.Items.GEMS_DIAMOND, 4, Items.DIAMOND_AXE);
        this.addSizedUpgrade(Apoth.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, Items.GOLDEN_SHOVEL, Tags.Items.GEMS_DIAMOND, 4, Items.DIAMOND_SHOVEL);
        this.addSizedUpgrade(Apoth.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, Items.GOLDEN_HOE, Tags.Items.GEMS_DIAMOND, 4, Items.DIAMOND_HOE);
        this.addSizedUpgrade(Apoth.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, Items.GOLDEN_HELMET, Tags.Items.GEMS_DIAMOND, 4, Items.DIAMOND_HELMET);
        this.addSizedUpgrade(Apoth.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, Items.GOLDEN_CHESTPLATE, Tags.Items.GEMS_DIAMOND, 4, Items.DIAMOND_CHESTPLATE);
        this.addSizedUpgrade(Apoth.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, Items.GOLDEN_LEGGINGS, Tags.Items.GEMS_DIAMOND, 4, Items.DIAMOND_LEGGINGS);
        this.addSizedUpgrade(Apoth.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE, Items.GOLDEN_BOOTS, Tags.Items.GEMS_DIAMOND, 4, Items.DIAMOND_BOOTS);

        // This is a bit of a hack. This provider doesn't currently support conditions, so I wrap this thing to force it to emit them.
        RecipeOutput _out = this.recipeOutput;

        this.recipeOutput = _out.withConditions(new ModLoadedCondition(Gateways.MODID));

        this.gateRecipe("tiered/frontier",
            Items.SPIDER_EYE, Tags.Items.INGOTS_IRON, Items.SPIDER_EYE,
            Tags.Items.BONES, Tags.Items.ENDER_PEARLS, Tags.Items.BONES,
            Items.ROTTEN_FLESH, Items.ROTTEN_FLESH, Items.ROTTEN_FLESH);

        this.gateRecipe("tiered/ascent",
            Tags.Items.INGOTS_GOLD, Items.RARE_MATERIAL, Tags.Items.INGOTS_GOLD,
            Items.RARE_MATERIAL, Tags.Items.ENDER_PEARLS, Items.RARE_MATERIAL,
            Items.GEM_DUST, Items.GEM_DUST, Items.GEM_DUST);

        this.gateRecipe("tiered/summit",
            Items.BLAZE_POWDER, Items.GHAST_TEAR, Items.BLAZE_POWDER,
            Items.EPIC_MATERIAL, Items.ENDER_EYE, Items.EPIC_MATERIAL,
            Items.GEM_DUST, Items.GEM_DUST, Items.GEM_DUST);

        this.gateRecipe("tiered/pinnacle",
            Items.SIGIL_OF_MALICE, Ench.Items.WARDEN_TENDRIL, Items.SIGIL_OF_MALICE,
            Ench.Items.INFUSED_BREATH, Items.MYTHIC_MATERIAL, Ench.Items.INFUSED_BREATH,
            Items.GEM_DUST, Items.GEM_DUST, Items.GEM_DUST);

        this.recipeOutput = _out;
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
        this.recipeOutput.accept(key(Apotheosis.loc("gem_cutting/" + purity.name().toLowerCase(Locale.ROOT))), recipe, null);
    }

    @SafeVarargs
    private void addReforging(String rarity, int mats, int sigils, int levels, Holder<Block>... tables) {
        DynamicHolder<LootRarity> lRarity = RarityRegistry.INSTANCE.holder(Apotheosis.loc(rarity));
        this.recipeOutput.accept(key(Apotheosis.loc("reforging/" + rarity)), new ReforgingRecipe(lRarity, mats, sigils, levels, HolderSet.direct(tables)), null);
    }

    private void addGemSalvaging(Purity purity, int min, int max) {
        Ingredient input = new Ingredient(new GemIngredient(purity));
        OutputData output = new OutputData(Items.GEM_DUST.value(), min, max);
        this.addSalvaging("gem/" + purity.getSerializedName(), input, output);
    }

    private void addAffixSalvaging(String rarity, Holder<Item> material) {
        Ingredient input = new Ingredient(new AffixItemIngredient(RarityRegistry.INSTANCE.holder(Apotheosis.loc(rarity))));
        OutputData output = new OutputData(material.value(), 1, 4);
        this.addSalvaging("affix_item/" + rarity, input, output);
    }

    private void addOtherSalvaging(String path, OutputData output, Item... inputs) {
        this.addSalvaging("salvaging/other/" + path, Ingredient.of(inputs), List.of(output));
    }

    private void addSalvaging(String path, Ingredient input, OutputData output) {
        this.addSalvaging("salvaging/" + path, input, List.of(output));
    }

    private void addSalvaging(String path, Ingredient input, List<OutputData> outputs) {
        this.recipeOutput.accept(key(Apotheosis.loc(path)), new SalvagingRecipe(input, outputs), null);
    }

    private void addSockets(String path, Ingredient input, int maxSockets) {
        this.recipeOutput.accept(key(Apotheosis.loc(path)), new AddSocketsRecipe(input, maxSockets), null);
    }

    private static <T extends ItemLike> Ingredient ingredient(Holder<T> holder) {
        return Ingredient.of(holder.value());
    }

    private void addSizedUpgrade(Holder<Item> template, Item base, TagKey<Item> addition, int size, Item output) {
        String path1 = BuiltInRegistries.ITEM.getKey(base).getPath();
        String path2 = BuiltInRegistries.ITEM.getKey(output).getPath();
        HolderSet<Item> additionTag = this.currentRegistries.lookupOrThrow(net.minecraft.core.registries.Registries.ITEM).getOrThrow(addition);
        this.recipeOutput.accept(key(Apotheosis.loc("smithing/upgrade_%s_to_%s".formatted(path1, path2))),
            new SizedUpgradeRecipe(Ingredient.of(template.value()), Ingredient.of(base), new SizedIngredient(Ingredient.of(additionTag), size), new ItemStackTemplate(output)), null);
    }

    @SuppressWarnings("deprecation")
    private void gateRecipe(String gatePath, Object... pattern) {
        Identifier key = Apotheosis.loc(gatePath);
        DataComponentPatch patch = DataComponentPatch.builder()
            .set(GatewayObjects.GATEWAY_COMPONENT, GatewayRegistry.INSTANCE.holder(key))
            .build();
        ItemStackTemplate output = new ItemStackTemplate(GatewayObjects.GATE_PEARL.value().builtInRegistryHolder(), 1, patch);
        this.addShaped(Apotheosis.loc("gateways/" + gatePath), output, 3, 3, pattern);
    }

}
