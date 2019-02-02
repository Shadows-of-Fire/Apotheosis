package shadows.deadly;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import shadows.Apotheosis;
import shadows.placebo.loot.PlaceboLootPool.PoolBuilder;
import shadows.placebo.loot.PlaceboLootSystem;
import shadows.util.ChestBuilder;
import shadows.util.ChestBuilder.EnchBookEntry;

/**
 * Loot entries for deadly module
 * TODO: Make configurable and make defaults not garbage.
 * @author Shadows
 *
 */
public class DeadlyLoot {

	public static final ResourceLocation DUNGEON = LootTableList.CHESTS_SIMPLE_DUNGEON;

	public static final ResourceLocation SPAWNER_BRUTAL = new ResourceLocation(Apotheosis.MODID, "spawner_brutal");
	public static final ResourceLocation SPAWNER_SWARM = new ResourceLocation(Apotheosis.MODID, "spawner_swarm");

	public static final ResourceLocation CHEST_VALUABLE = new ResourceLocation(Apotheosis.MODID, "chest_valuable");

	@SuppressWarnings("deprecation")
	public static void init() {
		PoolBuilder build = new PoolBuilder(5, 5, 0, 3);
		build.addEntries(ChestBuilder.loot(Blocks.TORCH, 0, 4, 12, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.EXPERIENCE_BOTTLE, 0, 2, 4, 10, 1));
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
		build.addEntries(ChestBuilder.loot(Items.APPLE, 0, 1, 3, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_APPLE, 0, 1, 1, 1, 3));
		build.addEntries(ChestBuilder.loot(Items.NAME_TAG, 0, 1, 1, 5, 0));
		build.addEntries(ChestBuilder.loot(Items.LEAD, 0, 1, 1, 5, 0));
		build.addEntries(ChestBuilder.loot(Items.SADDLE, 0, 1, 1, 3, 0));
		build.addEntries(ChestBuilder.loot(Items.FLINT_AND_STEEL, 0, 1, 1, 1, 0));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_HORSE_ARMOR, 0, 1, 1, 1, 0));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_HORSE_ARMOR, 0, 1, 1, 1, 0));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_HORSE_ARMOR, 0, 1, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 0, 1, 3, 3, 1));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 0, 1, 1, 3, 0));
		build.addEntries(new EnchBookEntry(3));
		PlaceboLootSystem.registerLootTable(SPAWNER_BRUTAL, new LootTable(new LootPool[] { build.build() }));

		build = new PoolBuilder(5, 5, 0, 3);
		build.addEntries(ChestBuilder.loot(Blocks.TORCH, 0, 4, 12, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.EXPERIENCE_BOTTLE, 0, 2, 4, 10, 1));
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
		build.addEntries(ChestBuilder.loot(Items.APPLE, 0, 1, 3, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_APPLE, 0, 1, 1, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.NAME_TAG, 0, 1, 1, 5, 1));
		build.addEntries(ChestBuilder.loot(Items.LEAD, 0, 1, 1, 5, 1));
		build.addEntries(ChestBuilder.loot(Items.SADDLE, 0, 1, 1, 3, 1));
		build.addEntries(ChestBuilder.loot(Items.FLINT_AND_STEEL, 0, 1, 1, 1, 0));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_HORSE_ARMOR, 0, 1, 1, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_HORSE_ARMOR, 0, 1, 1, 1, 3));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 0, 1, 3, 3, 0));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 0, 1, 1, 3, 0));
		build.addEntries(new EnchBookEntry(3));
		PlaceboLootSystem.registerLootTable(SPAWNER_SWARM, new LootTable(new LootPool[] { build.build() }));

		build = new PoolBuilder(6, 12, 2, 5);
		build.addEntries(ChestBuilder.loot(Blocks.TORCH, 0, 4, 12, 7));
		build.addEntries(ChestBuilder.loot(Items.EXPERIENCE_BOTTLE, 0, 1, 3, 7));
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8225, 1, 1, 2)); // regeneration II
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8226, 1, 1, 2)); // swiftness II
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8259, 1, 1, 2)); // fire resistance (ext)
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8229, 1, 1, 2)); // healing II
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8262, 1, 1, 2)); // night vision (ext)
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8265, 1, 1, 2)); // strength (ext)
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8270, 1, 1, 2)); // invisibility (ext)
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8269, 1, 1, 2)); // water breathing (ext)
		build.addEntries(ChestBuilder.loot(Items.DIAMOND, 0, 2, 5, 6));
		build.addEntries(ChestBuilder.loot(Items.IRON_INGOT, 0, 5, 12, 10));
		build.addEntries(ChestBuilder.loot(Items.GOLD_INGOT, 0, 4, 15, 10));
		build.addEntries(ChestBuilder.loot(Items.COAL, 0, 26, 54, 10));
		build.addEntries(ChestBuilder.loot(Items.COOKED_BEEF, 0, 3, 6, 15));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_APPLE, 0, 1, 2, 3));
		build.addEntries(ChestBuilder.loot(Items.NAME_TAG, 0, 1, 3, 2));
		build.addEntries(ChestBuilder.loot(Items.LEAD, 0, 1, 3, 5));
		build.addEntries(ChestBuilder.loot(Items.SADDLE, 0, 1, 3, 3));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_HORSE_ARMOR, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_PICKAXE, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_SWORD, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_CHESTPLATE, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_HELMET, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_LEGGINGS, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_BOOTS, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 0, 1, 3, 3));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 0, 1, 1, 3));
		build.addEntries(new EnchBookEntry(4));
		PlaceboLootSystem.registerLootTable(CHEST_VALUABLE, new LootTable(new LootPool[] { build.build() }));
	}
	
	private static ItemStack egg(String mob) {
		ItemStack s = new ItemStack(Items.SPAWN_EGG);
		ItemMonsterPlacer.applyEntityIdToItemStack(s, new ResourceLocation(mob));
		return s;
	}

}
