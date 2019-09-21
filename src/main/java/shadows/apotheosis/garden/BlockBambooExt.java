package shadows.apotheosis.garden;

import java.util.Random;

import net.minecraft.block.BambooBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockBambooExt extends BambooBlock {

	public BlockBambooExt() {
		super(Block.Properties.create(Material.BAMBOO, MaterialColor.FOLIAGE).tickRandomly().hardnessAndResistance(1.0F).sound(SoundType.BAMBOO));
		setRegistryName(new ResourceLocation("bamboo"));
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
		if (!state.isValidPosition(worldIn, pos)) {
			worldIn.destroyBlock(pos, true);
		} else if (state.get(PROPERTY_STAGE) == 0) {
			if (random.nextInt(3) == 0 && worldIn.isAirBlock(pos.up()) && worldIn.getLightSubtracted(pos.up(), 0) >= 9) {
				int i = this.getNumBambooBlocksBelow(worldIn, pos) + 1;
				if (i < GardenModule.maxBambooHeight) {
					this.grow(state, worldIn, pos, random, i);
				}
			}
		}
	}

	@Override
	public boolean canGrow(IBlockReader worldIn, BlockPos pos, BlockState state, boolean isClient) {
		int i = this.getNumBambooBlocksAbove(worldIn, pos);
		int j = this.getNumBambooBlocksBelow(worldIn, pos);
		return i + j + 1 < GardenModule.maxBambooHeight && worldIn.getBlockState(pos.up(i)).get(PROPERTY_STAGE) != 1;
	}

	@Override
	public void grow(World worldIn, Random rand, BlockPos pos, BlockState state) {
		int i = this.getNumBambooBlocksAbove(worldIn, pos);
		int j = this.getNumBambooBlocksBelow(worldIn, pos);
		int k = i + j + 1;
		int l = 1 + rand.nextInt(2);

		for (int i1 = 0; i1 < l; ++i1) {
			BlockPos blockpos = pos.up(i);
			BlockState blockstate = worldIn.getBlockState(blockpos);
			if (k >= GardenModule.maxBambooHeight || blockstate.get(PROPERTY_STAGE) == 1 || !worldIn.isAirBlock(blockpos.up())) {
				return;
			}
			this.grow(blockstate, worldIn, blockpos, rand, k);
			++i;
			++k;
		}
	}

	@Override
	protected int getNumBambooBlocksAbove(IBlockReader worldIn, BlockPos pos) {
		int i;
		for (i = 0; i < GardenModule.maxBambooHeight && worldIn.getBlockState(pos.up(i + 1)).getBlock() == Blocks.BAMBOO; ++i) {
			;
		}
		return i;
	}

	@Override
	protected int getNumBambooBlocksBelow(IBlockReader worldIn, BlockPos pos) {
		int i;
		for (i = 0; i < GardenModule.maxBambooHeight && worldIn.getBlockState(pos.down(i + 1)).getBlock() == Blocks.BAMBOO; ++i) {
			;
		}
		return i;
	}

}
