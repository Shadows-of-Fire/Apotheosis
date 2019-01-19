package shadows.deadly.feature;
/*
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public class VeinLiquid extends VeinFeature {
	public VeinLiquid(double count, Block block, int size, int min, int max) {
		super(count, block, size, min, max);
	}

	/// Returns true if this feature can be placed at the location.
	@Override
	public boolean canBePlaced(World world, Random random, int x, int y, int z) {
		return this.isSolidOrFeature(world, x, y - 1, z) && this.isSolidOrFeature(world, x, y + 1, z) && this.isSolidOrFeature(world, x - 1, y, z) && this.isSolidOrFeature(world, x + 1, y, z) && this.isSolidOrFeature(world, x, y, z - 1) && this.isSolidOrFeature(world, x, y, z + 1);
	}

	/// Returns true if the block is solid or this feature.
	public boolean isSolidOrFeature(World world, int x, int y, int z) {
		return world.isBlockNormalCubeDefault(x, y, z, false) || world.getBlock(x, y, z) == this.block;
	}
}*/