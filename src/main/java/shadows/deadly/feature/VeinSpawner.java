package shadows.deadly.feature;
/*
import java.util.List;
import java.util.Properties;
import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import shadows.deadly.util.ChestBuilder;

public class VeinSpawner implements WorldFeature {
	/// This feature's items.
	public static final List<WorldFeatureItem> items = SpawnerItem.buildItems("spawner_vein");
	/// The total weight for this feature's items.
	public static final int totalWeight = WorldGenerator.getTotalWeight(VeinSpawner.items);
	/// The chance for a spawner to be armored.
	public static final double armorChance = Properties.getDouble(Properties.SPAWNER_VEINS, "_armor_chance");
	/// The chance for a spawner to have a chest.
	public static final double chestChance = Properties.getDouble(Properties.SPAWNER_VEINS, "_chest_chance");

	/// The number of veins to be generated. Determined by the properties file.
	public final double veinCount;

	public VeinSpawner(double count) {
		this.veinCount = count;
	}

	/// Attempts to generate this feature in the given chunk. Block position is given.
	@Override
	public void generate(World world, Random random, int x, int z) {
		int chunkX = x;
		int chunkZ = z;
		int y;
		for (double count = this.veinCount; count >= 1.0 || count > 0.0 && count > random.nextDouble(); count--) {
			x = chunkX + random.nextInt(16);
			y = random.nextInt(44) + 5;
			z = chunkZ + random.nextInt(16);
			if (this.canBePlaced(world, random, x, y, z)) {
				this.place(world, random, x, y, z);
			}
		}
	}

	/// Returns true if this feature can be placed at the location.
	@Override
	public boolean canBePlaced(World world, Random random, int x, int y, int z) {
		return world.isBlockNormalCubeDefault(x, y - 1, z, false) && world.isBlockNormalCubeDefault(x, y + 1, z, false) && world.isBlockNormalCubeDefault(x - 1, y, z, false) && world.isBlockNormalCubeDefault(x + 1, y, z, false) && world.isBlockNormalCubeDefault(x, y, z - 1, false) && world.isBlockNormalCubeDefault(x, y, z + 1, false);
	}

	/// Places this feature at the location.
	@Override
	public void place(World world, Random random, int x, int y, int z) {
		if (random.nextDouble() < VeinSpawner.armorChance) {
			int[][] positions = {};
			if (random.nextDouble() < VeinSpawner.chestChance) {
				positions = new int[][] { { 0, 1, 0 }, { -1, 0, 0 }, { 1, 0, 0 }, { 0, 0, -1 }, { 0, 0, 1 }, { -1, -1, 0 }, { 1, -1, 0 }, { 0, -1, -1 }, { 0, -1, 1 }, { 0, -2, 0 } };
				ChestBuilder.place(world, random, x, y - 1, z, ChestBuilder.SPAWNER_ARMORED);
			} else {
				positions = new int[][] { { 0, 1, 0 }, { -1, 0, 0 }, { 1, 0, 0 }, { 0, 0, -1 }, { 0, 0, 1 }, { 0, -1, 0 } };
			}
			for (int[] pos : positions) {
				world.setBlock(x + pos[0], y + pos[1], z + pos[2], Blocks.obsidian, 0, 2);
			}
		} else if (random.nextDouble() < VeinSpawner.chestChance) {
			ChestBuilder.place(world, random, x, y - 1, z, ChestBuilder.SPAWNER);
		}
		WorldGenerator.choose(random, VeinSpawner.totalWeight, VeinSpawner.items).place(world, random, x, y, z);
	}
}*/