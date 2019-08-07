package shadows.util;

import java.util.Random;
import java.util.function.Consumer;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.functions.EnchantRandomly;
import net.minecraft.world.storage.loot.functions.ILootFunction;
import shadows.placebo.loot.StackLootEntry;

/**
 * Utils for loot chests. Uses the Placebo loot system.
 * @author Shadows
 *
 */
public class ChestBuilder {

	protected Random random;
	protected ChestTileEntity chest;
	protected boolean isValid;

	public ChestBuilder(World world, Random rand, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof ChestTileEntity) {
			random = rand;
			chest = (ChestTileEntity) tileEntity;
			isValid = true;
		}
	}

	public ChestBuilder(ChestTileEntity tileEntity, Random rand) {
		chest = tileEntity;
		if (chest != null) {
			random = rand;
			isValid = true;
		}
	}

	public void fill(ResourceLocation loot) {
		chest.setLootTable(loot, random.nextLong());
	}

	public static LootEntry loot(Item item, int min, int max, int weight, int quality) {
		return loot(new ItemStack(item), min, max, weight, quality);
	}

	public static LootEntry loot(Block block, int min, int max, int weight, int quality) {
		return loot(new ItemStack(block), min, max, weight, quality);
	}

	public static LootEntry loot(ItemStack item, int min, int max, int weight, int quality) {
		return new StackLootEntry(item, min, max, weight, quality);
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

	public static class EnchantedEntry extends StackLootEntry {

		final ILootFunction func = EnchantRandomly.func_215900_c().build();
		private Item i;

		public EnchantedEntry(Item i, int weight) {
			super(i, 1, 1, weight, 5);
			this.i = i;
		}

		@Override
		protected void func_216154_a(Consumer<ItemStack> list, LootContext ctx) {
			ItemStack s = new ItemStack(i);
			func.apply(s, ctx);
			list.accept(s);
		}

	}
}