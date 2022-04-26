
package shadows.apotheosis.deadly;

import java.util.function.Consumer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.core.Registry;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.affix.AffixLootEntry;
import shadows.apotheosis.deadly.loot.LootController;
import shadows.apotheosis.deadly.loot.LootRarity;
import shadows.apotheosis.deadly.reload.AffixLootManager;
import shadows.apotheosis.ench.EnchModule;
import shadows.placebo.loot.LootSystem;
import shadows.placebo.loot.PoolBuilder;
import shadows.placebo.loot.StackLootEntry;

/**
 * Loot entries for deadly module
 * TODO: Make configurable.
 * @author Shadows
 *
 */
public class DeadlyLoot {

    public static final ResourceLocation BRUTAL = new ResourceLocation(Apotheosis.MODID, "spawner_brutal");
    public static final ResourceLocation BRUTAL_ROTATE = new ResourceLocation(Apotheosis.MODID, "spawner_brutal_rotate");
    public static final ResourceLocation SWARM = new ResourceLocation(Apotheosis.MODID, "spawner_swarm");
    public static final ResourceLocation VALUABLE = new ResourceLocation(Apotheosis.MODID, "chest_valuable");
    public static final ResourceLocation TOME_TOWER = new ResourceLocation(Apotheosis.MODID, "tome_tower");

