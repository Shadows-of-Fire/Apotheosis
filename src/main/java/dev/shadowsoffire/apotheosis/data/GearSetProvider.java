package dev.shadowsoffire.apotheosis.data;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.compat.enchanting.ApothicEnchantingCompat;
import dev.shadowsoffire.apotheosis.compat.spawners.ApothicSpawnersCompat;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.util.random.Weight;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

public class GearSetProvider extends DynamicRegistryProvider<GearSet> {

    public static final int DEFAULT_WEIGHT = 100;

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
        addSet("haven/leather", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(new ItemStack(Items.STONE_SWORD), 10)
            .mainhand(new ItemStack(Items.STONE_AXE), 10)
            .mainhand(new ItemStack(Items.STONE_PICKAXE), 10)
            .mainhand(new ItemStack(Items.STONE_SHOVEL), 10)
            .helmet(new ItemStack(Items.LEATHER_HELMET), 10)
            .chestplate(new ItemStack(Items.LEATHER_CHESTPLATE), 10)
            .leggings(new ItemStack(Items.LEATHER_LEGGINGS), 10)
            .boots(new ItemStack(Items.LEATHER_BOOTS), 10)
            .tag("haven_melee"));

        addSet("haven/ranged/leather", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(new ItemStack(Items.BOW), 16)
            .mainhand(new ItemStack(Items.CROSSBOW), 4)
            .helmet(new ItemStack(Items.LEATHER_HELMET), 10)
            .chestplate(new ItemStack(Items.LEATHER_CHESTPLATE), 10)
            .leggings(new ItemStack(Items.LEATHER_LEGGINGS), 10)
            .boots(new ItemStack(Items.LEATHER_BOOTS), 10)
            .tag("haven_ranged"));

        addSet("haven/chain", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(new ItemStack(Items.STONE_SWORD), 10)
            .mainhand(new ItemStack(Items.STONE_AXE), 10)
            .mainhand(new ItemStack(Items.STONE_PICKAXE), 10)
            .mainhand(new ItemStack(Items.STONE_SHOVEL), 10)
            .helmet(new ItemStack(Items.CHAINMAIL_HELMET), 10)
            .chestplate(new ItemStack(Items.CHAINMAIL_CHESTPLATE), 10)
            .leggings(new ItemStack(Items.CHAINMAIL_LEGGINGS), 10)
            .boots(new ItemStack(Items.CHAINMAIL_BOOTS), 10)
            .tag("haven_melee"));

        addSet("haven/ranged/chain", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(new ItemStack(Items.BOW), 16)
            .mainhand(new ItemStack(Items.CROSSBOW), 4)
            .helmet(new ItemStack(Items.CHAINMAIL_HELMET), 10)
            .chestplate(new ItemStack(Items.CHAINMAIL_CHESTPLATE), 10)
            .leggings(new ItemStack(Items.CHAINMAIL_LEGGINGS), 10)
            .boots(new ItemStack(Items.CHAINMAIL_BOOTS), 10)
            .tag("haven_ranged"));

        // Frontier Sets
        addSet("frontier/chain", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(new ItemStack(Items.IRON_SWORD), 10)
            .mainhand(new ItemStack(Items.IRON_AXE), 10)
            .mainhand(new ItemStack(Items.IRON_PICKAXE), 10)
            .mainhand(new ItemStack(Items.IRON_SHOVEL), 10)
            .helmet(new ItemStack(Items.CHAINMAIL_HELMET), 10)
            .chestplate(new ItemStack(Items.CHAINMAIL_CHESTPLATE), 10)
            .leggings(new ItemStack(Items.CHAINMAIL_LEGGINGS), 10)
            .boots(new ItemStack(Items.CHAINMAIL_BOOTS), 10)
            .tag("frontier_melee"));

