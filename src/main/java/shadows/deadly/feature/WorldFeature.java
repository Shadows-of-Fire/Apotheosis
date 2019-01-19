package shadows.deadly.feature;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Base class for all worldgen features.
 * @author Shadows	
 */
public abstract class WorldFeature {

	/**
	 * Generates this feature.  Run every chunk.  Pos always has y == 0.
	 */
	public abstract void generate(World world, BlockPos pos);

	/**
	 * Checks if this features can generate.
	 * @return If {@link WorldFeature#place} can be run here.
	 */
	public abstract boolean canBePlaced(World world, BlockPos pos);

	/**
	 * Actually place this feature.
	 */
	public abstract void place(World world, BlockPos pos);

	/**
	 * @return If this feature is enabled.
	 */
	public abstract boolean isEnabled();

}