    public static void init() {
        PoolBuilder build = new PoolBuilder(5, 8);
        build.setBonusRolls(BinomialDistributionGenerator.binomial(1, 3));
        // (ItemStack item, int min, int max, int weight, int quality)
        build.addEntries(new StackLootEntry(Items.SKELETON_SKULL, 1, 1, 1, 2));
        build.addEntries(new StackLootEntry(Items.WITHER_SKELETON_SKULL, 1, 1, 1, 2));
        build.addEntries(new StackLootEntry(Items.CREEPER_HEAD, 1, 1, 1, 2));
        build.addEntries(new StackLootEntry(Items.ZOMBIE_HEAD, 1, 1, 1, 2));
        build.addEntries(new StackLootEntry(Items.PLAYER_HEAD, 1, 1, 1, 2));
        build.addEntries(new StackLootEntry(Blocks.TNT, 1, 1, 2, 0));
        build.addEntries(new StackLootEntry(Items.DIAMOND, 1, 3, 3, 5));
        build.addEntries(new StackLootEntry(Items.EMERALD, 1, 3, 3, 6));
        build.addEntries(new StackLootEntry(Items.IRON_INGOT, 1, 5, 10, 3));
        build.addEntries(new StackLootEntry(Items.GOLD_INGOT, 1, 5, 10, 4));
        build.addEntries(new StackLootEntry(Items.GOLDEN_APPLE, 1, 1, 1, 3));
        build.addEntries(new StackLootEntry(Items.NAME_TAG, 1, 1, 5, 0));
        build.addEntries(new StackLootEntry(Items.LEAD, 1, 1, 5, 0));
        build.addEntries(new StackLootEntry(Items.SADDLE, 1, 1, 3, 0));
        build.addEntries(new StackLootEntry(Items.DIAMOND_HORSE_ARMOR, 1, 1, 1, 5));
        build.addEntries(new StackLootEntry(Items.SLIME_BALL, 1, 3, 3, 1));
        build.addEntries(new StackLootEntry(Items.BUCKET, 1, 1, 3, 0));
        build.addEntries(new StackLootEntry(Blocks.ANVIL, 1, 1, 3, 0));
        build.addEntries(new StackLootEntry(Blocks.ENCHANTING_TABLE, 1, 1, 3, 0));
        build.addEntries(new StackLootEntry(Blocks.IRON_BLOCK, 1, 1, 3, 0));
        build.addEntries(new EnchantedEntry(Items.ENCHANTED_BOOK, 3));
        build.addEntries(new AffixEntry(8, 5));
        LootSystem.registerLootTable(BRUTAL, LootSystem.tableBuilder().withPool(build).build());

        build = new PoolBuilder(5, 8);
        build.setBonusRolls(UniformGenerator.between(1, 3));
        build.addEntries(new StackLootEntry(Items.SKELETON_SKULL, 1, 2, 1, 2));
        build.addEntries(new StackLootEntry(Items.WITHER_SKELETON_SKULL, 1, 2, 1, 2));
        build.addEntries(new StackLootEntry(Items.CREEPER_HEAD, 1, 2, 1, 2));
        build.addEntries(new StackLootEntry(Items.ZOMBIE_HEAD, 1, 2, 1, 2));
        build.addEntries(new StackLootEntry(Items.PLAYER_HEAD, 1, 2, 1, 2));
        build.addEntries(new StackLootEntry(Blocks.TNT, 1, 2, 2, 0));
        build.addEntries(new StackLootEntry(Items.DIAMOND, 1, 4, 3, 5));
        build.addEntries(new StackLootEntry(Items.EMERALD, 1, 4, 3, 6));
        build.addEntries(new StackLootEntry(Items.IRON_INGOT, 1, 7, 10, 3));
        build.addEntries(new StackLootEntry(Items.GOLD_INGOT, 1, 7, 10, 4));
        build.addEntries(new StackLootEntry(Items.GOLDEN_APPLE, 1, 2, 1, 3));
        build.addEntries(new StackLootEntry(Items.NAME_TAG, 1, 2, 5, 0));
        build.addEntries(new StackLootEntry(Items.LEAD, 1, 2, 5, 0));
        build.addEntries(new StackLootEntry(Items.SADDLE, 1, 2, 3, 0));
        build.addEntries(new StackLootEntry(Items.DIAMOND_HORSE_ARMOR, 1, 2, 1, 5));
        build.addEntries(new StackLootEntry(Items.SLIME_BALL, 1, 5, 3, 1));
        build.addEntries(new StackLootEntry(Items.BUCKET, 1, 1, 3, 0));
        build.addEntries(new StackLootEntry(Blocks.ANVIL, 1, 1, 3, 0));
        build.addEntries(new StackLootEntry(Blocks.ENCHANTING_TABLE, 1, 1, 3, 0));
        build.addEntries(new StackLootEntry(Blocks.IRON_BLOCK, 1, 3, 3, 0));
        build.addEntries(new EnchantedEntry(Items.BOOK, 3));
        build.addEntries(new AffixEntry(8, 5));
        build.addEntries(new AffixEntry(8, 8));
        LootSystem.registerLootTable(BRUTAL_ROTATE, LootSystem.tableBuilder().withPool(build).build());

        build = new PoolBuilder(5, 6);
        build.setBonusRolls(UniformGenerator.between(1, 4));
        build.addEntries(new StackLootEntry(egg("creeper"), 1, 3, 1, 1));
        build.addEntries(new StackLootEntry(egg("skeleton"), 1, 3, 1, 1));
        build.addEntries(new StackLootEntry(egg("spider"), 1, 3, 1, 1));
        build.addEntries(new StackLootEntry(egg("zombie"), 1, 3, 1, 1));
        build.addEntries(new StackLootEntry(egg("slime"), 1, 3, 1, 1));
        build.addEntries(new StackLootEntry(egg("enderman"), 1, 3, 1, 1));
        build.addEntries(new StackLootEntry(egg("cave_spider"), 1, 3, 1, 1));
        build.addEntries(new StackLootEntry(egg("silverfish"), 1, 3, 1, 1));
        build.addEntries(new StackLootEntry(Items.DIAMOND, 1, 3, 3, 4));
        build.addEntries(new StackLootEntry(Items.EMERALD, 1, 3, 3, 4));
        build.addEntries(new StackLootEntry(Items.IRON_INGOT, 1, 5, 10, 1));
        build.addEntries(new StackLootEntry(Items.GOLD_INGOT, 1, 5, 10, 3));
        build.addEntries(new StackLootEntry(Items.GOLDEN_APPLE, 1, 1, 1, 2));
        build.addEntries(new StackLootEntry(Items.NAME_TAG, 1, 1, 5, 1));
        build.addEntries(new StackLootEntry(Items.LEAD, 1, 1, 5, 1));
        build.addEntries(new StackLootEntry(Items.SADDLE, 1, 1, 3, 1));
        build.addEntries(new StackLootEntry(Items.DIAMOND_HORSE_ARMOR, 1, 1, 1, 3));
        build.addEntries(new StackLootEntry(Items.SLIME_BALL, 1, 3, 3, 0));
        build.addEntries(new StackLootEntry(Items.BUCKET, 1, 1, 3, 0));
        build.addEntries(new StackLootEntry(Blocks.ANVIL, 1, 1, 3, 0));
        build.addEntries(new StackLootEntry(Blocks.OBSIDIAN, 3, 8, 3, 0));
        build.addEntries(new EnchantedEntry(Items.BOOK, 3));
        build.addEntries(new AffixEntry(8, 5));
        LootSystem.registerLootTable(SWARM, LootSystem.tableBuilder().withPool(build).build());

        build = new PoolBuilder(6, 12);
        build.setBonusRolls(UniformGenerator.between(2, 5));
        build.addEntries(new StackLootEntry(potion(Potions.STRONG_REGENERATION), 1, 1, 20, 10));
        build.addEntries(new StackLootEntry(potion(Potions.STRONG_SWIFTNESS), 1, 1, 20, 10));
        build.addEntries(new StackLootEntry(potion(Potions.LONG_FIRE_RESISTANCE), 1, 1, 20, 10));
        build.addEntries(new StackLootEntry(potion(Items.SPLASH_POTION, Potions.STRONG_HEALING), 1, 1, 20, 10));
        build.addEntries(new StackLootEntry(potion(Potions.LONG_NIGHT_VISION), 1, 1, 20, 10));
        build.addEntries(new StackLootEntry(potion(Potions.LONG_STRENGTH), 1, 1, 20, 10));
        build.addEntries(new StackLootEntry(potion(Potions.LONG_INVISIBILITY), 1, 1, 20, 10));
        build.addEntries(new StackLootEntry(potion(Potions.LONG_WATER_BREATHING), 1, 1, 20, 10));
        build.addEntries(new StackLootEntry(Items.DIAMOND, 1, 3, 30, 4));
        build.addEntries(new StackLootEntry(Items.EMERALD, 1, 3, 30, 4));
        build.addEntries(new StackLootEntry(Items.IRON_INGOT, 1, 5, 100, 1));
        build.addEntries(new StackLootEntry(Items.GOLD_INGOT, 1, 5, 100, 3));
        build.addEntries(new StackLootEntry(Items.ENCHANTED_GOLDEN_APPLE, 1, 1, 1, 15));
        build.addEntries(new StackLootEntry(Items.NAME_TAG, 1, 2, 50, 1));
        build.addEntries(new StackLootEntry(Items.LEAD, 1, 2, 50, 1));
        build.addEntries(new StackLootEntry(Items.SADDLE, 1, 2, 40, 1));
        build.addEntries(new StackLootEntry(Items.DIAMOND_HORSE_ARMOR, 1, 1, 40, 3));
        build.addEntries(new StackLootEntry(Items.SLIME_BALL, 3, 6, 50, 0));
        build.addEntries(new StackLootEntry(Items.BUCKET, 1, 1, 50, 0));
        build.addEntries(new EnchantedEntry(Items.DIAMOND_SWORD, 30));
        build.addEntries(new EnchantedEntry(Items.DIAMOND_AXE, 30));
        build.addEntries(new EnchantedEntry(Items.DIAMOND_PICKAXE, 30));
        build.addEntries(new EnchantedEntry(Items.DIAMOND_BOOTS, 20));
        build.addEntries(new EnchantedEntry(Items.DIAMOND_LEGGINGS, 20));
        build.addEntries(new EnchantedEntry(Items.DIAMOND_HELMET, 20));
        build.addEntries(new EnchantedEntry(Items.DIAMOND_CHESTPLATE, 20));
        build.addEntries(new EnchantedEntry(Items.BOOK, 20));
        build.addEntries(new AffixEntry(20, 15));
        LootSystem.registerLootTable(VALUABLE, LootSystem.tableBuilder().withPool(build).build());

        if (Apotheosis.enableEnch) {
            build = new PoolBuilder(6, 9);
            build.setBonusRolls(UniformGenerator.between(0, 3));
            for (Item i : EnchModule.TYPED_BOOKS)
                build.addEntries(new TomeEntry(i, 5));
            build.addEntries(new EnchantedEntry(Items.BOOK, 5));
            for (int i = 0; i < 5; i++)
                build.addEntries(new StackLootEntry(DeadlyModule.RARITY_TOMES.get(LootRarity.values()[i]), 1, 1, 16 - 3 * i, 10));
            build.addEntries(new AffixEntry(20, 35));
            LootSystem.registerLootTable(TOME_TOWER, LootSystem.tableBuilder().withPool(build).build());
        }
    }