        addSet("frontier/ranged/chain", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(new ItemStack(Items.BOW), 16)
            .mainhand(new ItemStack(Items.CROSSBOW), 4)
            .helmet(new ItemStack(Items.CHAINMAIL_HELMET), 10)
            .chestplate(new ItemStack(Items.CHAINMAIL_CHESTPLATE), 10)
            .leggings(new ItemStack(Items.CHAINMAIL_LEGGINGS), 10)
            .boots(new ItemStack(Items.CHAINMAIL_BOOTS), 10)
            .tag("frontier_ranged"));

        addSet("frontier/iron", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(new ItemStack(Items.IRON_SWORD), 10)
            .mainhand(new ItemStack(Items.IRON_AXE), 10)
            .mainhand(new ItemStack(Items.IRON_PICKAXE), 10)
            .mainhand(new ItemStack(Items.IRON_SHOVEL), 10)
            .offhand(new ItemStack(Items.SHIELD), 10)
            .helmet(new ItemStack(Items.IRON_HELMET), 10)
            .chestplate(new ItemStack(Items.IRON_CHESTPLATE), 10)
            .leggings(new ItemStack(Items.IRON_LEGGINGS), 10)
            .boots(new ItemStack(Items.IRON_BOOTS), 10)
            .tag("frontier_melee"));

        addSet("frontier/ranged/iron", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(new ItemStack(Items.BOW), 16)
            .mainhand(new ItemStack(Items.CROSSBOW), 4)
            .helmet(new ItemStack(Items.IRON_HELMET), 10)
            .chestplate(new ItemStack(Items.IRON_CHESTPLATE), 10)
            .leggings(new ItemStack(Items.IRON_LEGGINGS), 10)
            .boots(new ItemStack(Items.IRON_BOOTS), 10)
            .tag("frontier_ranged"));

        addSet("frontier/diamond", 10, 2.5F, c -> c
            .mainhand(new ItemStack(Items.DIAMOND_SWORD), 10)
            .mainhand(new ItemStack(Items.DIAMOND_AXE), 10)
            .mainhand(new ItemStack(Items.DIAMOND_PICKAXE), 10)
            .mainhand(new ItemStack(Items.DIAMOND_SHOVEL), 10)
            .offhand(new ItemStack(Items.SHIELD), 10)
            .helmet(new ItemStack(Items.DIAMOND_HELMET), 10)
            .chestplate(new ItemStack(Items.DIAMOND_CHESTPLATE), 10)
            .leggings(new ItemStack(Items.DIAMOND_LEGGINGS), 10)
            .boots(new ItemStack(Items.DIAMOND_BOOTS), 10)
            .tag("frontier_melee"));

        // Ascent Sets
        addSet("ascent/enchanted_gold", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(buffedGoldItem(Items.GOLDEN_SWORD, enchants), 10)
            .mainhand(buffedGoldItem(Items.GOLDEN_AXE, enchants), 10)
            .mainhand(buffedGoldItem(Items.GOLDEN_PICKAXE, enchants), 10)
            .mainhand(buffedGoldItem(Items.GOLDEN_SHOVEL, enchants), 10)
            .helmet(buffedGoldItem(Items.GOLDEN_HELMET, enchants), 10)
            .chestplate(buffedGoldItem(Items.GOLDEN_CHESTPLATE, enchants), 10)
            .leggings(buffedGoldItem(Items.GOLDEN_LEGGINGS, enchants), 10)
            .boots(buffedGoldItem(Items.GOLDEN_BOOTS, enchants), 10)
            .tag("ascent_melee"));

        addSet("ascent/ranged/enchanted_gold", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(new ItemStack(Items.BOW), 12)
            .mainhand(new ItemStack(Items.CROSSBOW), 8)
            .helmet(buffedGoldItem(Items.GOLDEN_HELMET, enchants), 10)
            .chestplate(buffedGoldItem(Items.GOLDEN_CHESTPLATE, enchants), 10)
            .leggings(buffedGoldItem(Items.GOLDEN_LEGGINGS, enchants), 10)
            .boots(buffedGoldItem(Items.GOLDEN_BOOTS, enchants), 10)
            .tag("ascent_ranged"));

