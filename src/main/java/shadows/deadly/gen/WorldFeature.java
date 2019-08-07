package shadows.deadly.gen;

import java.util.Random;

import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

/**
 * Base class for all worldgen features.
 * @author Shadows
 */
public abstract class WorldFeature {

	/**
	 * Generates this feature.
	 */
	public abstract boolean generate(IWorld world, int x, int z, Random rand);

	/**
	 * Checks if this features can generate.
	 * @return If {@link WorldFeature#place} can be run here.
	 */
	public abstract boolean canBePlaced(IWorld world, BlockPos pos, Random rand);

	/**
	 * Actually place this feature.
	 */
	public abstract void place(IWorld world, BlockPos pos, Random rand);

	/**
	 * @return If this feature is enabled.
	 */
	public abstract boolean isEnabled();

	public static abstract class WorldFeatureItem extends WeightedRandom.Item {

		public WorldFeatureItem(int weight) {
			super(weight);
		}

		public abstract void place(IWorld world, BlockPos pos);
	}

}