    private static ItemStack egg(String mob) {
        return new ItemStack(SpawnEggItem.BY_ID.get(ForgeRegistries.ENTITIES.getValue(new ResourceLocation(mob))));
    }

    private static ItemStack potion(Potion type) {
        return potion(Items.POTION, type);
    }

    private static ItemStack potion(Item pot, Potion type) {
        ItemStack s = new ItemStack(pot);
        PotionUtils.setPotion(s, type);
        return s;
    }

    public static class AffixEntry extends LootPoolSingletonContainer {

        public static final Serializer SERIALIZER = new Serializer();
        public static final LootPoolEntryType AFFIX_TYPE = Registry.register(Registry.LOOT_POOL_ENTRY_TYPE, new ResourceLocation(Apotheosis.MODID, "affix_entry"), new LootPoolEntryType(SERIALIZER));

        public AffixEntry(int weight, int quality) {
            this(weight, quality, new LootItemCondition[0], new LootItemFunction[0]);
        }

        public AffixEntry(int weight, int quality, LootItemCondition[] cond, LootItemFunction[] fun) {
            super(weight, quality, cond, fun);
        }

        @Override
        protected void createItemStack(Consumer<ItemStack> list, LootContext ctx) {
            LootRarity rarity = LootRarity.random(ctx.getRandom());
            AffixLootEntry entry = AffixLootManager.getRandomEntry(ctx.getRandom()).get();
            ItemStack stack = entry.getStack().copy();
            stack.getOrCreateTag().putBoolean("apoth_rchest", true);
            list.accept(LootController.lootifyItem(stack, rarity, ctx.getRandom()));
        }

