package shadows.apotheosis.ench.objects;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RectifierBlock extends Block implements IEnchantingBlock {

	private final int val;

	public RectifierBlock(int val, Properties prop) {
		super(prop);
		this.val = val;
	}

	@Override
	public float getQuantaRectification(BlockState state, LevelReader world, BlockPos pos) {
		return val;
	}

	@Override
	public float getEnchantPowerBonus(BlockState state, LevelReader world, BlockPos pos) {
		return 0;
	}

}
