package shadows.deadly.util;

import java.util.Collection;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.EnchantRandomly;
import shadows.Apotheosis;
import shadows.placebo.loot.PlaceboLootEntry;
import shadows.placebo.loot.PlaceboLootPool.PoolBuilder;
import shadows.placebo.loot.PlaceboLootSystem;

/**
 * The loot table manager for DeadlyWorld.  Uses the Placebo loot system.
 * @author Shadows
 *
 */
public class ChestBuilder {

	public static final ResourceLocation DUNGEON = LootTableList.CHESTS_SIMPLE_DUNGEON;
	public static final ResourceLocation TOWER = new ResourceLocation(Apotheosis.MODID, "tower");
	public static final ResourceLocation SILVER_NEST = new ResourceLocation(Apotheosis.MODID, "silverfish_nest");

	public static final ResourceLocation SPAWNER = new ResourceLocation(Apotheosis.MODID, "spawner");
	public static final ResourceLocation SPAWNER_ARMORED = new ResourceLocation(Apotheosis.MODID, "spawner_armored");
	public static final ResourceLocation SPAWNER_BRUTAL = new ResourceLocation(Apotheosis.MODID, "spawner_brutal");
	public static final ResourceLocation SPAWNER_SWARM = new ResourceLocation(Apotheosis.MODID, "spawner_swarm");
	public static final ResourceLocation SPAWNER_TRAP = new ResourceLocation(Apotheosis.MODID, "spwaner_trap");

	public static final ResourceLocation CHEST_VALUABLE = new ResourceLocation(Apotheosis.MODID, "chest_valuable");
	public static final ResourceLocation[] CHEST_ROGUE = new ResourceLocation[5];

	protected Random random;
	protected TileEntityChest chest;
	protected boolean isValid;

