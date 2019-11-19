package shadows.util;

import java.util.Collection;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.EnchantRandomly;
import shadows.deadly.loot.LootManager;
import shadows.deadly.loot.LootRarity;
import shadows.placebo.loot.PlaceboLootEntry;

/**
 * Utils for loot chests. Uses the Placebo loot system.
 * @author Shadows
 *
 */
public class ChestBuilder {

	protected Random random;
	protected TileEntityChest chest;
	protected boolean isValid;

	public ChestBuilder(World world, Random rand, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileEntityChest) {
			random = rand;
			chest = (TileEntityChest) tileEntity;
			isValid = true;
		}
	}

	public ChestBuilder(TileEntityChest tileEntity, Random rand) {
		chest = tileEntity;
		if (chest != null) {
			random = rand;
			isValid = true;
		}
	}

	public void fill(ResourceLocation loot) {
		chest.setLootTable(loot, random.nextLong());
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

	public static class EnchantedEntry extends PlaceboLootEntry {

		final EnchantRandomly func = new EnchantRandomly(new LootCondition[0], null);
		private Item i;

		public EnchantedEntry(Item i, int weight) {
			super(i, 1, 1, weight, 5);
			this.i = i;
		}

		@Override
		public void addLoot(Collection<ItemStack> stacks, Random rand, LootContext context) {
			ItemStack s = new ItemStack(i);
			func.apply(s, rand, context);
			stacks.add(s);
		}

	}

	public static class AffixEntry extends PlaceboLootEntry {

		public AffixEntry(int weight, int quality) {
			super(ItemStack.EMPTY, 1, 1, weight, quality);
		}

		@Override
		public void addLoot(Collection<ItemStack> stacks, Random rand, LootContext context) {
			LootRarity rarity = LootRarity.random(rand);
			ItemStack stack = LootManager.getRandomEntry(rand, rarity);
			stacks.add(LootManager.genLootItem(stack, rand, rarity));
		}
	}
}