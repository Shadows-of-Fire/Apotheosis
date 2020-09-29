package shadows.apotheosis.deadly.gen;

import java.util.Random;

import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;

/**
 * Base class for all worldgen features.
 * @author Shadows
 */
public abstract class WeightedGenerator {

	/**
	 * Generates this feature.
	 */
	public abstract boolean generate(IServerWorld world, int x, int z, Random rand);

	/**
	 * Checks if this features can generate.
	 * @return If {@link WeightedGenerator#place} can be run here.
	 */
	public abstract boolean canBePlaced(IServerWorld world, BlockPos pos, Random rand);

	/**
	 * Actually place this feature.
	 */
	public abstract void place(IServerWorld world, BlockPos pos, Random rand);

	/**
	 * @return If this feature is enabled.
	 */
	public abstract boolean isEnabled();

	public static abstract class WorldFeatureItem extends WeightedRandom.Item {

		public WorldFeatureItem(int weight) {
			super(weight);
		}

		public abstract void place(IServerWorld world, BlockPos pos, Random rand);
	}

}