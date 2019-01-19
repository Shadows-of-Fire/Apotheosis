package shadows.deadly.feature;
/*
import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import shadows.deadly.util.ChestBuilder;

public class SpawnerTrap implements WorldFeature {
	/// This feature's items.
	public static final WorldFeatureItem[] items = SpawnerItem.buildItems("spawner_trap");
	/// The total weight for this feature's items.
	public static final int totalWeight = WorldGenerator.getTotalWeight(SpawnerTrap.items);
	/// The chance for a spawner to have a chest.
	public static final double chestChance = Properties.getDouble(Properties.SPAWNER_TRAPS, "_chest_chance");

	/// The chance for this to appear in any given chunk. Determined by the properties file.
	public final double frequency;

	public SpawnerTrap(double freq) {
		this.frequency = freq;
	}

	/// Attempts to generate this feature in the given chunk. Block position is given.
	@Override
	public void generate(World world, Random random, int x, int z) {
		if (this.frequency <= random.nextDouble()) return;
		x += random.nextInt(16);
		z += random.nextInt(16);
		int y = random.nextInt(50) + 11;
		for (byte state = 0; y > 5; y--) {
			if (world.isBlockNormalCubeDefault(x, y, z, true)) {
				if (state == 0) {
					if (this.canBePlaced(world, random, x, y, z)) {
						this.place(world, random, x, y, z);
						return;
					}
					state = -1;
				}
			} else {
				state = 0;
			}
		}
	}

	/// Returns true if this feature can be placed at the location.
	@Override
	public boolean canBePlaced(World world, Random random, int x, int y, int z) {
		if (world.isAirBlock(x - 1, y, z) || world.isAirBlock(x + 1, y, z) || world.isAirBlock(x, y, z - 1) || world.isAirBlock(x, y, z + 1)) {
			y--;
		}
		if (!world.isBlockNormalCubeDefault(x - 1, y, z, false) || !world.isBlockNormalCubeDefault(x + 1, y, z, false) || !world.isBlockNormalCubeDefault(x, y, z - 1, false) || !world.isBlockNormalCubeDefault(x, y, z + 1, false)) return false;
		return (world.isAirBlock(x, y + 1, z) || world.isBlockNormalCubeDefault(x, y + 1, z, false)) && world.isAirBlock(x, y + 2, z) && world.isBlockNormalCubeDefault(x, y - 1, z, false);
	}

	/// Places this feature at the location.
	@Override
	public void place(World world, Random random, int x, int y, int z) {
		if (world.isAirBlock(x - 1, y, z) || world.isAirBlock(x + 1, y, z) || world.isAirBlock(x, y, z - 1) || world.isAirBlock(x, y, z + 1)) {
			world.setBlock(x, y, z, Blocks.air, 0, 2);
			y--;
		}
		WorldGenerator.choose(random, SpawnerTrap.totalWeight, SpawnerTrap.items).place(world, random, x, y, z);
		if (random.nextDouble() < SpawnerTrap.chestChance && world.isBlockNormalCubeDefault(x, y - 2, z, false)) {
			ChestBuilder.place(world, random, x, y - 1, z, ChestBuilder.SPAWNER_TRAP);
		}
		Mine.coverTrap(world, random, x, y, z);
	}
}*/