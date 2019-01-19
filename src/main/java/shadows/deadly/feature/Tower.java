package shadows.deadly.feature;
/*
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import shadows.deadly.DeadlyWorld;
import shadows.deadly.util.ChestBuilder;

public class Tower extends WorldFeature {

	/// This feature's items.
	public static final WorldFeatureItem[] items = { SpawnerItem.buildSpawner("arrow", 0), SpawnerItem.buildSpawner("arrow", 1) };
	/// The weights for each nest type.
	public static final int[] weights = WorldGenerator.getWeights(Properties.TOWERS, DeadlyWorld.TOWERS);
	/// The total weight for nest types.
	public static final int totalWeight = WorldGenerator.getTotalWeight(Tower.weights);

	/// The chance for this to appear in any given chunk. Determined by the properties file.
	public final double frequency;

	public Tower(double freq) {
		this.frequency = freq;
	}

	/// Attempts to generate this feature in the given chunk. Block position is given.
	@Override
	public void generate(World world, BlockPos pos) {
		if (this.frequency <= world.rand.nextDouble()) return;
		int x = pos.getX() + world.rand.nextInt(16);
		int z = pos.getZ() + world.rand.nextInt(16);
		int y = 62;
		ArrayList<Integer> yValues = new ArrayList<Integer>(15);
		MutableBlockPos mPos = new MutableBlockPos(x, y, z);
		for (byte state = 3; y > 5; y--) {
			if (!world.isAirBlock(mPos.setPos(x, y, z))) {
				if (state <= 0) {
					if (this.canBePlaced(world, mPos.up(2))) {
						yValues.add(y + 2);
					}
				}
				state = 3;
			} else {
				state--;
			}
		}
		if (yValues.size() > 0) {
			this.place(world, random, x, yValues.get(random.nextInt(yValues.size())).intValue(), z);
		}
	}

	@Override
	public boolean canBePlaced(World world, BlockPos pos) {
		for (int x1 = -1; x1 < 2; x1++) {
			for (int z1 = -1; z1 < 2; z1++) {
				if (!world.isAirBlock(pos.add(x1, 0, z1))) return false;
			}
		}
		return world.isAirBlock(pos.down());
	}

	@Override
	public void place(World world, BlockPos pos) {
		Tower.placeTower(world, pos);
		for (y -= 2; y > 4 && !world.isBlockNormalCubeDefault(x, y, z, true); y--) {
			if (random.nextInt(4) == 0) {
				world.setBlock(x, y, z, Blocks.cobblestone, 0, 2);
			} else {
				world.setBlock(x, y, z, Blocks.mossy_cobblestone, 0, 2);
			}
			world.markBlockForUpdate(x, y, z);
		}
	}

	/// Places a tower at the given location.
	public static void placeTower(World world, BlockPos pos) {
		Random random = world.rand;
		String type = WorldGenerator.choose(random, Tower.totalWeight, DeadlyWorld.TOWERS, Tower.weights);
		boolean onFire = type.endsWith("fire");
		if (type.startsWith("spawner")) {
			WorldGenerator.choose(random, RogueSpawner.totalWeight, RogueSpawner.items).place(world, random, x, y - 1, z);
		} else if (type.startsWith("double")) {
			Tower.items[onFire].place(world, random, x, y - 1, z);
		} else if (type.startsWith("chest")) {
			ChestBuilder.place(world, random, x, y - 1, z, ChestBuilder.TOWER);
		} else if (random.nextInt(4) == 0) {
			world.setBlock(x, y - 1, z, Blocks.cobblestone, 0, 2);
		} else {
			world.setBlock(x, y - 1, z, Blocks.mossy_cobblestone, 0, 2);
		}
		Tower.items[onFire].place(world, random, x, y, z);
	}
}*/