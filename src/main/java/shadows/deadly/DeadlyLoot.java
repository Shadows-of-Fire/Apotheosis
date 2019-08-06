package shadows.deadly;

import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import shadows.Apotheosis;
import shadows.placebo.loot.PlaceboLootPool.PoolBuilder;
import shadows.placebo.loot.PlaceboLootSystem;
import shadows.util.ChestBuilder;
import shadows.util.ChestBuilder.EnchantedEntry;

/**
 * Loot entries for deadly module
 * TODO: Make configurable.
 * @author Shadows
 *
 */
public class DeadlyLoot {

	public static final ResourceLocation DUNGEON = LootTableList.CHESTS_SIMPLE_DUNGEON;

	public static final ResourceLocation SPAWNER_BRUTAL = new ResourceLocation(Apotheosis.MODID, "spawner_brutal");
	public static final ResourceLocation SPAWNER_SWARM = new ResourceLocation(Apotheosis.MODID, "spawner_swarm");

	public static final ResourceLocation CHEST_VALUABLE = new ResourceLocation(Apotheosis.MODID, "chest_valuable");

	public static void init() {
		PoolBuilder build = new PoolBuilder(5, 8, 1, 3);
		build.addEntries(ChestBuilder.loot(Items.SKULL, 0, 1, 1, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.SKULL, 1, 1, 1, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.SKULL, 2, 1, 1, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.SKULL, 3, 1, 1, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.SKULL, 4, 1, 1, 1, 2));
		build.addEntries(ChestBuilder.loot(Blocks.TNT, 0, 1, 1, 2, 0));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND, 0, 1, 3, 3, 5));
		build.addEntries(ChestBuilder.loot(Items.EMERALD, 0, 1, 3, 3, 6));
		build.addEntries(ChestBuilder.loot(Items.IRON_INGOT, 0, 1, 5, 10, 3));
		build.addEntries(ChestBuilder.loot(Items.GOLD_INGOT, 0, 1, 5, 10, 4));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_APPLE, 0, 1, 1, 1, 3));
		build.addEntries(ChestBuilder.loot(Items.NAME_TAG, 0, 1, 1, 5, 0));
		build.addEntries(ChestBuilder.loot(Items.LEAD, 0, 1, 1, 5, 0));
		build.addEntries(ChestBuilder.loot(Items.SADDLE, 0, 1, 1, 3, 0));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_HORSE_ARMOR, 0, 1, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 0, 1, 3, 3, 1));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 0, 1, 1, 3, 0));
		build.addEntries(ChestBuilder.loot(Blocks.ANVIL, 0, 1, 1, 3, 0));
		build.addEntries(ChestBuilder.loot(Blocks.ENCHANTING_TABLE, 0, 1, 1, 3, 0));
		build.addEntries(ChestBuilder.loot(Blocks.IRON_BLOCK, 0, 1, 1, 3, 0));
		build.addEntries(new EnchantedEntry(Items.BOOK, 3));
		PlaceboLootSystem.registerLootTable(SPAWNER_BRUTAL, new LootTable(new LootPool[] { build.build() }));

		build = new PoolBuilder(5, 6, 1, 4);
		build.addEntries(ChestBuilder.loot(egg("creeper"), 1, 3, 1, 1));
		build.addEntries(ChestBuilder.loot(egg("skeleton"), 1, 3, 1, 1));
		build.addEntries(ChestBuilder.loot(egg("spider"), 1, 3, 1, 1));
		build.addEntries(ChestBuilder.loot(egg("zombie"), 1, 3, 1, 1));
		build.addEntries(ChestBuilder.loot(egg("slime"), 1, 3, 1, 1));
		build.addEntries(ChestBuilder.loot(egg("enderman"), 1, 3, 1, 1));
		build.addEntries(ChestBuilder.loot(egg("cave_spider"), 1, 3, 1, 1));
		build.addEntries(ChestBuilder.loot(egg("silverfish"), 1, 3, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND, 0, 1, 3, 3, 4));
		build.addEntries(ChestBuilder.loot(Items.EMERALD, 0, 1, 3, 3, 4));
		build.addEntries(ChestBuilder.loot(Items.IRON_INGOT, 0, 1, 5, 10, 1));
		build.addEntries(ChestBuilder.loot(Items.GOLD_INGOT, 0, 1, 5, 10, 3));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_APPLE, 0, 1, 1, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.NAME_TAG, 0, 1, 1, 5, 1));
		build.addEntries(ChestBuilder.loot(Items.LEAD, 0, 1, 1, 5, 1));
		build.addEntries(ChestBuilder.loot(Items.SADDLE, 0, 1, 1, 3, 1));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_HORSE_ARMOR, 0, 1, 1, 1, 3));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 0, 1, 3, 3, 0));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 0, 1, 1, 3, 0));
		build.addEntries(ChestBuilder.loot(Blocks.ANVIL, 0, 1, 1, 3, 0));
		build.addEntries(ChestBuilder.loot(Blocks.OBSIDIAN, 0, 3, 8, 3, 0));
		build.addEntries(new EnchantedEntry(Items.BOOK, 3));
		PlaceboLootSystem.registerLootTable(SPAWNER_SWARM, new LootTable(new LootPool[] { build.build() }));

		build = new PoolBuilder(6, 12, 2, 5);
		build.addEntries(ChestBuilder.loot(potion(PotionTypes.STRONG_REGENERATION), 1, 1, 20, 10));
		build.addEntries(ChestBuilder.loot(potion(PotionTypes.STRONG_SWIFTNESS), 1, 1, 20, 10));
		build.addEntries(ChestBuilder.loot(potion(PotionTypes.LONG_FIRE_RESISTANCE), 1, 1, 20, 10));
		build.addEntries(ChestBuilder.loot(potion(Items.SPLASH_POTION, PotionTypes.STRONG_HEALING), 1, 1, 20, 10));
		build.addEntries(ChestBuilder.loot(potion(PotionTypes.LONG_NIGHT_VISION), 1, 1, 20, 10));
		build.addEntries(ChestBuilder.loot(potion(PotionTypes.LONG_STRENGTH), 1, 1, 20, 10));
		build.addEntries(ChestBuilder.loot(potion(PotionTypes.LONG_INVISIBILITY), 1, 1, 20, 10));
		build.addEntries(ChestBuilder.loot(potion(PotionTypes.LONG_WATER_BREATHING), 1, 1, 20, 10));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND, 0, 1, 3, 30, 4));
		build.addEntries(ChestBuilder.loot(Items.EMERALD, 0, 1, 3, 30, 4));
		build.addEntries(ChestBuilder.loot(Items.IRON_INGOT, 0, 1, 5, 100, 1));
		build.addEntries(ChestBuilder.loot(Items.GOLD_INGOT, 0, 1, 5, 100, 3));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_APPLE, 1, 1, 1, 1, 15));
		build.addEntries(ChestBuilder.loot(Items.NAME_TAG, 0, 1, 2, 50, 1));
		build.addEntries(ChestBuilder.loot(Items.LEAD, 0, 1, 2, 50, 1));
		build.addEntries(ChestBuilder.loot(Items.SADDLE, 0, 1, 2, 40, 1));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_HORSE_ARMOR, 0, 1, 1, 40, 3));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 0, 3, 6, 50, 0));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 0, 1, 1, 50, 0));
		build.addEntries(new EnchantedEntry(Items.DIAMOND_SWORD, 30));
		build.addEntries(new EnchantedEntry(Items.DIAMOND_AXE, 30));
		build.addEntries(new EnchantedEntry(Items.DIAMOND_PICKAXE, 30));
		build.addEntries(new EnchantedEntry(Items.DIAMOND_BOOTS, 20));
		build.addEntries(new EnchantedEntry(Items.DIAMOND_LEGGINGS, 20));
		build.addEntries(new EnchantedEntry(Items.DIAMOND_HELMET, 20));
		build.addEntries(new EnchantedEntry(Items.DIAMOND_CHESTPLATE, 20));
		build.addEntries(new EnchantedEntry(Items.BOOK, 40));
		PlaceboLootSystem.registerLootTable(CHEST_VALUABLE, new LootTable(new LootPool[] { build.build() }));
	}

	private static ItemStack egg(String mob) {
		ItemStack s = new ItemStack(Items.SPAWN_EGG);
		ItemMonsterPlacer.applyEntityIdToItemStack(s, new ResourceLocation(mob));
		return s;
	}

	private static ItemStack potion(PotionType type) {
		return potion(Items.POTIONITEM, type);
	}

	private static ItemStack potion(Item pot, PotionType type) {
		ItemStack s = new ItemStack(pot);
		PotionUtils.addPotionToItemStack(s, type);
		return s;
	}

}
