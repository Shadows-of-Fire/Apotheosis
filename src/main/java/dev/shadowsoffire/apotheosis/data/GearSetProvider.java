package dev.shadowsoffire.apotheosis.data;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
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
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.util.random.Weight;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
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
        addSet("pinnacle/enchanted_netherite", DEFAULT_WEIGHT, 5, c -> c
            .mainhand(buffedItem(Items.NETHERITE_SWORD, enchants, 3F), 10)
            .mainhand(buffedItem(Items.NETHERITE_AXE, enchants, 3F), 10)
            .mainhand(buffedItem(Items.NETHERITE_PICKAXE, enchants, 3F), 10)
            .mainhand(buffedItem(Items.NETHERITE_SHOVEL, enchants, 3F), 10)
            .offhand(new ItemStack(Items.SHIELD), 10)
            .helmet(buffedItem(Items.NETHERITE_HELMET, enchants, 2F), 10)
            .chestplate(buffedItem(Items.NETHERITE_CHESTPLATE, enchants, 2F), 10)
            .leggings(buffedItem(Items.NETHERITE_LEGGINGS, enchants, 2F), 10)
            .boots(buffedItem(Items.NETHERITE_BOOTS, enchants, 2F), 10)
            .tag("pinnacle_melee"));

        addSet("pinnacle/ranged/enchanted_netherite", DEFAULT_WEIGHT, 5, c -> c
            .mainhand(buffedItem(Items.BOW, enchants, 3F), 10)
            .mainhand(buffedItem(Items.CROSSBOW, enchants, 3F), 10)
            .helmet(buffedItem(Items.NETHERITE_HELMET, enchants, 2F), 10)
            .chestplate(buffedItem(Items.NETHERITE_CHESTPLATE, enchants, 2F), 10)
            .leggings(buffedItem(Items.NETHERITE_LEGGINGS, enchants, 2F), 10)
            .boots(buffedItem(Items.NETHERITE_BOOTS, enchants, 2F), 10)
            .tag("pinnacle_ranged"));

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
