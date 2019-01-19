package shadows.deadly.feature;

import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * This class is functional.
 * @author Shadows
 */
public abstract class WorldFeatureItem extends WeightedRandom.Item {
	
	public WorldFeatureItem(int weight) {
		super(weight);
	}

	/**
	 * Generates this feature.
	 */
	public abstract void place(World world, BlockPos pos);

	public int getWeight() {
		return itemWeight;
	}
}