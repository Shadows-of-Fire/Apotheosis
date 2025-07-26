package dev.shadowsoffire.apotheosis.data;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.trades.AutomaticAffixTrade;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import dev.shadowsoffire.placebo.systems.wanderer.BasicWandererTrade;
import dev.shadowsoffire.placebo.systems.wanderer.WandererTrade;
import dev.shadowsoffire.placebo.systems.wanderer.WandererTradesRegistry;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class WandererTradesProvider extends DynamicRegistryProvider<WandererTrade> {

    // TODO: Make one of these in AS directly.
    public static final ResourceKey<Enchantment> CAPTURING = ResourceKey.create(Registries.ENCHANTMENT, ApothicSpawners.loc("capturing"));

    public WandererTradesProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries, WandererTradesRegistry.INSTANCE);
    }

    @Override
    public String getName() {
        return "Wanderer Trades";
    }

    @Override
    public void generate() {
        HolderLookup.Provider registries = this.lookupProvider.join();

        this.add("beacon", b -> b
            .rare()
            .price(Items.EMERALD, 25)
            .forSale(Items.BEACON, 1)
            .xp(50));

        this.add("blaze_powder", b -> b
            .price(Items.EMERALD, 1)
            .forSale(Items.BLAZE_POWDER, 3)
            .maxTrades(5)
            .xp(5));

        this.add("diamond_horse_armor", b -> b
            .price(Items.EMERALD, 6)
            .forSale(Items.DIAMOND_HORSE_ARMOR, 1)
            .maxTrades(1)
            .xp(50));

        this.add("diamond", b -> b
            .price(Items.EMERALD, 3)
            .forSale(Items.DIAMOND, 1)
            .maxTrades(5)
            .xp(15));

        this.add("enchanted_golden_apple", b -> b
            .rare()
            .price(Items.EMERALD, 15)
            .forSale(Items.ENCHANTED_GOLDEN_APPLE, 1)
            .maxTrades(1)
            .xp(15));

        this.add("eye_of_ender", b -> b
            .price(Items.EMERALD, 2)
            .forSale(Items.ENDER_EYE, 1)
            .maxTrades(3)
            .xp(15));

        this.add("gold_ingot", b -> b
            .price(Items.EMERALD, 2)
            .forSale(Items.GOLD_INGOT, 1)
            .maxTrades(8)
            .xp(5));

        this.add("golden_horse_armor", b -> b
            .price(Items.EMERALD, 4)
            .forSale(Items.GOLDEN_HORSE_ARMOR, 1)
            .maxTrades(2)
            .xp(30));

        this.add("iron_horse_armor", b -> b
            .price(Items.EMERALD, 2)
            .forSale(Items.IRON_HORSE_ARMOR, 1)
            .maxTrades(3)
            .xp(15));

        this.add("iron_ingot", b -> b
            .price(Items.EMERALD, 1)
            .forSale(Items.IRON_INGOT, 3)
            .maxTrades(15)
            .xp(5));

        this.add("prismarine_shard", b -> b
            .price(Items.EMERALD, 4)
            .forSale(Items.PRISMARINE_SHARD, 5)
            .maxTrades(5)
            .xp(15));

        this.add("saddle", b -> b
            .price(Items.EMERALD, 2)
            .forSale(Items.SADDLE, 1)
            .maxTrades(2)
            .xp(30));

        this.add("skeleton_skull", b -> b
            .price(Items.EMERALD, 4)
            .forSale(Items.SKELETON_SKULL, 1)
            .maxTrades(5)
            .xp(15));

        this.add("totem_of_undying", b -> b
            .rare()
            .price(Items.EMERALD, 10)
            .forSale(Items.TOTEM_OF_UNDYING, 1)
            .maxTrades(1)
            .xp(50));

        this.add("wither_skeleton_skull", b -> b
            .price(Items.EMERALD, 5)
            .forSale(Items.WITHER_SKELETON_SKULL, 1)
            .maxTrades(5)
            .xp(15));

        this.add("zombie_head", b -> b
            .price(Items.EMERALD, 2)
            .forSale(Items.ZOMBIE_HEAD, 1)
            .maxTrades(5)
            .xp(15));

        this.add("rare_gear/arachnids_fear", b -> b
            .rare()
            .price(Items.DIAMOND_SWORD, 1)
            .price2(Items.EMERALD, 45)
            .forSale(enchantedItem(registries, c -> c
                .item(Items.DIAMOND_SWORD)
                .name("arachnids_fear")
                .nameColor(0xC11101)
                .enchant(Enchantments.BANE_OF_ARTHROPODS, 10)
                .enchant(Enchantments.MENDING, 1)
                .enchant(Enchantments.LOOTING, 5)
                .enchant(Enchantments.UNBREAKING, 5)
                .enchant(Enchantments.FIRE_ASPECT, 5)))
            .maxTrades(1)
            .xp(500));

        this.add("rare_gear/bonesplitter", b -> b
            .rare()
            .price(Items.DIAMOND_AXE, 1)
            .price2(Items.EMERALD, 64)
            .forSale(enchantedItem(registries, c -> c
                .item(Items.DIAMOND_AXE)
                .name("bonesplitter")
                .nameColor(0x9AB091)
                .enchant(Enchantments.SHARPNESS, 10)
                .enchant(Enchantments.MENDING, 1)
                .enchant(Ench.Enchantments.SCAVENGER, 2)
                .enchant(CAPTURING, 2)
                .enchant(Enchantments.LOOTING, 5)
                .enchant(Enchantments.UNBREAKING, 3)))
            .maxTrades(1)
            .xp(500));

        this.add("rare_gear/captive_dreams", b -> b
            .rare()
            .price(Items.DIAMOND_SWORD, 1)
            .price2(Items.EMERALD, 45)
            .forSale(enchantedItem(registries, c -> c
                .item(Items.DIAMOND_SWORD)
                .name("captive_dreams")
                .nameColor(0xADD8E6)
                .enchant(Enchantments.SHARPNESS, 5)
                .enchant(Enchantments.MENDING, 1)
                .enchant(Enchantments.LOOTING, 4)
                .enchant(Enchantments.UNBREAKING, 5)
                .enchant(CAPTURING, 5)))
            .maxTrades(1)
            .xp(500));

        this.add("rare_gear/eternal_vigilance", b -> b
            .rare()
            .price(Items.DIAMOND, 64)
            .price2(Items.PHANTOM_MEMBRANE, 32)
            .forSale(enchantedItem(registries, c -> c
                .item(Items.DIAMOND_SWORD)
                .name("eternal_vigilance")
                .enchant(Enchantments.SHARPNESS, 10)
                .enchant(Ench.Enchantments.LIFE_MENDING, 5)
                .enchant(Enchantments.LOOTING, 5)
                .enchant(Enchantments.UNBREAKING, 5)
                .enchant(Ench.Enchantments.SCAVENGER, 5)))
            .maxTrades(1)
            .xp(1000));

        this.add("rare_gear/greatplate_of_eternity", b -> b
            .rare()
            .price(Items.DIAMOND_CHESTPLATE, 1)
            .price2(Items.EMERALD, 55)
            .forSale(enchantedItem(registries, c -> c
                .item(Items.DIAMOND_CHESTPLATE)
                .name("greatplate_of_eternity")
                .enchant(Enchantments.PROTECTION, 5)
                .enchant(Enchantments.PROJECTILE_PROTECTION, 5)
                .enchant(Enchantments.MENDING, 1)
                .enchant(Enchantments.UNBREAKING, 5)
                .enchant(Ench.Enchantments.BERSERKERS_FURY, 2)))
            .maxTrades(1)
            .xp(500));

        this.add("rare_gear/rune_forged_greaves", b -> b
            .rare()
            .price(Items.DIAMOND_BOOTS, 1)
            .price2(Items.EMERALD, 45)
            .forSale(enchantedItem(registries, c -> c
                .item(Items.DIAMOND_BOOTS)
                .name("rune_forged_greaves")
                .enchant(Enchantments.PROTECTION, 5)
                .enchant(Enchantments.MENDING, 1)
                .enchant(Enchantments.UNBREAKING, 5)
                .enchant(Enchantments.FEATHER_FALLING, 5)
                .enchant(Ench.Enchantments.STABLE_FOOTING, 1)))
            .maxTrades(1)
            .xp(500));

        this.add("rare_gear/stonebreaker", b -> b
            .rare()
            .price(Items.DIAMOND_PICKAXE, 1)
            .price2(Items.EMERALD, 45)
            .forSale(enchantedItem(registries, c -> c
                .item(Items.DIAMOND_PICKAXE)
                .name("stonebreaker")
                .enchant(Enchantments.EFFICIENCY, 5)
                .enchant(Enchantments.MENDING, 1)
                .enchant(Enchantments.FORTUNE, 4)
                .enchant(Enchantments.UNBREAKING, 5)
                .enchant(Ench.Enchantments.BOON_OF_THE_EARTH, 4)))
            .maxTrades(1)
            .xp(500));

        this.add("rare_gear/thunder_forged_legguards", b -> b
            .rare()
            .price(Items.DIAMOND_LEGGINGS, 1)
            .price2(Items.EMERALD, 55)
            .forSale(enchantedItem(registries, c -> c
                .item(Items.DIAMOND_LEGGINGS)
                .name("thunder_forged_legguards")
                .enchant(Enchantments.PROTECTION, 5)
                .enchant(Enchantments.MENDING, 1)
                .enchant(Enchantments.UNBREAKING, 5)
                .enchant(Ench.Enchantments.REBOUNDING, 10)))
            .maxTrades(1)
            .xp(500));

        this.add("rare_gear/timeworn_visage", b -> b
            .rare()
            .price(Items.DIAMOND_HELMET, 1)
            .price2(Items.EMERALD, 45)
            .forSale(enchantedItem(registries, c -> c
                .item(Items.DIAMOND_HELMET)
                .name("timeworn_visage")
                .enchant(Enchantments.PROTECTION, 5)
                .enchant(Enchantments.MENDING, 1)
                .enchant(Enchantments.UNBREAKING, 5)
                .enchant(Enchantments.RESPIRATION, 5)
                .enchant(Enchantments.AQUA_AFFINITY, 1)))
            .maxTrades(1)
            .xp(500));

        this.add("rare_gear/treecapitator", b -> b
            .rare()
            .price(Items.DIAMOND_AXE, 1)
            .price2(Items.EMERALD, 45)
            .forSale(enchantedItem(registries, c -> c
                .item(Items.DIAMOND_AXE)
                .name("treecapitator")
                .nameColor(0x608F07)
                .enchant(Enchantments.EFFICIENCY, 10)
                .enchant(Enchantments.MENDING, 1)
                .enchant(Enchantments.SILK_TOUCH, 1)
                .enchant(Enchantments.UNBREAKING, 5)
                .enchant(Ench.Enchantments.CHAINSAW, 1)))
            .maxTrades(1)
            .xp(500));

        // Need to add a bunch of these so they sufficiently fill the trade pool.
        // Otherwise they become way too rare.
        this.addAffixTrade("affix/automatic_1");
        this.addAffixTrade("affix/automatic_2");
        this.addAffixTrade("affix/automatic_3");
        this.addAffixTrade("affix/automatic_4");
        this.addAffixTrade("affix/automatic_5");
        this.addAffixTrade("affix/automatic_6");
        this.addAffixTrade("affix/automatic_7");
        this.addAffixTrade("affix/automatic_8");
        this.addAffixTrade("affix/automatic_9");
        this.addAffixTrade("affix/automatic_10");
    }

    public void addAffixTrade(String path) {
        this.add(Apotheosis.loc(path), new AutomaticAffixTrade(Set.of(), List.of(), false));
    }

    public void add(String path, UnaryOperator<Builder> builder) {
        this.add(Apotheosis.loc(path), builder.apply(new Builder()).build());
    }

    public ItemStack enchantedItem(HolderLookup.Provider registries, UnaryOperator<EnchantedItemBuilder> config) {
        return config.apply(new EnchantedItemBuilder(registries)).build();
    }

    private static class EnchantedItemBuilder {

        protected final HolderLookup.Provider registries;

        protected Item item;
        protected String name;
        protected ItemEnchantments.Mutable enchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        protected int nameColor = 0x1ABBE0;

        public EnchantedItemBuilder(HolderLookup.Provider registries) {
            this.registries = registries;
        }

        public EnchantedItemBuilder item(Item item) {
            this.item = item;
            return this;
        }

        public EnchantedItemBuilder name(String name) {
            this.name = name;
            return this;
        }

        public EnchantedItemBuilder nameColor(int nameColor) {
            this.nameColor = nameColor;
            return this;
        }

        public EnchantedItemBuilder enchant(ResourceKey<Enchantment> ench, int level) {
            Holder<Enchantment> holder = ApothMiscUtil.standaloneHolder(this.registries, ench);
            this.enchants.upgrade(holder, level);
            return this;
        }

        public ItemStack build() {
            ItemStack stack = new ItemStack(item);
            stack.set(DataComponents.CUSTOM_NAME, Apotheosis.lang("name", this.name).withStyle(Style.EMPTY.withItalic(false).withColor(this.nameColor)));
            stack.set(DataComponents.ENCHANTMENTS, enchants.toImmutable());
            return stack;
        }
    }

    public static class Builder {
        private ItemStack price = ItemStack.EMPTY;
        private ItemStack price2 = ItemStack.EMPTY;
        private ItemStack forSale = ItemStack.EMPTY;
        private int maxTrades = 1;
        private int xp = 0;
        private float priceMult = 1F;
        private boolean rare = false;

        public Builder price(ItemStack price) {
            this.price = price;
            return this;
        }

        public Builder price(Item price, int count) {
            return price(new ItemStack(price, count));
        }

        public Builder price2(ItemStack price2) {
            this.price2 = price2;
            return this;
        }

        public Builder price2(Item price, int count) {
            return price2(new ItemStack(price, count));
        }

        public Builder forSale(ItemStack forSale) {
            this.forSale = forSale;
            return this;
        }

        public Builder forSale(Item output, int count) {
            return forSale(new ItemStack(output, count));
        }

        public Builder maxTrades(int maxTrades) {
            this.maxTrades = maxTrades;
            return this;
        }

        public Builder xp(int xp) {
            this.xp = xp;
            return this;
        }

        public Builder priceMult(float priceMult) {
            this.priceMult = priceMult;
            return this;
        }

        public Builder rare() {
            this.rare = true;
            return this;
        }

        public BasicWandererTrade build() {
            return new BasicWandererTrade(price, price2, forSale, maxTrades, xp, priceMult, rare);
        }
    }
}
