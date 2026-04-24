package dev.shadowsoffire.apotheosis.data;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apotheosis;
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
        this.addSet("pinnacle/enchanted_netherite", DEFAULT_WEIGHT, 5, c -> c
            .mainhand(buffedItem(Items.NETHERITE_SWORD, enchants, Enchantments.SHARPNESS, 15, 0.8F), 10)
            .mainhand(buffedItem(Items.NETHERITE_AXE, enchants, Enchantments.SHARPNESS, 15, 0.8F), 10)
            .mainhand(buffedItem(Items.NETHERITE_PICKAXE, enchants, Enchantments.FORTUNE, 15, 0.8F), 10)
            .mainhand(buffedItem(Items.NETHERITE_SHOVEL, enchants, Enchantments.FORTUNE, 15, 0.8F), 10)
            .offhand(new ItemStackTemplate(Items.SHIELD), 10)
            .helmet(buffedItem(Items.NETHERITE_HELMET, enchants, Enchantments.PROTECTION, 6, 0.7F), 10)
            .chestplate(buffedItem(Items.NETHERITE_CHESTPLATE, enchants, Enchantments.PROTECTION, 6, 0.7F), 10)
            .leggings(buffedItem(Items.NETHERITE_LEGGINGS, enchants, Enchantments.PROTECTION, 6, 0.7F), 10)
            .boots(buffedItem(Items.NETHERITE_BOOTS, enchants, Enchantments.PROTECTION, 6, 0.7F), 10)
            .tag("pinnacle_melee"));

        this.addSet("pinnacle/ranged/enchanted_netherite", DEFAULT_WEIGHT, 5, c -> c
            .mainhand(buffedItem(Items.BOW, enchants, Enchantments.POWER, 15, 0.8F), 10)
            .mainhand(buffedItem(Items.CROSSBOW, enchants, Enchantments.POWER, 15, 0.8F), 10)
            .helmet(buffedItem(Items.NETHERITE_HELMET, enchants, Enchantments.PROTECTION, 6, 0.7F), 10)
            .chestplate(buffedItem(Items.NETHERITE_CHESTPLATE, enchants, Enchantments.PROTECTION, 6, 0.7F), 10)
            .leggings(buffedItem(Items.NETHERITE_LEGGINGS, enchants, Enchantments.PROTECTION, 6, 0.7F), 10)
            .boots(buffedItem(Items.NETHERITE_BOOTS, enchants, Enchantments.PROTECTION, 6, 0.7F), 10)
            .tag("pinnacle_ranged"));

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
        this.add(Apotheosis.loc(name), config.apply(new GSBuilder(weight, quality)).build());
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

        public GSBuilder tag(String tag) {
            this.tags.add(tag);
            return this;
        }

        public GearSet build() {
            return new GearSet(this.weight, this.quality, this.mainhands, this.offhands, this.boots, this.leggings, this.chestplates, this.helmets, this.tags);
        }
    }
}
