package dev.shadowsoffire.apotheosis.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.compat.enchanting.ApothicEnchantingCompat;
import dev.shadowsoffire.apotheosis.compat.spawners.ApothicSpawnersCompat;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.placebo.json.WeightedItemStack;
import dev.shadowsoffire.placebo.systems.gear.GearSet;
import dev.shadowsoffire.placebo.systems.gear.GearSetRegistry;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

public class GearSetProvider extends DynamicRegistryProvider<GearSet> {

    public static final int DEFAULT_WEIGHT = 100;

    /**
     * Tag id (without leading '#') → list of gear set entry ids that should be members of that tag. Populated by
     * {@link GearSetProvider#addSet} via {@link GSBuilder#tag(String)} declarations and consumed by
     * {@link GearSetTagsProvider} to emit the corresponding tag JSON files.
     */
    public static final Map<String, List<Identifier>> TAG_ASSOCIATIONS = new LinkedHashMap<>();

    /**
     * Same as {@link #TAG_ASSOCIATIONS}, but for gear sets that are registered conditionally. These must be emitted as optional
     * tag entries, since a missing required entry causes the entire tag to be skipped when the condition fails.
     */
    public static final Map<String, List<Identifier>> OPTIONAL_TAG_ASSOCIATIONS = new LinkedHashMap<>();

