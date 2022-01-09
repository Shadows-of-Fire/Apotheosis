package shadows.apotheosis.ench.objects;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RevealerBlock extends Block implements IEnchantingBlock {

	protected final int clues;

	public RevealerBlock(int clues, Properties props) {
		super(props);
		this.clues = clues;
	}

	@Override
	public float getEnchantPowerBonus(BlockState state, LevelReader world, BlockPos pos) {
		return 0;
	}

	@Override
	public int getBonusClues(BlockState state, LevelReader world, BlockPos pos) {
		return this.clues;
	}

}
