package shadows.apotheosis.deadly.gen;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Base class for all worldgen features.
 * @author Shadows
 */
public abstract class WeightedGenerator {

	/**
	 * Generates this feature.
	 */
	public abstract boolean generate(ServerLevelAccessor world, int x, int z, Random rand);

	/**
	 * Checks if this features can generate.
	 * @return If {@link WeightedGenerator#place} can be run here.
	 */
	public abstract boolean canBePlaced(ServerLevelAccessor world, BlockPos pos, Random rand);

	/**
	 * Actually place this feature.
	 */
	public abstract void place(ServerLevelAccessor world, BlockPos pos, Random rand);

	/**
	 * @return If this feature is enabled.
	 */
	public abstract boolean isEnabled();

	public static abstract class WorldFeatureItem extends WeightedEntry.IntrusiveBase {

		public WorldFeatureItem(int weight) {
			super(weight);
		}

		public abstract void place(ServerLevelAccessor world, BlockPos pos, Random rand);
	}

}