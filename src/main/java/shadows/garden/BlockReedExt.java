package shadows.garden;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class BlockReedExt extends SugarCaneBlock {

	public BlockReedExt() {
		super(Block.Properties.create(Material.PLANTS).doesNotBlockMovement().tickRandomly().hardnessAndResistance(0).sound(SoundType.PLANT));
		setRegistryName(new ResourceLocation("sugar_cane"));
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
		if (!state.isValidPosition(worldIn, pos)) {
			worldIn.destroyBlock(pos, true);
		} else if (worldIn.isAirBlock(pos.up())) {
			int i = 0;
			if (GardenModule.maxReedHeight != 255) for (i = 1; worldIn.getBlockState(pos.down(i)).getBlock() == this; ++i)
				;

			if (i < GardenModule.maxReedHeight) {
				int j = state.get(AGE);
				if (ForgeHooks.onCropsGrowPre(worldIn, pos, state, true)) {
					if (j == 15) {
						worldIn.setBlockState(pos.up(), this.getDefaultState());
						worldIn.setBlockState(pos, state.with(AGE, Integer.valueOf(0)), 4);
					} else {
						worldIn.setBlockState(pos, state.with(AGE, Integer.valueOf(j + 1)), 4);
					}
					ForgeHooks.onCropsGrowPost(worldIn, pos, state);
				}
			}
		}

	}

	@Override
	@Deprecated
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos origin, boolean isMoving) {
		if (pos.getY() != origin.getY()) {
			super.neighborChanged(state, world, pos, block, origin, isMoving);
		}
	}

}
