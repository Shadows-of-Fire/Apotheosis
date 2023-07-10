package shadows.apotheosis.ench.objects;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
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
	 * @return The amount of enchanting power this block produces.
	 */
	@Override
	default float getEnchantPowerBonus(BlockState state, LevelReader world, BlockPos pos) {
		return IForgeBlock.super.getEnchantPowerBonus(state, world, pos);
	}

	/**
	 * Determines the maximum enchanting power (Eterna) that this block may contribute up to.
	 * @return The max Eterna this block may contribute towards.  Once past this value, this block has no effect.
	 */
	default float getMaxEnchantingPower(BlockState state, LevelReader world, BlockPos pos) {
		return 15F;
	}

	/**
	 * Determines the amount of Quanta this block provides to an enchanting table.
	 * @return The change in Quanta caused by this block.
	 */
	default float getQuantaBonus(BlockState state, LevelReader world, BlockPos pos) {
		return 0;
	}

	/**
	 * Determines the amount of Arcana this block provides to an enchanting table.
	 * @return The change in Arcana caused by this block.
	 */
	default float getArcanaBonus(BlockState state, LevelReader world, BlockPos pos) {
		return 0;
	}

	/**
	 * Determines how much "Quanta Rectification" this block provides.
	 * 1F of Rectification reduces the negative threshold by 1%
	 * [-Q, +Q] -> [-(1-QR/100F)Q, +Q]
	 * At 100%, quanta only has a positive effect.
	 * @return The Quanta Rectification bonus from this block.
	 */
	default float getQuantaRectification(BlockState state, LevelReader world, BlockPos pos) {
		return 0;
	}

	/**
	 * Determines how many extra clues can be viewed by the client when this block is present.
	 * @return The number of bonus clues to show.
	 */
	default int getBonusClues(BlockState state, LevelReader world, BlockPos pos) {
		return 0;
	}

}