        addSet("ascent/iron", 80, 0, c -> c
            .mainhand(new ItemStack(Items.IRON_SWORD), 10)
            .mainhand(new ItemStack(Items.IRON_AXE), 10)
            .mainhand(new ItemStack(Items.IRON_PICKAXE), 10)
            .mainhand(new ItemStack(Items.IRON_SHOVEL), 10)
            .offhand(new ItemStack(Items.SHIELD), 10)
            .helmet(new ItemStack(Items.IRON_HELMET), 10)
            .chestplate(new ItemStack(Items.IRON_CHESTPLATE), 10)
            .leggings(new ItemStack(Items.IRON_LEGGINGS), 10)
            .boots(new ItemStack(Items.IRON_BOOTS), 10)
            .tag("ascent_melee"));

        addSet("ascent/ranged/iron", 80, 0, c -> c
            .mainhand(new ItemStack(Items.BOW), 12)
            .mainhand(new ItemStack(Items.CROSSBOW), 8)
            .helmet(new ItemStack(Items.IRON_HELMET), 10)
            .chestplate(new ItemStack(Items.IRON_CHESTPLATE), 10)
            .leggings(new ItemStack(Items.IRON_LEGGINGS), 10)
            .boots(new ItemStack(Items.IRON_BOOTS), 10)
            .tag("ascent_ranged"));

        addSet("ascent/diamond", DEFAULT_WEIGHT, 5, c -> c
            .mainhand(new ItemStack(Items.DIAMOND_SWORD), 10)
            .mainhand(new ItemStack(Items.DIAMOND_AXE), 10)
            .mainhand(new ItemStack(Items.DIAMOND_PICKAXE), 10)
            .mainhand(new ItemStack(Items.DIAMOND_SHOVEL), 10)
            .offhand(new ItemStack(Items.SHIELD), 10)
            .helmet(new ItemStack(Items.DIAMOND_HELMET), 10)
            .chestplate(new ItemStack(Items.DIAMOND_CHESTPLATE), 10)
            .leggings(new ItemStack(Items.DIAMOND_LEGGINGS), 10)
            .boots(new ItemStack(Items.DIAMOND_BOOTS), 10)
            .tag("ascent_melee"));

        addSet("ascent/ranged/diamond", DEFAULT_WEIGHT, 5, c -> c
            .mainhand(new ItemStack(Items.BOW), 12)
            .mainhand(new ItemStack(Items.CROSSBOW), 8)
            .helmet(new ItemStack(Items.DIAMOND_HELMET), 10)
            .chestplate(new ItemStack(Items.DIAMOND_CHESTPLATE), 10)
            .leggings(new ItemStack(Items.DIAMOND_LEGGINGS), 10)
            .boots(new ItemStack(Items.DIAMOND_BOOTS), 10)
            .tag("ascent_ranged"));

        // Summit Sets
        addSet("summit/enchanted_iron", 30, 0, c -> c
            .mainhand(buffedItem(Items.IRON_SWORD, enchants, 1F), 10)
            .mainhand(buffedItem(Items.IRON_AXE, enchants, 1F), 10)
            .mainhand(buffedItem(Items.IRON_PICKAXE, enchants, 1F), 10)
            .mainhand(buffedItem(Items.IRON_SHOVEL, enchants, 1F), 10)
            .helmet(buffedItem(Items.IRON_HELMET, enchants, 1F), 10)
            .chestplate(buffedItem(Items.IRON_CHESTPLATE, enchants, 1F), 10)
            .leggings(buffedItem(Items.IRON_LEGGINGS, enchants, 1F), 10)
            .boots(buffedItem(Items.IRON_BOOTS, enchants, 1F), 10)
            .tag("summit_melee"));

