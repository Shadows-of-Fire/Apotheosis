package shadows.apotheosis.ench.objects;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.extensions.IForgeBlock;

/**
 * This is the main interface for all blocks that provide stats to an enchanting table.
 * The stats allocated to an enchanting table are as follows:
 * Enchanting Power: This is the primary stat, which controls the level at which the table enchants at.
 * Flux: This stat controls how volatile the real level is.  With the base flux, the table ranges from -15% to +30% of the original.
 * Enchantment Count: This controls the number of enchantments that can be placed on an item.
 *
 */
public interface IEnchantingBlock extends IForgeBlock {

	/**
	 * Determines the amount of enchanting power (Eterna) this block can provide to an enchanting table.
	 * @param world The World
	 * @param pos Block position in world
	 * @return The amount of enchanting power this block produces.
	 */
	@Override
	float getEnchantPowerBonus(BlockState state, IWorldReader world, BlockPos pos);

	/**
	 * Determines the maximum enchanting power (Eterna) that this block may contribute up to.
	 * @param world The World
	 * @param pos Block position in world
	 * @return The max Eterna this block may contribute towards.  Once past this value, this block has no effect.
	 */
	default float getMaxEnchantingPower(BlockState state, IWorldReader world, BlockPos pos) {
		return 15F;
	}

	/**
	 * Determines the amount of Quanta this block provides to an enchanting table.
	 * @param world The World
	 * @param pos Block position in world
	 * @return The change in Quanta caused by this block.
	 */
	default float getQuantaBonus(BlockState state, IWorldReader world, BlockPos pos) {
		return 0;
	}

	/**
	 * Determines the amount of Arcana this block provides to an enchanting table.
	 * @param world The World
	 * @param pos Block position in world
	 * @return The change in Arcana caused by this block.
	 */
	default float getArcanaBonus(BlockState state, IWorldReader world, BlockPos pos) {
		return 0;
	}

}