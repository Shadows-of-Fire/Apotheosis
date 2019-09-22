package shadows.apotheosis.garden;

import java.util.Random;

import net.minecraft.block.BambooBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.state.properties.BambooLeaves;
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
		int bambooAbove = this.getNumBambooBlocksAbove(worldIn, pos);
		int bambooBelow = this.getNumBambooBlocksBelow(worldIn, pos);
		int bambooSize = bambooAbove + bambooBelow + 1;
		int l = 1 + rand.nextInt(2);

		for (int i1 = 0; i1 < l; ++i1) {
			BlockPos blockpos = pos.up(bambooAbove);
			BlockState blockstate = worldIn.getBlockState(blockpos);
			if (bambooSize >= GardenModule.maxBambooHeight || blockstate.get(PROPERTY_STAGE) == 1 || !worldIn.isAirBlock(blockpos.up())) {
				return;
			}
			this.grow(blockstate, worldIn, blockpos, rand, bambooSize);
			++bambooAbove;
			++bambooSize;
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

	@Override
	protected void grow(BlockState blockStateIn, World worldIn, BlockPos posIn, Random rand, int size) {
		BlockState blockstate = worldIn.getBlockState(posIn.down());
		BlockPos blockpos = posIn.down(2);
		BlockState blockstate1 = worldIn.getBlockState(blockpos);
		BambooLeaves bambooleaves = BambooLeaves.NONE;
		if (size >= 1) {
			if (blockstate.getBlock() == Blocks.BAMBOO && blockstate.get(PROPERTY_BAMBOO_LEAVES) != BambooLeaves.NONE) {
				if (blockstate.getBlock() == Blocks.BAMBOO && blockstate.get(PROPERTY_BAMBOO_LEAVES) != BambooLeaves.NONE) {
					bambooleaves = BambooLeaves.LARGE;
					if (blockstate1.getBlock() == Blocks.BAMBOO) {
						worldIn.setBlockState(posIn.down(), blockstate.with(PROPERTY_BAMBOO_LEAVES, BambooLeaves.SMALL), 3);
						worldIn.setBlockState(blockpos, blockstate1.with(PROPERTY_BAMBOO_LEAVES, BambooLeaves.NONE), 3);
					}
				}
			} else {
				bambooleaves = BambooLeaves.SMALL;
			}
		}

		int i = blockStateIn.get(PROPERTY_AGE) != 1 && blockstate1.getBlock() != Blocks.BAMBOO ? 0 : 1;
		int j = (size < (GardenModule.maxBambooHeight - (int) GardenModule.maxBambooHeight / 5D) || !(rand.nextFloat() < 0.25F)) && size != GardenModule.maxBambooHeight - 1 ? 0 : 1;
		worldIn.setBlockState(posIn.up(), this.getDefaultState().with(PROPERTY_AGE, Integer.valueOf(i)).with(PROPERTY_BAMBOO_LEAVES, bambooleaves).with(PROPERTY_STAGE, Integer.valueOf(j)), 3);
	}

}