        addSet("summit/ranged/enchanted_iron", 30, 0, c -> c
            .mainhand(buffedItem(Items.BOW, enchants, 1F), 10)
            .mainhand(buffedItem(Items.CROSSBOW, enchants, 1F), 10)
            .helmet(buffedItem(Items.IRON_HELMET, enchants, 1F), 10)
            .chestplate(buffedItem(Items.IRON_CHESTPLATE, enchants, 1F), 10)
            .leggings(buffedItem(Items.IRON_LEGGINGS, enchants, 1F), 10)
            .boots(buffedItem(Items.IRON_BOOTS, enchants, 1F), 10)
            .tag("summit_ranged"));

        addSet("summit/diamond", 40, 0, c -> c
            .mainhand(new ItemStack(Items.DIAMOND_SWORD), 10)
            .mainhand(new ItemStack(Items.DIAMOND_AXE), 10)
            .mainhand(new ItemStack(Items.DIAMOND_PICKAXE), 10)
            .mainhand(new ItemStack(Items.DIAMOND_SHOVEL), 10)
            .offhand(new ItemStack(Items.SHIELD), 10)
            .helmet(new ItemStack(Items.DIAMOND_HELMET), 10)
            .chestplate(new ItemStack(Items.DIAMOND_CHESTPLATE), 10)
            .leggings(new ItemStack(Items.DIAMOND_LEGGINGS), 10)
            .boots(new ItemStack(Items.DIAMOND_BOOTS), 10)
            .tag("summit_melee"));

        addSet("summit/ranged/diamond", 40, 0, c -> c
            .mainhand(new ItemStack(Items.BOW), 10)
            .mainhand(new ItemStack(Items.CROSSBOW), 10)
            .helmet(new ItemStack(Items.DIAMOND_HELMET), 10)
            .chestplate(new ItemStack(Items.DIAMOND_CHESTPLATE), 10)
            .leggings(new ItemStack(Items.DIAMOND_LEGGINGS), 10)
            .boots(new ItemStack(Items.DIAMOND_BOOTS), 10)
            .tag("summit_ranged"));

        addSet("summit/enchanted_diamond", 60, 0, c -> c
            .mainhand(buffedItem(Items.DIAMOND_SWORD, enchants, 1.5F), 10)
            .mainhand(buffedItem(Items.DIAMOND_AXE, enchants, 1.5F), 10)
            .mainhand(buffedItem(Items.DIAMOND_PICKAXE, enchants, 1.5F), 10)
            .mainhand(buffedItem(Items.DIAMOND_SHOVEL, enchants, 1.5F), 10)
            .helmet(buffedItem(Items.DIAMOND_HELMET, enchants, 1F), 10)
            .chestplate(buffedItem(Items.DIAMOND_CHESTPLATE, enchants, 1F), 10)
            .leggings(buffedItem(Items.DIAMOND_LEGGINGS, enchants, 1F), 10)
            .boots(buffedItem(Items.DIAMOND_BOOTS, enchants, 1F), 10)
            .tag("summit_melee"));

        addSet("summit/ranged/enchanted_diamond", DEFAULT_WEIGHT, 0, c -> c
            .mainhand(buffedItem(Items.BOW, enchants, 1.2F), 10)
            .mainhand(buffedItem(Items.CROSSBOW, enchants, 1.2F), 10)
            .helmet(buffedItem(Items.DIAMOND_HELMET, enchants, 0.6F), 10)
            .chestplate(buffedItem(Items.DIAMOND_CHESTPLATE, enchants, 0.6F), 10)
            .leggings(buffedItem(Items.DIAMOND_LEGGINGS, enchants, 0.6F), 10)
            .boots(buffedItem(Items.DIAMOND_BOOTS, enchants, 0.6F), 10)
            .tag("summit_ranged"));

