package shadows.apotheosis.ench.objects;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.extensions.IForgeBlock;

/**
 * This is the main interface for all blocks that provide stats to an enchanting table.
 * The stats allocated to an enchanting table are as follows:
 * Enchanting Power: This is the primary stat, which controls the level at which the table enchants at.
 * Rarity Offset: This stat controls the weights of certain rarities, shifting towards more common (negative) or more rare (positive).
 * Enchantment Count: This controls the number of enchantments that can be placed on an item.
 *
 */
public interface IEnchantingBlock extends IForgeBlock {

	/**
	 * Determines the amount of enchanting power this block can provide to an enchanting table.
	 * @param world The World
	 * @param pos Block position in world
	 * @return The amount of enchanting power this block produces.
	 */
	@Override
	float getEnchantPowerBonus(BlockState state, IWorldReader world, BlockPos pos);

	/**
	 * Determines the amount of rarity offset this block provides to an enchanting table.
	 * @param world The World
	 * @param pos Block position in world
	 * @return The rarity offset this block produces.
	 */
	float getRarityOffset(BlockState state, IWorldReader world, BlockPos pos);

	/**
	 * Determines the amount of rarity offset this block provides to an enchanting table.
	 * @param world The World
	 * @param pos Block position in world
	 * @return The amount of enchanting power this block produces.
	 */
	float getEnchantmentCount(BlockState state, IWorldReader world, BlockPos pos);

}