	public ChestBuilder(World world, Random rand, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileEntityChest) {
			this.random = rand;
			this.chest = (TileEntityChest) tileEntity;
			this.isValid = true;
		}
	}

	public ChestBuilder(TileEntityChest tileEntity, Random rand) {
		this.chest = tileEntity;
		if (this.chest != null) {
			this.random = rand;
			this.isValid = true;
		}
	}

	public void fill(ResourceLocation loot) {
		this.chest.setLootTable(loot, random.nextLong());
	}

	public static void init() {
		for (int i = ChestBuilder.CHEST_ROGUE.length; i-- > 0;)
			ChestBuilder.CHEST_ROGUE[i] = new ResourceLocation(Apotheosis.MODID, "chest_rogue_" + Integer.toString(i));

		PoolBuilder build = new PoolBuilder(2, 5, 1, 4);
		build.addEntries(ChestBuilder.loot(Blocks.TORCH, 0, 4, 12, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.IRON_INGOT, 0, 1, 3, 5, 1));
		build.addEntries(ChestBuilder.loot(Items.GOLD_NUGGET, 0, 4, 9, 5, 0));
		build.addEntries(ChestBuilder.loot(Items.FLINT, 0, 1, 5, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.FEATHER, 0, 1, 5, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.BREAD, 0, 1, 3, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.ARROW, 0, 4, 12, 20, 0));
		build.addEntries(ChestBuilder.loot(Items.BOW, 0, 1, 1, 2, 0));
		build.addEntries(ChestBuilder.loot(Items.FIRE_CHARGE, 0, 1, 5, 5, 0));
		build.addEntries(ChestBuilder.loot(Items.FLINT_AND_STEEL, 0, 1, 1, 1, 0));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 0, 1, 3, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 0, 1, 1, 1, 0));
		build.addEntries(new EnchBookEntry(1));
		PlaceboLootSystem.registerLootTable(TOWER, new LootTable(new LootPool[] { build.build() }));

		build = new PoolBuilder(1, 6, 0, 3);
		build.addEntries(ChestBuilder.loot(Blocks.TORCH, 0, 4, 12, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.WHEAT_SEEDS, 0, 1, 3, 3, 0));
		build.addEntries(ChestBuilder.loot(Items.PUMPKIN_SEEDS, 0, 1, 3, 3, 0));
		build.addEntries(ChestBuilder.loot(Items.MELON_SEEDS, 0, 1, 3, 3, 0));
		build.addEntries(ChestBuilder.loot(Items.DYE, 3, 1, 3, 3, 0)); // cocoa beans
		build.addEntries(ChestBuilder.loot(Items.GOLD_NUGGET, 0, 4, 9, 5, 1));
		build.addEntries(ChestBuilder.loot(Items.BONE, 0, 4, 6, 15, 0));
		build.addEntries(ChestBuilder.loot(Items.DYE, 15, 8, 14, 20, 0)); // bonemeal
		build.addEntries(ChestBuilder.loot(Items.ROTTEN_FLESH, 0, 3, 7, 20, 0));
		build.addEntries(ChestBuilder.loot(Items.STRING, 0, 4, 6, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.GUNPOWDER, 0, 4, 6, 10, 1));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 0, 1, 3, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 0, 1, 1, 1, 0));
		build.addEntries(new EnchBookEntry(1));
		PlaceboLootSystem.registerLootTable(SILVER_NEST, new LootTable(new LootPool[] { build.build() }));

		build = new PoolBuilder(4, 4, 2, 2);
		build.addEntries(ChestBuilder.loot(Blocks.TORCH, 0, 4, 12, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND, 0, 1, 2, 3, 3));
		build.addEntries(ChestBuilder.loot(Items.IRON_INGOT, 0, 1, 5, 10, 1));
		build.addEntries(ChestBuilder.loot(Items.GOLD_INGOT, 0, 1, 3, 10, 2));
		build.addEntries(ChestBuilder.loot(Items.REDSTONE, 0, 4, 9, 5, 1));
		build.addEntries(ChestBuilder.loot(Items.BONE, 0, 4, 6, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.ARROW, 0, 4, 12, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.ROTTEN_FLESH, 0, 3, 7, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.STRING, 0, 4, 6, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.GUNPOWDER, 0, 4, 6, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.ENDER_PEARL, 0, 1, 1, 10, 3));
		build.addEntries(ChestBuilder.loot(Items.BREAD, 0, 1, 3, 15, 0));
		build.addEntries(ChestBuilder.loot(Items.APPLE, 0, 1, 3, 15, 0));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_APPLE, 0, 1, 1, 1, 4));
		build.addEntries(ChestBuilder.loot(Items.CAKE, 0, 1, 1, 1, 0));
		build.addEntries(ChestBuilder.loot(Items.NAME_TAG, 0, 1, 1, 5, 1));
		build.addEntries(ChestBuilder.loot(Items.LEAD, 0, 1, 1, 5, 0));
		build.addEntries(ChestBuilder.loot(Items.SADDLE, 0, 1, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.IRON_HORSE_ARMOR, 0, 1, 1, 1, 0));
		build.addEntries(ChestBuilder.loot(Items.FLINT_AND_STEEL, 0, 1, 1, 5, 0));
		build.addEntries(ChestBuilder.loot(Items.IRON_PICKAXE, 0, 1, 1, 5, 1));
		build.addEntries(ChestBuilder.loot(Items.IRON_SWORD, 0, 1, 1, 5, 1));
		build.addEntries(ChestBuilder.loot(Items.IRON_CHESTPLATE, 0, 1, 1, 5, 1));
		build.addEntries(ChestBuilder.loot(Items.IRON_HELMET, 0, 1, 1, 5, 1));
		build.addEntries(ChestBuilder.loot(Items.IRON_LEGGINGS, 0, 1, 1, 5, 1));
		build.addEntries(ChestBuilder.loot(Items.IRON_BOOTS, 0, 1, 1, 5, 1));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 0, 1, 3, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 0, 1, 1, 1, 0));
		build.addEntries(new EnchBookEntry(1));
		PlaceboLootSystem.registerLootTable(SPAWNER, new LootTable(new LootPool[] { build.build() }));

		build = new PoolBuilder(6, 6, 0, 3);
		build.addEntries(ChestBuilder.loot(Blocks.TORCH, 0, 4, 12, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.EXPERIENCE_BOTTLE, 0, 1, 3, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND, 0, 1, 3, 3, 4));
		build.addEntries(ChestBuilder.loot(Items.EMERALD, 0, 1, 3, 3, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_INGOT, 0, 1, 5, 10, 1));
		build.addEntries(ChestBuilder.loot(Items.GOLD_INGOT, 0, 1, 3, 10, 3));
		build.addEntries(ChestBuilder.loot(Items.REDSTONE, 0, 4, 9, 5, 0));
		build.addEntries(ChestBuilder.loot(Items.DYE, 4, 4, 9, 5, 2)); // lapis lazuli
		build.addEntries(ChestBuilder.loot(Items.COAL, 0, 3, 8, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.APPLE, 0, 1, 3, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_APPLE, 0, 1, 1, 1, 0));
		build.addEntries(ChestBuilder.loot(Items.NAME_TAG, 0, 1, 1, 5, 0));
		build.addEntries(ChestBuilder.loot(Items.LEAD, 0, 1, 1, 5, 0));
		build.addEntries(ChestBuilder.loot(Items.SADDLE, 0, 1, 1, 3, 0));
		build.addEntries(ChestBuilder.loot(Items.IRON_HORSE_ARMOR, 0, 1, 1, 1, 0));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_HORSE_ARMOR, 0, 1, 1, 1, 0));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_HORSE_ARMOR, 0, 1, 1, 1, 0));
		build.addEntries(ChestBuilder.loot(Items.FLINT_AND_STEEL, 0, 1, 1, 1, 0));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_PICKAXE, 0, 1, 1, 1, 3));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_SWORD, 0, 1, 1, 1, 3));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_CHESTPLATE, 0, 1, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_HELMET, 0, 1, 1, 1, 3));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_LEGGINGS, 0, 1, 1, 1, 6));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_BOOTS, 0, 1, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 0, 1, 3, 3, 1));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 0, 1, 1, 3, 0));
		build.addEntries(ChestBuilder.loot(Items.ENCHANTED_BOOK, 0, 1, 3, 1, 3));
		PlaceboLootSystem.registerLootTable(SPAWNER_ARMORED, new LootTable(new LootPool[] { build.build() }));

		build = new PoolBuilder(5, 5, 0, 3);
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
		build.addEntries(ChestBuilder.loot(Items.IRON_HORSE_ARMOR, 0, 1, 1, 1, 0));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_HORSE_ARMOR, 0, 1, 1, 1, 0));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_HORSE_ARMOR, 0, 1, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 0, 1, 3, 3, 1));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 0, 1, 1, 3, 0));
		build.addEntries(new EnchBookEntry(3));
		PlaceboLootSystem.registerLootTable(SPAWNER_BRUTAL, new LootTable(new LootPool[] { build.build() }));

		build = new PoolBuilder(5, 5, 0, 3);
		build.addEntries(ChestBuilder.loot(Blocks.TORCH, 0, 4, 12, 10, 0));
		build.addEntries(ChestBuilder.loot(Items.EXPERIENCE_BOTTLE, 0, 2, 4, 10, 1));
		build.addEntries(ChestBuilder.loot(Items.SPAWN_EGG, 50, 1, 3, 1, 1)); // creeper
		build.addEntries(ChestBuilder.loot(Items.SPAWN_EGG, 51, 1, 3, 1, 1)); // skeleton
		build.addEntries(ChestBuilder.loot(Items.SPAWN_EGG, 52, 1, 3, 1, 1)); // spider
		build.addEntries(ChestBuilder.loot(Items.SPAWN_EGG, 54, 1, 3, 1, 1)); // zombie
		build.addEntries(ChestBuilder.loot(Items.SPAWN_EGG, 55, 1, 3, 1, 1)); // slime
		build.addEntries(ChestBuilder.loot(Items.SPAWN_EGG, 58, 1, 3, 1, 1)); // enderman
		build.addEntries(ChestBuilder.loot(Items.SPAWN_EGG, 59, 1, 3, 1, 1)); // cave spider
		build.addEntries(ChestBuilder.loot(Items.SPAWN_EGG, 60, 1, 3, 1, 1)); // silverfish
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
		build.addEntries(ChestBuilder.loot(Items.IRON_HORSE_ARMOR, 0, 1, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_HORSE_ARMOR, 0, 1, 1, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_HORSE_ARMOR, 0, 1, 1, 1, 3));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 0, 1, 3, 3, 0));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 0, 1, 1, 3, 0));
		build.addEntries(new EnchBookEntry(3));
		PlaceboLootSystem.registerLootTable(SPAWNER_SWARM, new LootTable(new LootPool[] { build.build() }));

		build = new PoolBuilder(4, 4, 0, 3);
		build.addEntries(ChestBuilder.loot(Blocks.TORCH, 0, 4, 12, 10));
		build.addEntries(ChestBuilder.loot(Blocks.TRIPWIRE_HOOK, 0, 2, 2, 2));
		build.addEntries(ChestBuilder.loot(Blocks.DAYLIGHT_DETECTOR, 0, 1, 1, 2));
		build.addEntries(ChestBuilder.loot(Blocks.REDSTONE_TORCH, 0, 3, 7, 5));
		build.addEntries(ChestBuilder.loot(Items.REPEATER, 0, 1, 5, 3));
		build.addEntries(ChestBuilder.loot(Items.COMPARATOR, 0, 1, 3, 3));
		build.addEntries(ChestBuilder.loot(Blocks.TNT, 0, 1, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.IRON_INGOT, 0, 1, 5, 5));
		build.addEntries(ChestBuilder.loot(Items.GOLD_INGOT, 0, 1, 3, 5));
		build.addEntries(ChestBuilder.loot(Items.REDSTONE, 0, 4, 9, 10));
		build.addEntries(ChestBuilder.loot(Items.BONE, 0, 4, 6, 10));
		build.addEntries(ChestBuilder.loot(Items.ARROW, 0, 4, 12, 10));
		build.addEntries(ChestBuilder.loot(Items.ROTTEN_FLESH, 0, 3, 7, 10));
		build.addEntries(ChestBuilder.loot(Items.STRING, 0, 4, 6, 10));
		build.addEntries(ChestBuilder.loot(Items.GUNPOWDER, 0, 4, 6, 10));
		build.addEntries(ChestBuilder.loot(Items.ENDER_PEARL, 0, 1, 1, 10));
		build.addEntries(ChestBuilder.loot(Items.BREAD, 0, 1, 3, 15));
		build.addEntries(ChestBuilder.loot(Items.APPLE, 0, 1, 3, 15));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_APPLE, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.CAKE, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.NAME_TAG, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.LEAD, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.SADDLE, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.IRON_HORSE_ARMOR, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.FLINT_AND_STEEL, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_PICKAXE, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_SWORD, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_CHESTPLATE, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_HELMET, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_LEGGINGS, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_BOOTS, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 0, 1, 3, 1));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 0, 1, 1, 1));
		build.addEntries(new EnchBookEntry(3));
		PlaceboLootSystem.registerLootTable(SPAWNER_TRAP, new LootTable(new LootPool[] { build.build() }));

		build = new PoolBuilder(10, 10, 5, 10);
		build.addEntries(ChestBuilder.loot(Blocks.TORCH, 0, 4, 12, 10));
		build.addEntries(ChestBuilder.loot(Items.EXPERIENCE_BOTTLE, 0, 1, 3, 10));
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8225, 1, 1, 1)); // regeneration II
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8226, 1, 1, 1)); // swiftness II
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8259, 1, 1, 1)); // fire resistance (ext)
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8229, 1, 1, 1)); // healing II
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8262, 1, 1, 1)); // night vision (ext)
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8265, 1, 1, 1)); // strength (ext)
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8270, 1, 1, 1)); // invisibility (ext)
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8269, 1, 1, 1)); // water breathing (ext)
		build.addEntries(ChestBuilder.loot(Items.DIAMOND, 0, 1, 3, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_INGOT, 0, 1, 5, 10));
		build.addEntries(ChestBuilder.loot(Items.GOLD_INGOT, 0, 1, 3, 10));
		build.addEntries(ChestBuilder.loot(Items.COAL, 0, 3, 8, 10));
		build.addEntries(ChestBuilder.loot(Items.APPLE, 0, 1, 3, 15));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_APPLE, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.NAME_TAG, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.LEAD, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.SADDLE, 0, 1, 1, 3));
		build.addEntries(ChestBuilder.loot(Items.FLINT_AND_STEEL, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.IRON_HORSE_ARMOR, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_HORSE_ARMOR, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_HORSE_ARMOR, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.IRON_PICKAXE, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_SWORD, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_CHESTPLATE, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_HELMET, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_LEGGINGS, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_BOOTS, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 0, 1, 3, 3));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 0, 1, 1, 3));
		build.addEntries(new EnchBookEntry(4));
		PlaceboLootSystem.registerLootTable(CHEST_VALUABLE, new LootTable(new LootPool[] { build.build() }));

		build = new PoolBuilder(2, 4, 0, 3);
		build.addEntries(ChestBuilder.loot(Blocks.TORCH, 0, 4, 12, 10));
		build.addEntries(ChestBuilder.loot(Blocks.COBBLESTONE, 0, 8, 24, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_INGOT, 0, 1, 3, 3));
		build.addEntries(ChestBuilder.loot(Items.GOLD_NUGGET, 0, 4, 9, 3));
		build.addEntries(ChestBuilder.loot(Items.COAL, 0, 3, 8, 5));
		build.addEntries(ChestBuilder.loot(Items.BONE, 0, 4, 6, 15));
		build.addEntries(ChestBuilder.loot(Items.ROTTEN_FLESH, 0, 3, 7, 15));
		build.addEntries(ChestBuilder.loot(Items.ARROW, 0, 4, 12, 10));
		build.addEntries(ChestBuilder.loot(Items.BREAD, 0, 1, 3, 10));
		build.addEntries(ChestBuilder.loot(Items.FLINT_AND_STEEL, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.WOODEN_PICKAXE, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.WOODEN_SWORD, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.LEATHER_CHESTPLATE, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.LEATHER_HELMET, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.LEATHER_LEGGINGS, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.LEATHER_BOOTS, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 0, 1, 3, 1));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 0, 1, 1, 1));
		build.addEntries(new EnchBookEntry(1));
		PlaceboLootSystem.registerLootTable(CHEST_ROGUE[0], new LootTable(new LootPool[] { build.build() }));

		build = new PoolBuilder(2, 4, 0, 3);
		build.addEntries(ChestBuilder.loot(Blocks.TORCH, 0, 4, 12, 10));
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8193, 1, 1, 1)); // regeneration
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8194, 1, 1, 1)); // swiftness
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8195, 1, 1, 1)); // fire resistance
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8197, 1, 1, 1)); // healing
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8198, 1, 1, 1)); // night vision
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8201, 1, 1, 1)); // strength
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8206, 1, 1, 1)); // invisibility
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8237, 1, 1, 1)); // water breathing
		build.addEntries(ChestBuilder.loot(Blocks.COBBLESTONE, 0, 8, 24, 3));
		build.addEntries(ChestBuilder.loot(Items.IRON_INGOT, 0, 1, 5, 5));
		build.addEntries(ChestBuilder.loot(Items.GOLD_INGOT, 0, 1, 3, 5));
		build.addEntries(ChestBuilder.loot(Items.GOLD_NUGGET, 0, 4, 9, 5));
		build.addEntries(ChestBuilder.loot(Items.REDSTONE, 0, 4, 9, 5));
		build.addEntries(ChestBuilder.loot(Items.COAL, 0, 3, 8, 10));
		build.addEntries(ChestBuilder.loot(Items.BONE, 0, 4, 6, 10));
		build.addEntries(ChestBuilder.loot(Items.ROTTEN_FLESH, 0, 3, 7, 10));
		build.addEntries(ChestBuilder.loot(Items.ARROW, 0, 4, 12, 10));
		build.addEntries(ChestBuilder.loot(Items.BREAD, 0, 1, 3, 15));
		build.addEntries(ChestBuilder.loot(Items.APPLE, 0, 1, 3, 15));
		build.addEntries(ChestBuilder.loot(Items.LEAD, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.SADDLE, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.IRON_HORSE_ARMOR, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.FLINT_AND_STEEL, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_PICKAXE, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_SWORD, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_CHESTPLATE, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_HELMET, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_LEGGINGS, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_BOOTS, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.PAINTING, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 0, 1, 3, 1));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 0, 1, 1, 1));
		build.addEntries(new EnchBookEntry(1));
		PlaceboLootSystem.registerLootTable(CHEST_ROGUE[1], new LootTable(new LootPool[] { build.build() }));

		build = new PoolBuilder(3, 5, 0, 3);
		build.addEntries(ChestBuilder.loot(Blocks.TORCH, 0, 4, 12, 10));
		build.addEntries(ChestBuilder.loot(Blocks.COBBLESTONE, 0, 8, 24, 1));
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8193, 1, 1, 1)); // regeneration
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8194, 1, 1, 1)); // swiftness
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8195, 1, 1, 1)); // fire resistance
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8197, 1, 1, 1)); // healing
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8198, 1, 1, 1)); // night vision
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8201, 1, 1, 1)); // strength
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8206, 1, 1, 1)); // invisibility
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8237, 1, 1, 1)); // water breathing
		build.addEntries(ChestBuilder.loot(Items.IRON_INGOT, 0, 1, 5, 10));
		build.addEntries(ChestBuilder.loot(Items.GOLD_INGOT, 0, 1, 3, 5));
		build.addEntries(ChestBuilder.loot(Items.REDSTONE, 0, 4, 9, 5));
		build.addEntries(ChestBuilder.loot(Items.COAL, 0, 3, 8, 10));
		build.addEntries(ChestBuilder.loot(Items.ARROW, 0, 4, 12, 10));
		build.addEntries(ChestBuilder.loot(Items.BREAD, 0, 1, 3, 15));
		build.addEntries(ChestBuilder.loot(Items.APPLE, 0, 1, 3, 15));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_APPLE, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.NAME_TAG, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.LEAD, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.SADDLE, 0, 1, 1, 3));
		build.addEntries(ChestBuilder.loot(Items.IRON_HORSE_ARMOR, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_HORSE_ARMOR, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.FLINT_AND_STEEL, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.STONE_PICKAXE, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.STONE_SWORD, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.CHAINMAIL_CHESTPLATE, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.CHAINMAIL_HELMET, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.CHAINMAIL_LEGGINGS, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.CHAINMAIL_BOOTS, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.PAINTING, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 0, 1, 3, 3));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 0, 1, 1, 3));
		build.addEntries(new EnchBookEntry(1));
		PlaceboLootSystem.registerLootTable(CHEST_ROGUE[2], new LootTable(new LootPool[] { build.build() }));

		build = new PoolBuilder(4, 6, 0, 3);
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8193, 1, 1, 1)); // regeneration
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8194, 1, 1, 1)); // swiftness
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8195, 1, 1, 1)); // fire resistance
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8197, 1, 1, 1)); // healing
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8198, 1, 1, 1)); // night vision
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8201, 1, 1, 1)); // strength
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8206, 1, 1, 1)); // invisibility
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8237, 1, 1, 1)); // water breathing
		build.addEntries(ChestBuilder.loot(Items.DIAMOND, 0, 1, 3, 3));
		build.addEntries(ChestBuilder.loot(Items.IRON_INGOT, 0, 1, 5, 10));
		build.addEntries(ChestBuilder.loot(Items.GOLD_INGOT, 0, 1, 3, 10));
		build.addEntries(ChestBuilder.loot(Items.REDSTONE, 0, 4, 9, 5));
		build.addEntries(ChestBuilder.loot(Items.COAL, 0, 3, 8, 10));
		build.addEntries(ChestBuilder.loot(Items.ARROW, 0, 4, 12, 10));
		build.addEntries(ChestBuilder.loot(Items.CARROT, 0, 1, 3, 5));
		build.addEntries(ChestBuilder.loot(Items.POTATO, 0, 1, 3, 5));
		build.addEntries(ChestBuilder.loot(Items.BREAD, 0, 1, 3, 15));
		build.addEntries(ChestBuilder.loot(Items.APPLE, 0, 1, 3, 15));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_APPLE, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.NAME_TAG, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.LEAD, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.SADDLE, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_HORSE_ARMOR, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_HORSE_ARMOR, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_HORSE_ARMOR, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.FLINT_AND_STEEL, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_PICKAXE, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_SWORD, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_CHESTPLATE, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_HELMET, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_LEGGINGS, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.IRON_BOOTS, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.PAINTING, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 0, 1, 3, 3));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 0, 1, 1, 5));
		build.addEntries(new EnchBookEntry(1));
		PlaceboLootSystem.registerLootTable(CHEST_ROGUE[3], new LootTable(new LootPool[] { build.build() }));

		build = new PoolBuilder(2, 4, 0, 3);
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8193, 1, 1, 1)); // regeneration
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8194, 1, 1, 1)); // swiftness
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8195, 1, 1, 1)); // fire resistance
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8197, 1, 1, 1)); // healing
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8198, 1, 1, 1)); // night vision
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8201, 1, 1, 1)); // strength
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8206, 1, 1, 1)); // invisibility
		build.addEntries(ChestBuilder.loot(Items.POTIONITEM, 8237, 1, 1, 1)); // water breathing
		build.addEntries(ChestBuilder.loot(Items.IRON_INGOT, 0, 1, 5, 10));
		build.addEntries(ChestBuilder.loot(Items.GOLD_INGOT, 0, 1, 5, 10));
		build.addEntries(ChestBuilder.loot(Items.BONE, 0, 4, 6, 10));
		build.addEntries(ChestBuilder.loot(Items.ROTTEN_FLESH, 0, 3, 7, 10));
		build.addEntries(ChestBuilder.loot(Items.ENDER_PEARL, 0, 1, 1, 10));
		build.addEntries(ChestBuilder.loot(Items.BREAD, 0, 1, 3, 15));
		build.addEntries(ChestBuilder.loot(Items.APPLE, 0, 1, 3, 15));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_APPLE, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.CAKE, 0, 1, 1, 3));
		build.addEntries(ChestBuilder.loot(Items.NAME_TAG, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.LEAD, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.SADDLE, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.FLINT_AND_STEEL, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_PICKAXE, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_SWORD, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_CHESTPLATE, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_HELMET, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_LEGGINGS, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_BOOTS, 0, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.PAINTING, 0, 1, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 0, 1, 3, 3));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 0, 1, 1, 1));
		build.addEntries(new EnchBookEntry(1));
		PlaceboLootSystem.registerLootTable(CHEST_ROGUE[4], new LootTable(new LootPool[] { build.build() }));
	}

	public static LootEntry loot(Item item, int damage, int min, int max, int weight, int quality) {
		return loot(new ItemStack(item, 1, damage), min, max, weight, quality);
	}

	public static LootEntry loot(Block block, int damage, int min, int max, int weight, int quality) {
		return loot(new ItemStack(block, 1, damage), min, max, weight, quality);
	}

	public static LootEntry loot(ItemStack item, int min, int max, int weight, int quality) {
		return new PlaceboLootEntry(item, min, max, weight, quality);
	}

	@Deprecated
	public static LootEntry loot(Item item, int damage, int min, int max, int weight) {
		return loot(new ItemStack(item, 1, damage), min, max, weight, 0);
	}

	@Deprecated
	public static LootEntry loot(Block block, int damage, int min, int max, int weight) {
		return loot(new ItemStack(block, 1, damage), min, max, weight, 0);
	}

	@Deprecated
	public static LootEntry loot(ItemStack item, int min, int max, int weight) {
		return loot(item, min, max, weight, 0);
	}

	public static ResourceLocation getRogueChestByHeight(int y) {
		if (y < 20) return ChestBuilder.CHEST_ROGUE[4];
		else if (y < 28) return ChestBuilder.CHEST_ROGUE[3];
		else if (y < 38) return ChestBuilder.CHEST_ROGUE[2];
		else if (y < 48) return ChestBuilder.CHEST_ROGUE[1];
		else return ChestBuilder.CHEST_ROGUE[0];
	}

	public static void place(World world, Random random, BlockPos pos, ResourceLocation loot) {
		world.setBlockState(pos, Blocks.CHEST.getDefaultState(), 2);
		ChestBuilder chest = new ChestBuilder(world, random, pos);
		if (chest.isValid) {
			chest.fill(loot);
		}
	}

	public static void placeTrapped(World world, Random random, BlockPos pos, ResourceLocation loot) {
		world.setBlockState(pos, Blocks.TRAPPED_CHEST.getDefaultState(), 2);
		ChestBuilder chest = new ChestBuilder(world, random, pos);
		if (chest.isValid) {
			chest.fill(loot);
		}
	}

	public static class EnchBookEntry extends PlaceboLootEntry {

		final EnchantRandomly func = new EnchantRandomly(new LootCondition[0], null);

		public EnchBookEntry(int weight) {
			super(Items.BOOK, 1, 1, weight, 5);
		}

		@Override
		public void addLoot(Collection<ItemStack> stacks, Random rand, LootContext context) {
			ItemStack s = new ItemStack(Items.BOOK);
			func.apply(s, rand, context);
			stacks.add(s);
		}

	}
}