        @Override
        public LootPoolEntryType getType() {
            return AFFIX_TYPE;
        }

        public static class Serializer extends LootPoolSingletonContainer.Serializer<AffixEntry> {

            @Override
            protected AffixEntry deserialize(JsonObject jsonObject, JsonDeserializationContext context, int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions) {
                return new AffixEntry(weight, quality, conditions, functions);
            }

        }
    }

    public static class TomeEntry extends EnchantedEntry {

        public TomeEntry(Item i, int weight) {
            super(i, weight);
        }

        @Override
        protected void createItemStack(Consumer<ItemStack> list, LootContext ctx) {
            ItemStack enchTome = this.func.apply(new ItemStack(this.i), ctx);
            ItemStack ench = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantmentHelper.getEnchantments(enchTome).entrySet().stream().map(e -> new EnchantmentInstance(e.getKey(), e.getValue())).forEach(d -> EnchantedBookItem.addEnchantment(ench, d));
            list.accept(ench);
        }

    }

    public static class EnchantedEntry extends StackLootEntry {
        protected final LootItemFunction func = EnchantRandomlyFunction.randomApplicableEnchantment().build();
        protected Item i;

        public EnchantedEntry(Item i, int weight) {
            super(i, 1, 1, weight, 5);
            this.i = i;
        }

        @Override
        protected void createItemStack(Consumer<ItemStack> list, LootContext ctx) {
            list.accept(this.func.apply(new ItemStack(this.i), ctx));
        }

    }

}