    public GearSetProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries, GearSetRegistry.INSTANCE);
    }

    @Override
    public String getName() {
        return "Gear Sets";
    }

    @Override
    public void generate() {
        HolderLookup.Provider registries = this.lookupProvider.join();
        RegistryLookup<Enchantment> enchants = registries.lookup(Registries.ENCHANTMENT).get();

        // Haven Sets
        this.addSet("haven/leather", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(new ItemStackTemplate(Items.STONE_SWORD), 10)
            .mainhand(new ItemStackTemplate(Items.STONE_AXE), 10)
            .mainhand(new ItemStackTemplate(Items.STONE_PICKAXE), 10)
            .mainhand(new ItemStackTemplate(Items.STONE_SHOVEL), 10)
            .helmet(new ItemStackTemplate(Items.LEATHER_HELMET), 10)
            .chestplate(new ItemStackTemplate(Items.LEATHER_CHESTPLATE), 10)
            .leggings(new ItemStackTemplate(Items.LEATHER_LEGGINGS), 10)
            .boots(new ItemStackTemplate(Items.LEATHER_BOOTS), 10)
            .tag("haven_melee"));

        this.addSet("haven/ranged/leather", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(new ItemStackTemplate(Items.BOW), 16)
            .mainhand(new ItemStackTemplate(Items.CROSSBOW), 4)
            .helmet(new ItemStackTemplate(Items.LEATHER_HELMET), 10)
            .chestplate(new ItemStackTemplate(Items.LEATHER_CHESTPLATE), 10)
            .leggings(new ItemStackTemplate(Items.LEATHER_LEGGINGS), 10)
            .boots(new ItemStackTemplate(Items.LEATHER_BOOTS), 10)
            .tag("haven_ranged"));

        this.addSet("haven/chain", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(new ItemStackTemplate(Items.STONE_SWORD), 10)
            .mainhand(new ItemStackTemplate(Items.STONE_AXE), 10)
            .mainhand(new ItemStackTemplate(Items.STONE_PICKAXE), 10)
            .mainhand(new ItemStackTemplate(Items.STONE_SHOVEL), 10)
            .helmet(new ItemStackTemplate(Items.CHAINMAIL_HELMET), 10)
            .chestplate(new ItemStackTemplate(Items.CHAINMAIL_CHESTPLATE), 10)
            .leggings(new ItemStackTemplate(Items.CHAINMAIL_LEGGINGS), 10)
            .boots(new ItemStackTemplate(Items.CHAINMAIL_BOOTS), 10)
            .tag("haven_melee"));

        this.addSet("haven/ranged/chain", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(new ItemStackTemplate(Items.BOW), 16)
            .mainhand(new ItemStackTemplate(Items.CROSSBOW), 4)
            .helmet(new ItemStackTemplate(Items.CHAINMAIL_HELMET), 10)
            .chestplate(new ItemStackTemplate(Items.CHAINMAIL_CHESTPLATE), 10)
            .leggings(new ItemStackTemplate(Items.CHAINMAIL_LEGGINGS), 10)
            .boots(new ItemStackTemplate(Items.CHAINMAIL_BOOTS), 10)
            .tag("haven_ranged"));

        // Frontier Sets
        this.addSet("frontier/chain", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(new ItemStackTemplate(Items.IRON_SWORD), 10)
            .mainhand(new ItemStackTemplate(Items.IRON_AXE), 10)
            .mainhand(new ItemStackTemplate(Items.IRON_PICKAXE), 10)
            .mainhand(new ItemStackTemplate(Items.IRON_SHOVEL), 10)
            .helmet(new ItemStackTemplate(Items.CHAINMAIL_HELMET), 10)
            .chestplate(new ItemStackTemplate(Items.CHAINMAIL_CHESTPLATE), 10)
            .leggings(new ItemStackTemplate(Items.CHAINMAIL_LEGGINGS), 10)
            .boots(new ItemStackTemplate(Items.CHAINMAIL_BOOTS), 10)
            .tag("frontier_melee"));

        this.addSet("frontier/ranged/chain", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(new ItemStackTemplate(Items.BOW), 16)
            .mainhand(new ItemStackTemplate(Items.CROSSBOW), 4)
            .helmet(new ItemStackTemplate(Items.CHAINMAIL_HELMET), 10)
            .chestplate(new ItemStackTemplate(Items.CHAINMAIL_CHESTPLATE), 10)
            .leggings(new ItemStackTemplate(Items.CHAINMAIL_LEGGINGS), 10)
            .boots(new ItemStackTemplate(Items.CHAINMAIL_BOOTS), 10)
            .tag("frontier_ranged"));

        this.addSet("frontier/iron", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(new ItemStackTemplate(Items.IRON_SWORD), 10)
            .mainhand(new ItemStackTemplate(Items.IRON_AXE), 10)
            .mainhand(new ItemStackTemplate(Items.IRON_PICKAXE), 10)
            .mainhand(new ItemStackTemplate(Items.IRON_SHOVEL), 10)
            .offhand(new ItemStackTemplate(Items.SHIELD), 10)
            .helmet(new ItemStackTemplate(Items.IRON_HELMET), 10)
            .chestplate(new ItemStackTemplate(Items.IRON_CHESTPLATE), 10)
            .leggings(new ItemStackTemplate(Items.IRON_LEGGINGS), 10)
            .boots(new ItemStackTemplate(Items.IRON_BOOTS), 10)
            .tag("frontier_melee"));

        this.addSet("frontier/ranged/iron", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(new ItemStackTemplate(Items.BOW), 16)
            .mainhand(new ItemStackTemplate(Items.CROSSBOW), 4)
            .helmet(new ItemStackTemplate(Items.IRON_HELMET), 10)
            .chestplate(new ItemStackTemplate(Items.IRON_CHESTPLATE), 10)
            .leggings(new ItemStackTemplate(Items.IRON_LEGGINGS), 10)
            .boots(new ItemStackTemplate(Items.IRON_BOOTS), 10)
            .tag("frontier_ranged"));

        this.addSet("frontier/diamond", 10, 2.5F, c -> c
            .mainhand(new ItemStackTemplate(Items.DIAMOND_SWORD), 10)
            .mainhand(new ItemStackTemplate(Items.DIAMOND_AXE), 10)
            .mainhand(new ItemStackTemplate(Items.DIAMOND_PICKAXE), 10)
            .mainhand(new ItemStackTemplate(Items.DIAMOND_SHOVEL), 10)
            .offhand(new ItemStackTemplate(Items.SHIELD), 10)
            .helmet(new ItemStackTemplate(Items.DIAMOND_HELMET), 10)
            .chestplate(new ItemStackTemplate(Items.DIAMOND_CHESTPLATE), 10)
            .leggings(new ItemStackTemplate(Items.DIAMOND_LEGGINGS), 10)
            .boots(new ItemStackTemplate(Items.DIAMOND_BOOTS), 10)
            .tag("frontier_melee"));

        // Ascent Sets
        this.addSet("ascent/enchanted_gold", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(buffedItem(Items.GOLDEN_SWORD, enchants, Enchantments.SHARPNESS, 5, 0.5F), 10)
            .mainhand(buffedItem(Items.GOLDEN_AXE, enchants, Enchantments.SHARPNESS, 5, 0.5F), 10)
            .mainhand(buffedItem(Items.GOLDEN_PICKAXE, enchants, Enchantments.FORTUNE, 5, 0.5F), 10)
            .mainhand(buffedItem(Items.GOLDEN_SHOVEL, enchants, Enchantments.FORTUNE, 5, 0.5F), 10)
            .helmet(buffedItem(Items.GOLDEN_HELMET, enchants, Enchantments.PROTECTION, 3, 0.5F), 10)
            .chestplate(buffedItem(Items.GOLDEN_CHESTPLATE, enchants, Enchantments.PROTECTION, 3, 0.5F), 10)
            .leggings(buffedItem(Items.GOLDEN_LEGGINGS, enchants, Enchantments.PROTECTION, 3, 0.5F), 10)
            .boots(buffedItem(Items.GOLDEN_BOOTS, enchants, Enchantments.PROTECTION, 3, 0.5F), 10)
            .tag("ascent_melee"));

        this.addSet("ascent/ranged/enchanted_gold", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(new ItemStackTemplate(Items.BOW), 12)
            .mainhand(new ItemStackTemplate(Items.CROSSBOW), 8)
            .helmet(buffedItem(Items.GOLDEN_HELMET, enchants, Enchantments.PROTECTION, 3, 0.5F), 10)
            .chestplate(buffedItem(Items.GOLDEN_CHESTPLATE, enchants, Enchantments.PROTECTION, 3, 0.5F), 10)
            .leggings(buffedItem(Items.GOLDEN_LEGGINGS, enchants, Enchantments.PROTECTION, 3, 0.5F), 10)
            .boots(buffedItem(Items.GOLDEN_BOOTS, enchants, Enchantments.PROTECTION, 3, 0.5F), 10)
            .tag("ascent_ranged"));

        this.addSet("ascent/iron", 80, 0, c -> c
            .mainhand(new ItemStackTemplate(Items.IRON_SWORD), 10)
            .mainhand(new ItemStackTemplate(Items.IRON_AXE), 10)
            .mainhand(new ItemStackTemplate(Items.IRON_PICKAXE), 10)
            .mainhand(new ItemStackTemplate(Items.IRON_SHOVEL), 10)
            .offhand(new ItemStackTemplate(Items.SHIELD), 10)
            .helmet(new ItemStackTemplate(Items.IRON_HELMET), 10)
            .chestplate(new ItemStackTemplate(Items.IRON_CHESTPLATE), 10)
            .leggings(new ItemStackTemplate(Items.IRON_LEGGINGS), 10)
            .boots(new ItemStackTemplate(Items.IRON_BOOTS), 10)
            .tag("ascent_melee"));

        this.addSet("ascent/ranged/iron", 80, 0, c -> c
            .mainhand(new ItemStackTemplate(Items.BOW), 12)
            .mainhand(new ItemStackTemplate(Items.CROSSBOW), 8)
            .helmet(new ItemStackTemplate(Items.IRON_HELMET), 10)
            .chestplate(new ItemStackTemplate(Items.IRON_CHESTPLATE), 10)
            .leggings(new ItemStackTemplate(Items.IRON_LEGGINGS), 10)
            .boots(new ItemStackTemplate(Items.IRON_BOOTS), 10)
            .tag("ascent_ranged"));

        this.addSet("ascent/diamond", DEFAULT_WEIGHT, 5, c -> c
            .mainhand(new ItemStackTemplate(Items.DIAMOND_SWORD), 10)
            .mainhand(new ItemStackTemplate(Items.DIAMOND_AXE), 10)
            .mainhand(new ItemStackTemplate(Items.DIAMOND_PICKAXE), 10)
            .mainhand(new ItemStackTemplate(Items.DIAMOND_SHOVEL), 10)
            .offhand(new ItemStackTemplate(Items.SHIELD), 10)
            .helmet(new ItemStackTemplate(Items.DIAMOND_HELMET), 10)
            .chestplate(new ItemStackTemplate(Items.DIAMOND_CHESTPLATE), 10)
            .leggings(new ItemStackTemplate(Items.DIAMOND_LEGGINGS), 10)
            .boots(new ItemStackTemplate(Items.DIAMOND_BOOTS), 10)
            .tag("ascent_melee"));

        this.addSet("ascent/ranged/diamond", DEFAULT_WEIGHT, 5, c -> c
            .mainhand(new ItemStackTemplate(Items.BOW), 12)
            .mainhand(new ItemStackTemplate(Items.CROSSBOW), 8)
            .helmet(new ItemStackTemplate(Items.DIAMOND_HELMET), 10)
            .chestplate(new ItemStackTemplate(Items.DIAMOND_CHESTPLATE), 10)
            .leggings(new ItemStackTemplate(Items.DIAMOND_LEGGINGS), 10)
            .boots(new ItemStackTemplate(Items.DIAMOND_BOOTS), 10)
            .tag("ascent_ranged"));

        // Summit Sets
        this.addSet("summit/enchanted_iron", 30, 0, c -> c
            .mainhand(buffedItem(Items.IRON_SWORD, enchants, Enchantments.SHARPNESS, 5, 0.35F), 10)
            .mainhand(buffedItem(Items.IRON_AXE, enchants, Enchantments.SHARPNESS, 5, 0.35F), 10)
            .mainhand(buffedItem(Items.IRON_PICKAXE, enchants, Enchantments.FORTUNE, 5, 0.35F), 10)
            .mainhand(buffedItem(Items.IRON_SHOVEL, enchants, Enchantments.FORTUNE, 5, 0.35F), 10)
            .helmet(buffedItem(Items.IRON_HELMET, enchants, Enchantments.PROTECTION, 3, 0.35F), 10)
            .chestplate(buffedItem(Items.IRON_CHESTPLATE, enchants, Enchantments.PROTECTION, 3, 0.35F), 10)
            .leggings(buffedItem(Items.IRON_LEGGINGS, enchants, Enchantments.PROTECTION, 3, 0.35F), 10)
            .boots(buffedItem(Items.IRON_BOOTS, enchants, Enchantments.PROTECTION, 3, 0.35F), 10)
            .tag("summit_melee"));

        this.addSet("summit/ranged/enchanted_iron", 30, 0, c -> c
            .mainhand(buffedItem(Items.BOW, enchants, Enchantments.POWER, 5, 0.35F), 10)
            .mainhand(buffedItem(Items.CROSSBOW, enchants, Enchantments.POWER, 5, 0.35F), 10)
            .helmet(buffedItem(Items.IRON_HELMET, enchants, Enchantments.PROTECTION, 3, 0.35F), 10)
            .chestplate(buffedItem(Items.IRON_CHESTPLATE, enchants, Enchantments.PROTECTION, 3, 0.35F), 10)
            .leggings(buffedItem(Items.IRON_LEGGINGS, enchants, Enchantments.PROTECTION, 3, 0.35F), 10)
            .boots(buffedItem(Items.IRON_BOOTS, enchants, Enchantments.PROTECTION, 3, 0.35F), 10)
            .tag("summit_ranged"));

        this.addSet("summit/diamond", 40, 0, c -> c
            .mainhand(new ItemStackTemplate(Items.DIAMOND_SWORD), 10)
            .mainhand(new ItemStackTemplate(Items.DIAMOND_AXE), 10)
            .mainhand(new ItemStackTemplate(Items.DIAMOND_PICKAXE), 10)
            .mainhand(new ItemStackTemplate(Items.DIAMOND_SHOVEL), 10)
            .offhand(new ItemStackTemplate(Items.SHIELD), 10)
            .helmet(new ItemStackTemplate(Items.DIAMOND_HELMET), 10)
            .chestplate(new ItemStackTemplate(Items.DIAMOND_CHESTPLATE), 10)
            .leggings(new ItemStackTemplate(Items.DIAMOND_LEGGINGS), 10)
            .boots(new ItemStackTemplate(Items.DIAMOND_BOOTS), 10)
            .tag("summit_melee"));

        this.addSet("summit/ranged/diamond", 40, 0, c -> c
            .mainhand(new ItemStackTemplate(Items.BOW), 10)
            .mainhand(new ItemStackTemplate(Items.CROSSBOW), 10)
            .helmet(new ItemStackTemplate(Items.DIAMOND_HELMET), 10)
            .chestplate(new ItemStackTemplate(Items.DIAMOND_CHESTPLATE), 10)
            .leggings(new ItemStackTemplate(Items.DIAMOND_LEGGINGS), 10)
            .boots(new ItemStackTemplate(Items.DIAMOND_BOOTS), 10)
            .tag("summit_ranged"));

        this.addSet("summit/enchanted_diamond", 60, 0, c -> c
            .mainhand(buffedItem(Items.DIAMOND_SWORD, enchants, Enchantments.SHARPNESS, 8, 0.525F), 10)
            .mainhand(buffedItem(Items.DIAMOND_AXE, enchants, Enchantments.SHARPNESS, 8, 0.525F), 10)
            .mainhand(buffedItem(Items.DIAMOND_PICKAXE, enchants, Enchantments.FORTUNE, 8, 0.525F), 10)
            .mainhand(buffedItem(Items.DIAMOND_SHOVEL, enchants, Enchantments.FORTUNE, 8, 0.525F), 10)
            .helmet(buffedItem(Items.DIAMOND_HELMET, enchants, Enchantments.PROTECTION, 3, 0.35F), 10)
            .chestplate(buffedItem(Items.DIAMOND_CHESTPLATE, enchants, Enchantments.PROTECTION, 3, 0.35F), 10)
            .leggings(buffedItem(Items.DIAMOND_LEGGINGS, enchants, Enchantments.PROTECTION, 3, 0.35F), 10)
            .boots(buffedItem(Items.DIAMOND_BOOTS, enchants, Enchantments.PROTECTION, 3, 0.35F), 10)
            .tag("summit_melee"));

        this.addSet("summit/ranged/enchanted_diamond", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(buffedItem(Items.BOW, enchants, Enchantments.POWER, 6, 0.42F), 10)
            .mainhand(buffedItem(Items.CROSSBOW, enchants, Enchantments.POWER, 6, 0.42F), 10)
            .helmet(buffedItem(Items.DIAMOND_HELMET, enchants, Enchantments.PROTECTION, 2, 0.21F), 10)
            .chestplate(buffedItem(Items.DIAMOND_CHESTPLATE, enchants, Enchantments.PROTECTION, 2, 0.21F), 10)
            .leggings(buffedItem(Items.DIAMOND_LEGGINGS, enchants, Enchantments.PROTECTION, 2, 0.21F), 10)
            .boots(buffedItem(Items.DIAMOND_BOOTS, enchants, Enchantments.PROTECTION, 2, 0.21F), 10)
            .tag("summit_ranged"));

        this.addSet("summit/netherite", 140, 5, c -> c
            .mainhand(new ItemStackTemplate(Items.NETHERITE_SWORD), 10)
            .mainhand(new ItemStackTemplate(Items.NETHERITE_AXE), 10)
            .mainhand(new ItemStackTemplate(Items.NETHERITE_PICKAXE), 10)
            .mainhand(new ItemStackTemplate(Items.NETHERITE_SHOVEL), 10)
            .offhand(new ItemStackTemplate(Items.SHIELD), 10)
            .helmet(new ItemStackTemplate(Items.NETHERITE_HELMET), 10)
            .chestplate(new ItemStackTemplate(Items.NETHERITE_CHESTPLATE), 10)
            .leggings(new ItemStackTemplate(Items.NETHERITE_LEGGINGS), 10)
            .boots(new ItemStackTemplate(Items.NETHERITE_BOOTS), 10)
            .tag("summit_melee"));

        this.addSet("summit/ranged/netherite", 140, 5, c -> c
            .mainhand(new ItemStackTemplate(Items.BOW), 10)
            .mainhand(new ItemStackTemplate(Items.CROSSBOW), 10)
            .helmet(new ItemStackTemplate(Items.NETHERITE_HELMET), 10)
            .chestplate(new ItemStackTemplate(Items.NETHERITE_CHESTPLATE), 10)
            .leggings(new ItemStackTemplate(Items.NETHERITE_LEGGINGS), 10)
            .boots(new ItemStackTemplate(Items.NETHERITE_BOOTS), 10)
            .tag("summit_ranged"));

        // Pinnacle
        // Every pinnacle item is a "chase" item: a single enchantment at CHASE_LEVEL, which is above the Apothic Enchanting max level
        // and therefore cannot be obtained, moved, or reapplied through normal means. Invader#modifyBossItem intentionally clamps
        // the enchantments of the guaranteed affix item, so only the non-affixed pieces retain these levels.
        // See the javadoc on CHASE_LEVEL for the selection rules.
        this.addSet("pinnacle/enchanted_netherite", DEFAULT_WEIGHT, 5, c -> chaseArmor(chaseTools(c, registries)
            .offhand(new ItemStackTemplate(Items.SHIELD), 10), registries)
            .tag("pinnacle_melee"));

        this.addSet("pinnacle/ranged/enchanted_netherite", DEFAULT_WEIGHT, 5, c -> chaseArmor(c
            .mainhand(chaseItem(Items.BOW, registries, Enchantments.POWER), 10)
            .mainhand(chaseItem(Items.CROSSBOW, registries, Enchantments.POWER), 8)
            .mainhand(chaseItem(Items.CROSSBOW, registries, Enchantments.PIERCING), 8)
            .mainhand(chaseItem(Items.CROSSBOW, registries, Enchantments.UNBREAKING), 3), registries)
            .tag("pinnacle_ranged"));

        // Apothic Enchanting variants. These reference AE enchantments, so they live in separate, conditional sets:
        // an unknown enchantment id would fail the item codec and prevent the entire set from loading.
        // The armor pool is the vanilla pool plus AE additions, so wearing this set does not look different; only the weapons do.
        this.addSet("pinnacle/apothic/enchanted_netherite", 40, 5, c -> apothicChaseArmor(chaseArmor(apothicChaseTools(c, registries)
            .offhand(chaseItem(Items.SHIELD, registries, Ench.Enchantments.SHIELD_BASH), 5)
            .offhand(chaseItem(Items.SHIELD, registries, Ench.Enchantments.REFLECTIVE_DEFENSES), 5), registries), registries)
            .tag("pinnacle_melee"),
            new ModLoadedCondition(ApothicEnchantingCompat.MODID));

        this.addSet("pinnacle/ranged/apothic/enchanted_netherite", 40, 5, c -> apothicChaseArmor(chaseArmor(c
            .mainhand(chaseItem(Items.CROSSBOW, registries, Ench.Enchantments.CRESCENDO_OF_BOLTS, REDUCED_CHASE_LEVEL), 10), registries), registries)
            .tag("pinnacle_ranged"),
            new ModLoadedCondition(ApothicEnchantingCompat.MODID));

        // Apothic Spawners variant. Capturing is only carried on melee weapons: the kill hook reads the main hand when the mob dies,
        // which is unreliable for ranged kills.
        this.addSet("pinnacle/spawners/enchanted_netherite", 20, 5, c -> chaseArmor(spawnersChaseTools(c, registries)
            .offhand(new ItemStackTemplate(Items.SHIELD), 10), registries)
            .tag("pinnacle_melee"),
            new ModLoadedCondition(ApothicSpawnersCompat.MODID));

        this.addSet("gateway_only/nether_herald", 0, 0, c -> c
            .helmet(getNetherHeraldBannerInstance(registries.lookupOrThrow(Registries.BANNER_PATTERN)), 1)
            .mainhand(buffedItem(Items.DIAMOND_AXE, enchants, Enchantments.SHARPNESS, 3, 0.175F), 1));

        this.addSet("gateway_only/bastion_guard", 0, 0, c -> c
            .helmet(getBastionGuardBannerInstance(registries.lookupOrThrow(Registries.BANNER_PATTERN)), 1)
            .mainhand(buffedItem(Items.NETHERITE_AXE, enchants, Enchantments.SHARPNESS, 3, 0.175F), 1)
            .offhand(new ItemStackTemplate(Items.SHIELD), 1)
            .chestplate(new ItemStackTemplate(Items.NETHERITE_CHESTPLATE), 1)
            .leggings(new ItemStackTemplate(Items.NETHERITE_LEGGINGS), 1)
            .boots(new ItemStackTemplate(Items.NETHERITE_BOOTS), 1));
    }

    /**
     * The enchantment level applied to pinnacle "chase" items.
     * <p>
     * This must exceed the Apothic Enchanting max level of every enchantment used by {@link #chaseItem}, so that the enchantment
     * cannot be reached via the enchanting table, and cannot be reapplied to another item through the anvil (which clamps to the max level).
     * <p>
     * Enchantments are only eligible for chase items when a level this high is both meaningful and desirable. We exclude:
     * <ul>
     * <li>Single-level enchantments (Mending, Silk Touch, Infinity, Flame, Multishot, Chainsaw, etc), where the level does nothing.</li>
     * <li>Enchantments that are hard-capped in code below this level (Depth Strider, Swift Sneak, Frost Walker, Quick Charge).</li>
     * <li>Enchantments that become unusable or harmful at this level (Knockback and Rebounding launch targets out of reach,
     * Berserker's Fury has an exponential health cost, Knowledge of the Ages becomes an unbounded experience source).</li>
     * <li>Enchantments for item types that invaders do not carry (tridents, maces, fishing rods, hoes, shears).</li>
     * <li>Enchantments that are unwanted at this level (Smite, Blast Protection, etc).</li>
     * </ul>
     */
    public static final int CHASE_LEVEL = 15;

    /**
     * A reduced chase level for enchantments whose value grows too quickly at {@link #CHASE_LEVEL}: Protection (the total protection cap is
     * reached with fewer pieces), Scavenger, Boon of the Earth, and Capturing (uncapped linear loot chances), and Crescendo of Bolts (extra shots per level).
     * Still above the Apothic Enchanting max level of each of these enchantments.
     */
    public static final int REDUCED_CHASE_LEVEL = 10;

    /**
     * Creates a chase item: an item with exactly one enchantment at {@link #CHASE_LEVEL} and the max durability bonus.
     * <p>
     * Chase items must only ever have a single enchantment. The over-cap enchantment is the entire identity of the item.
     */
    protected static ItemStackTemplate chaseItem(Item item, HolderLookup.Provider registries, ResourceKey<Enchantment> ench) {
        return chaseItem(item, registries, ench, CHASE_LEVEL);
    }

    protected static ItemStackTemplate chaseItem(Item item, HolderLookup.Provider registries, ResourceKey<Enchantment> ench, int level) {
        ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        // Standalone holders are required: Apothic Enchanting's enchantments are not present in the datagen registry lookup.
        mut.set(ApothMiscUtil.standaloneHolder(registries, ench), level);
        DataComponentPatch patch = DataComponentPatch.builder()
            .set(Components.DURABILITY_BONUS, 0.8F)
            .set(DataComponents.ENCHANTMENTS, mut.toImmutable())
            .build();
        return new ItemStackTemplate(item, 1, patch);
    }

    /**
     * Adds the vanilla pinnacle chase tool pool (netherite sword, axe, pickaxe, and shovel) to a gear set builder.
     */
    protected static GSBuilder chaseTools(GSBuilder c, HolderLookup.Provider registries) {
        return chaseTools(c, registries, Items.NETHERITE_SWORD, Items.NETHERITE_AXE, Items.NETHERITE_PICKAXE, Items.NETHERITE_SHOVEL);
    }

    /**
     * Adds the vanilla pinnacle chase tool pool to a gear set builder, using the provided items.
     */
    protected static GSBuilder chaseTools(GSBuilder c, HolderLookup.Provider registries, Item sword, Item axe, Item pickaxe, Item shovel) {
        return c
            .mainhand(chaseItem(sword, registries, Enchantments.SHARPNESS), 10)
            .mainhand(chaseItem(sword, registries, Enchantments.LOOTING), 5)
            .mainhand(chaseItem(axe, registries, Enchantments.SHARPNESS), 10)
            .mainhand(chaseItem(axe, registries, Enchantments.EFFICIENCY), 3)
            .mainhand(chaseItem(pickaxe, registries, Enchantments.FORTUNE), 10)
            .mainhand(chaseItem(pickaxe, registries, Enchantments.EFFICIENCY), 5)
            .mainhand(chaseItem(pickaxe, registries, Enchantments.UNBREAKING), 3)
            .mainhand(chaseItem(shovel, registries, Enchantments.FORTUNE), 10)
            .mainhand(chaseItem(shovel, registries, Enchantments.EFFICIENCY), 5)
            .mainhand(chaseItem(shovel, registries, Enchantments.UNBREAKING), 3);
    }

    /**
     * Adds the Apothic Enchanting pinnacle chase tool pool (netherite) to a gear set builder. Only valid in sets conditional on Apothic Enchanting.
     */
    protected static GSBuilder apothicChaseTools(GSBuilder c, HolderLookup.Provider registries) {
        return apothicChaseTools(c, registries, Items.NETHERITE_SWORD, Items.NETHERITE_AXE, Items.NETHERITE_PICKAXE);
    }

    /**
     * Adds the Apothic Enchanting pinnacle chase tool pool to a gear set builder, using the provided items.
     * Only valid in sets conditional on Apothic Enchanting.
     */
    protected static GSBuilder apothicChaseTools(GSBuilder c, HolderLookup.Provider registries, Item sword, Item axe, Item pickaxe) {
        return c
            .mainhand(chaseItem(sword, registries, Ench.Enchantments.SCAVENGER, REDUCED_CHASE_LEVEL), 6)
            .mainhand(chaseItem(axe, registries, Ench.Enchantments.SCAVENGER, REDUCED_CHASE_LEVEL), 4)
            .mainhand(chaseItem(pickaxe, registries, Ench.Enchantments.BOON_OF_THE_EARTH, REDUCED_CHASE_LEVEL), 6);
    }

    /**
     * Adds the Apothic Spawners pinnacle chase tool pool (netherite) to a gear set builder. Only valid in sets conditional on Apothic Spawners.
     */
    protected static GSBuilder spawnersChaseTools(GSBuilder c, HolderLookup.Provider registries) {
        return spawnersChaseTools(c, registries, Items.NETHERITE_SWORD, Items.NETHERITE_AXE);
    }

    /**
     * Adds the Apothic Spawners pinnacle chase tool pool to a gear set builder, using the provided items.
     * Only valid in sets conditional on Apothic Spawners.
     */
    protected static GSBuilder spawnersChaseTools(GSBuilder c, HolderLookup.Provider registries, Item sword, Item axe) {
        return c
            .mainhand(chaseItem(sword, registries, ApothicSpawnersCompat.CAPTURING, REDUCED_CHASE_LEVEL), 6)
            .mainhand(chaseItem(axe, registries, ApothicSpawnersCompat.CAPTURING, REDUCED_CHASE_LEVEL), 4);
    }

    /**
     * Adds the vanilla pinnacle chase armor pool (netherite) to a gear set builder.
     */
    protected static GSBuilder chaseArmor(GSBuilder c, HolderLookup.Provider registries) {
        return chaseArmor(c, registries, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);
    }

    /**
     * Adds the vanilla pinnacle chase armor pool to a gear set builder, using the provided items.
     */
    protected static GSBuilder chaseArmor(GSBuilder c, HolderLookup.Provider registries, Item helmet, Item chestplate, Item leggings, Item boots) {
        return c
            .helmet(chaseItem(helmet, registries, Enchantments.PROTECTION, REDUCED_CHASE_LEVEL), 10)
            .helmet(chaseItem(helmet, registries, Enchantments.RESPIRATION), 3)
            .helmet(chaseItem(helmet, registries, Enchantments.THORNS), 2)
            .chestplate(chaseItem(chestplate, registries, Enchantments.PROTECTION, REDUCED_CHASE_LEVEL), 10)
            .chestplate(chaseItem(chestplate, registries, Enchantments.THORNS), 3)
            .leggings(chaseItem(leggings, registries, Enchantments.PROTECTION, REDUCED_CHASE_LEVEL), 10)
            .leggings(chaseItem(leggings, registries, Enchantments.THORNS), 3)
            .boots(chaseItem(boots, registries, Enchantments.PROTECTION, REDUCED_CHASE_LEVEL), 10)
            .boots(chaseItem(boots, registries, Enchantments.FEATHER_FALLING), 4)
            .boots(chaseItem(boots, registries, Enchantments.THORNS), 2);
    }

    /**
     * Adds the Apothic Enchanting additions to the pinnacle chase armor pool (netherite). Only valid in sets conditional on Apothic Enchanting.
     */
    protected static GSBuilder apothicChaseArmor(GSBuilder c, HolderLookup.Provider registries) {
        return apothicChaseArmor(c, registries, Items.NETHERITE_CHESTPLATE);
    }

    /**
     * Adds the Apothic Enchanting additions to the pinnacle chase armor pool, using the provided items.
     * Only valid in sets conditional on Apothic Enchanting.
     */
    protected static GSBuilder apothicChaseArmor(GSBuilder c, HolderLookup.Provider registries, Item chestplate) {
        return c.chestplate(chaseItem(chestplate, registries, Ench.Enchantments.ICY_THORNS), 4);
    }

    protected static ItemStackTemplate buffedItem(Item item, RegistryLookup<Enchantment> enchants, ResourceKey<Enchantment> enchant, int level, float durabilityBonus) {
        ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mut.set(enchants.getOrThrow(enchant), level);
        DataComponentPatch patch = DataComponentPatch.builder()
            .set(Components.DURABILITY_BONUS, durabilityBonus)
            .set(DataComponents.ENCHANTMENTS, mut.toImmutable())
            .build();
        return new ItemStackTemplate(item, 1, patch);
    }

    public static ItemStackTemplate getNetherHeraldBannerInstance(HolderGetter<BannerPattern> patternRegistry) {
        BannerPatternLayers bannerpatternlayers = new BannerPatternLayers.Builder()
            .add(patternRegistry.getOrThrow(BannerPatterns.SKULL), DyeColor.YELLOW)
            .add(patternRegistry.getOrThrow(BannerPatterns.BORDER), DyeColor.RED)
            .add(patternRegistry.getOrThrow(BannerPatterns.GRADIENT_UP), DyeColor.BLACK)
            .build();
        DataComponentPatch patch = DataComponentPatch.builder()
            .set(DataComponents.BANNER_PATTERNS, bannerpatternlayers)
            .set(DataComponents.TOOLTIP_DISPLAY, net.minecraft.world.item.component.TooltipDisplay.DEFAULT.withHidden(DataComponents.BANNER_PATTERNS, true))
            .set(DataComponents.ITEM_NAME, Apotheosis.lang("banner", "nether_herald").withStyle(ChatFormatting.RED))
            .build();
        return new ItemStackTemplate(Items.BLACK_BANNER, 1, patch);
    }

    public static ItemStackTemplate getBastionGuardBannerInstance(HolderGetter<BannerPattern> patternRegistry) {
        BannerPatternLayers bannerpatternlayers = new BannerPatternLayers.Builder()
            .add(patternRegistry.getOrThrow(BannerPatterns.CIRCLE_MIDDLE), DyeColor.BLACK)
            .add(patternRegistry.getOrThrow(BannerPatterns.CURLY_BORDER), DyeColor.YELLOW)
            .build();
        DataComponentPatch patch = DataComponentPatch.builder()
            .set(DataComponents.BANNER_PATTERNS, bannerpatternlayers)
            .set(DataComponents.TOOLTIP_DISPLAY, net.minecraft.world.item.component.TooltipDisplay.DEFAULT.withHidden(DataComponents.BANNER_PATTERNS, true))
            .set(DataComponents.ITEM_NAME, Apotheosis.lang("banner", "bastion_guard").withStyle(ChatFormatting.RED))
            .build();
        return new ItemStackTemplate(Items.BROWN_BANNER, 1, patch);
    }

    protected void addSet(String name, int weight, float quality, UnaryOperator<GSBuilder> config) {
        Identifier id = Apotheosis.loc(name);
        GSBuilder builder = config.apply(new GSBuilder(weight, quality));
        this.add(id, builder.build());
        for (String tag : builder.tags()) {
            TAG_ASSOCIATIONS.computeIfAbsent(tag, k -> new ArrayList<>()).add(id);
        }
    }

    /**
     * Adds a gear set that only loads when the given conditions are met. Its tags are recorded in {@link #OPTIONAL_TAG_ASSOCIATIONS}.
     */
    protected void addSet(String name, int weight, float quality, UnaryOperator<GSBuilder> config, ICondition... conditions) {
        Identifier id = Apotheosis.loc(name);
        GSBuilder builder = config.apply(new GSBuilder(weight, quality));
        this.addConditionally(id, builder.build(), conditions);
        for (String tag : builder.tags()) {
            OPTIONAL_TAG_ASSOCIATIONS.computeIfAbsent(tag, k -> new ArrayList<>()).add(id);
        }
    }

    public static class GSBuilder {
        private final int weight;
        private final float quality;
        private final List<WeightedItemStack> helmets = new ArrayList<>();
        private final List<WeightedItemStack> chestplates = new ArrayList<>();
        private final List<WeightedItemStack> leggings = new ArrayList<>();
        private final List<WeightedItemStack> boots = new ArrayList<>();
        private final List<WeightedItemStack> mainhands = new ArrayList<>();
        private final List<WeightedItemStack> offhands = new ArrayList<>();
        private final Set<String> tags = new LinkedHashSet<>();

        public GSBuilder(int weight, float quality) {
            this.weight = weight;
            this.quality = quality;
        }

        public GSBuilder helmet(ItemStackTemplate template, int weight, float dropChance) {
            this.helmets.add(new WeightedItemStack(Optional.of(template), weight, dropChance));
            return this;
        }

        public GSBuilder helmet(ItemStackTemplate template, int weight) {
            return this.helmet(template, weight, -1);
        }

        public GSBuilder chestplate(ItemStackTemplate template, int weight, float dropChance) {
            this.chestplates.add(new WeightedItemStack(Optional.of(template), weight, dropChance));
            return this;
        }

        public GSBuilder chestplate(ItemStackTemplate template, int weight) {
            return this.chestplate(template, weight, -1);
        }

        public GSBuilder leggings(ItemStackTemplate template, int weight, float dropChance) {
            this.leggings.add(new WeightedItemStack(Optional.of(template), weight, dropChance));
            return this;
        }

        public GSBuilder leggings(ItemStackTemplate template, int weight) {
            return this.leggings(template, weight, -1);
        }

        public GSBuilder boots(ItemStackTemplate template, int weight, float dropChance) {
            this.boots.add(new WeightedItemStack(Optional.of(template), weight, dropChance));
            return this;
        }

        public GSBuilder boots(ItemStackTemplate template, int weight) {
            return this.boots(template, weight, -1);
        }

        public GSBuilder mainhand(ItemStackTemplate template, int weight, float dropChance) {
            this.mainhands.add(new WeightedItemStack(Optional.of(template), weight, dropChance));
            return this;
        }

        public GSBuilder mainhand(ItemStackTemplate template, int weight) {
            return this.mainhand(template, weight, -1);
        }

        public GSBuilder offhand(ItemStackTemplate template, int weight, float dropChance) {
            this.offhands.add(new WeightedItemStack(Optional.of(template), weight, dropChance));
            return this;
        }

        public GSBuilder offhand(ItemStackTemplate template, int weight) {
            return this.offhand(template, weight, -1);
        }

        /**
         * Records that this gear set belongs to the given tag id. Tag membership is no longer baked into the
         * {@link GearSet} record itself; tag JSON files must be generated separately via a
         * {@link dev.shadowsoffire.placebo.util.data.DynamicTagProvider DynamicTagProvider}. This method is retained
         * so that callers can declare intent inline; the resulting tags are exposed via {@link GSBuilder#tags()}.
         */
        public GSBuilder tag(String tag) {
            this.tags.add(tag);
            return this;
        }

        /**
         * @return The unmodifiable set of tag ids declared via {@link #tag(String)}. Use this from a sibling tag
         *         provider to emit the corresponding {@code data/<ns>/tags/apotheosis/gear_sets/<tag>.json} files.
         */
        public Set<String> tags() {
            return java.util.Collections.unmodifiableSet(this.tags);
        }

        public GearSet build() {
            return new GearSet(this.weight, this.quality, this.mainhands, this.offhands, this.boots, this.leggings, this.chestplates, this.helmets);
        }
    }
}