        addSet("summit/netherite", 140, 5, c -> c
            .mainhand(new ItemStack(Items.NETHERITE_SWORD), 10)
            .mainhand(new ItemStack(Items.NETHERITE_AXE), 10)
            .mainhand(new ItemStack(Items.NETHERITE_PICKAXE), 10)
            .mainhand(new ItemStack(Items.NETHERITE_SHOVEL), 10)
            .offhand(new ItemStack(Items.SHIELD), 10)
            .helmet(new ItemStack(Items.NETHERITE_HELMET), 10)
            .chestplate(new ItemStack(Items.NETHERITE_CHESTPLATE), 10)
            .leggings(new ItemStack(Items.NETHERITE_LEGGINGS), 10)
            .boots(new ItemStack(Items.NETHERITE_BOOTS), 10)
            .tag("summit_melee"));

        addSet("summit/ranged/netherite", 140, 5, c -> c
            .mainhand(new ItemStack(Items.BOW), 10)
            .mainhand(new ItemStack(Items.CROSSBOW), 10)
            .helmet(new ItemStack(Items.NETHERITE_HELMET), 10)
            .chestplate(new ItemStack(Items.NETHERITE_CHESTPLATE), 10)
            .leggings(new ItemStack(Items.NETHERITE_LEGGINGS), 10)
            .boots(new ItemStack(Items.NETHERITE_BOOTS), 10)
            .tag("summit_ranged"));

        // Pinnacle
        // Every pinnacle item is a "chase" item: a single enchantment at CHASE_LEVEL, which is above the Apothic Enchanting max level
        // and therefore cannot be obtained, moved, or reapplied through normal means. Invader#modifyBossItem intentionally clamps
        // the enchantments of the guaranteed affix item, so only the non-affixed pieces retain these levels.
        // See the javadoc on CHASE_LEVEL for the selection rules.
        addSet("pinnacle/enchanted_netherite", DEFAULT_WEIGHT, 5, c -> chaseArmor(chaseTools(c, registries)
            .offhand(new ItemStack(Items.SHIELD), 10), registries)
            .tag("pinnacle_melee"));

        addSet("pinnacle/ranged/enchanted_netherite", DEFAULT_WEIGHT, 5, c -> chaseArmor(c
            .mainhand(chaseItem(Items.BOW, registries, Enchantments.POWER), 10)
            .mainhand(chaseItem(Items.CROSSBOW, registries, Enchantments.POWER), 8)
            .mainhand(chaseItem(Items.CROSSBOW, registries, Enchantments.PIERCING), 8)
            .mainhand(chaseItem(Items.CROSSBOW, registries, Enchantments.UNBREAKING), 3), registries)
            .tag("pinnacle_ranged"));

        // Apothic Enchanting variants. These reference AE enchantments, so they live in separate, conditional sets:
        // an unknown enchantment id would fail the ItemStack codec and prevent the entire set from loading.
        // The armor pool is the vanilla pool plus AE additions, so wearing this set does not look different; only the weapons do.
        addSet("pinnacle/apothic/enchanted_netherite", 40, 5, c -> apothicChaseArmor(chaseArmor(apothicChaseTools(c, registries)
            .offhand(chaseItem(Items.SHIELD, registries, Ench.Enchantments.SHIELD_BASH), 5)
            .offhand(chaseItem(Items.SHIELD, registries, Ench.Enchantments.REFLECTIVE_DEFENSES), 5), registries), registries)
            .tag("pinnacle_melee"),
            new ModLoadedCondition(ApothicEnchantingCompat.MODID));

        addSet("pinnacle/ranged/apothic/enchanted_netherite", 40, 5, c -> apothicChaseArmor(chaseArmor(c
            .mainhand(chaseItem(Items.CROSSBOW, registries, Ench.Enchantments.CRESCENDO_OF_BOLTS, REDUCED_CHASE_LEVEL), 10), registries), registries)
            .tag("pinnacle_ranged"),
            new ModLoadedCondition(ApothicEnchantingCompat.MODID));

        // Apothic Spawners variant. Capturing is only carried on melee weapons: the kill hook reads the main hand when the mob dies,
        // which is unreliable for ranged kills.
        addSet("pinnacle/spawners/enchanted_netherite", 20, 5, c -> chaseArmor(spawnersChaseTools(c, registries)
            .offhand(new ItemStack(Items.SHIELD), 10), registries)
            .tag("pinnacle_melee"),
            new ModLoadedCondition(ApothicSpawnersCompat.MODID));

        addSet("gateway_only/nether_herald", 0, 0, c -> c
            .helmet(getNetherHeraldBannerInstance(registries.lookupOrThrow(Registries.BANNER_PATTERN)), 1)
            .mainhand(buffedItem(Items.DIAMOND_AXE, enchants, 0.5F), 1));

        addSet("gateway_only/bastion_guard", 0, 0, c -> c
            .helmet(getBastionGuardBannerInstance(registries.lookupOrThrow(Registries.BANNER_PATTERN)), 1)
            .mainhand(buffedItem(Items.NETHERITE_AXE, enchants, 0.5F), 1)
            .offhand(new ItemStack(Items.SHIELD), 1)
            .chestplate(new ItemStack(Items.NETHERITE_CHESTPLATE), 1)
            .leggings(new ItemStack(Items.NETHERITE_LEGGINGS), 1)
            .boots(new ItemStack(Items.NETHERITE_BOOTS), 1));
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
    protected static ItemStack chaseItem(Item item, HolderLookup.Provider registries, ResourceKey<Enchantment> ench) {
        return chaseItem(item, registries, ench, CHASE_LEVEL);
    }

    protected static ItemStack chaseItem(Item item, HolderLookup.Provider registries, ResourceKey<Enchantment> ench, int level) {
        ItemStack stack = new ItemStack(item);
        stack.set(Components.DURABILITY_BONUS, 0.8F);
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        // Standalone holders are required: Apothic Enchanting's enchantments are not present in the datagen registry lookup.
        builder.set(ApothMiscUtil.standaloneHolder(registries, ench), level);
        EnchantmentHelper.setEnchantments(stack, builder.toImmutable());
        return stack;
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

    @SuppressWarnings("removal")
    protected static ItemStack buffedItem(Item item, RegistryLookup<Enchantment> enchants, float magnitude) {
        ItemStack stack = new ItemStack(item);
        LootCategory cat = LootCategory.forItem(stack);
        stack.set(Components.DURABILITY_BONUS, Mth.clamp(0.35F * magnitude, 0, 0.8F));
        if (cat.isArmor()) {
            stack.enchant(enchants.getOrThrow(Enchantments.PROTECTION), Mth.ceil(magnitude * 3));
        }
        else if (cat.isMelee()) {
            stack.enchant(enchants.getOrThrow(Enchantments.SHARPNESS), Mth.ceil(magnitude * 5));
        }
        else if (cat.isBreaker()) {
            stack.enchant(enchants.getOrThrow(Enchantments.FORTUNE), Mth.ceil(magnitude * 5));
        }
        else if (cat.isRanged()) {
            stack.enchant(enchants.getOrThrow(Enchantments.POWER), Mth.ceil(magnitude * 5));
        }
        return stack;
    }

    @SuppressWarnings("removal")
    protected static ItemStack buffedGoldItem(Item item, RegistryLookup<Enchantment> enchants) {
        ItemStack stack = new ItemStack(item);
        LootCategory cat = LootCategory.forItem(stack);
        stack.set(Components.DURABILITY_BONUS, 0.50F);
        if (cat.isArmor()) {
            stack.enchant(enchants.getOrThrow(Enchantments.PROTECTION), 3);
        }
        else if (cat.isMelee()) {
            stack.enchant(enchants.getOrThrow(Enchantments.SHARPNESS), 5);
        }
        else if (cat.isBreaker()) {
            stack.enchant(enchants.getOrThrow(Enchantments.FORTUNE), 5);
        }
        return stack;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack getNetherHeraldBannerInstance(HolderGetter<BannerPattern> patternRegistry) {
        ItemStack itemstack = new ItemStack(Items.BLACK_BANNER);
        BannerPatternLayers bannerpatternlayers = new BannerPatternLayers.Builder()
            .addIfRegistered(patternRegistry, BannerPatterns.SKULL, DyeColor.YELLOW)
            .addIfRegistered(patternRegistry, BannerPatterns.BORDER, DyeColor.RED)
            .addIfRegistered(patternRegistry, BannerPatterns.GRADIENT_UP, DyeColor.BLACK)
            .build();
        itemstack.set(DataComponents.BANNER_PATTERNS, bannerpatternlayers);
        itemstack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        itemstack.set(DataComponents.ITEM_NAME, Apotheosis.lang("banner", "nether_herald").withStyle(ChatFormatting.RED));
        return itemstack;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack getBastionGuardBannerInstance(HolderGetter<BannerPattern> patternRegistry) {
        ItemStack itemstack = new ItemStack(Items.BROWN_BANNER);
        BannerPatternLayers bannerpatternlayers = new BannerPatternLayers.Builder()
            .addIfRegistered(patternRegistry, BannerPatterns.CIRCLE_MIDDLE, DyeColor.BLACK)
            .addIfRegistered(patternRegistry, BannerPatterns.CURLY_BORDER, DyeColor.YELLOW)
            .build();
        itemstack.set(DataComponents.BANNER_PATTERNS, bannerpatternlayers);
        itemstack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        itemstack.set(DataComponents.ITEM_NAME, Apotheosis.lang("banner", "bastion_guard").withStyle(ChatFormatting.RED));
        return itemstack;
    }

    protected void addSet(String name, int weight, float quality, UnaryOperator<GSBuilder> config) {
        this.add(Apotheosis.loc(name), config.apply(new GSBuilder(weight, quality)).build());
    }

    protected void addSet(String name, int weight, float quality, UnaryOperator<GSBuilder> config, ICondition... conditions) {
        this.addConditionally(Apotheosis.loc(name), config.apply(new GSBuilder(weight, quality)).build(), conditions);
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

        public GSBuilder helmet(ItemStack stack, int weight, float dropChance) {
            this.helmets.add(new WeightedItemStack(stack, Weight.of(weight), dropChance));
            return this;
        }

        public GSBuilder helmet(ItemStack stack, int weight) {
            return helmet(stack, weight, -1);
        }

        public GSBuilder chestplate(ItemStack stack, int weight, float dropChance) {
            this.chestplates.add(new WeightedItemStack(stack, Weight.of(weight), dropChance));
            return this;
        }

        public GSBuilder chestplate(ItemStack stack, int weight) {
            return chestplate(stack, weight, -1);
        }

        public GSBuilder leggings(ItemStack stack, int weight, float dropChance) {
            this.leggings.add(new WeightedItemStack(stack, Weight.of(weight), dropChance));
            return this;
        }

        public GSBuilder leggings(ItemStack stack, int weight) {
            return leggings(stack, weight, -1);
        }

        public GSBuilder boots(ItemStack stack, int weight, float dropChance) {
            this.boots.add(new WeightedItemStack(stack, Weight.of(weight), dropChance));
            return this;
        }

        public GSBuilder boots(ItemStack stack, int weight) {
            return boots(stack, weight, -1);
        }

        public GSBuilder mainhand(ItemStack stack, int weight, float dropChance) {
            this.mainhands.add(new WeightedItemStack(stack, Weight.of(weight), dropChance));
            return this;
        }

        public GSBuilder mainhand(ItemStack stack, int weight) {
            return mainhand(stack, weight, -1);
        }

        public GSBuilder offhand(ItemStack stack, int weight, float dropChance) {
            this.offhands.add(new WeightedItemStack(stack, Weight.of(weight), dropChance));
            return this;
        }

        public GSBuilder offhand(ItemStack stack, int weight) {
            return offhand(stack, weight, -1);
        }

        public GSBuilder tag(String tag) {
            this.tags.add(tag);
            return this;
        }

        public GearSet build() {
            return new GearSet(weight, quality, mainhands, offhands, boots, leggings, chestplates, helmets, tags);
        }
    }
}
