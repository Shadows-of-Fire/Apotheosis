package shadows.apotheosis.ench.table;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.INameable;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shadows.placebo.util.IReplacementBlock;

public class ApothEnchantBlock extends EnchantingTableBlock implements IReplacementBlock {

	public ApothEnchantBlock() {
		super(AbstractBlock.Properties.of(Material.STONE, MaterialColor.COLOR_RED).strength(5.0F, 1200.0F));
		this.setRegistryName("minecraft:enchanting_table");
	}

	@Override
	@Nullable
	public INamedContainerProvider getMenuProvider(BlockState state, World world, BlockPos pos) {
		TileEntity tileentity = world.getBlockEntity(pos);
		if (tileentity instanceof ApothEnchantTile) {
			ITextComponent itextcomponent = ((INameable) tileentity).getDisplayName();
			return new SimpleNamedContainerProvider((id, inventory, player) -> new ApothEnchantContainer(id, inventory, IWorldPosCallable.create(world, pos), (ApothEnchantTile) tileentity), itextcomponent);
		} else {
			return null;
		}
	}

	@Override
	public TileEntity newBlockEntity(IBlockReader worldIn) {
		return new ApothEnchantTile();
	}

	@Override
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			TileEntity tileentity = world.getBlockEntity(pos);
			if (tileentity instanceof ApothEnchantTile) {
				Block.popResource(world, pos, ((ApothEnchantTile) tileentity).inv.getStackInSlot(0));
				world.removeBlockEntity(pos);
			}
		}
	}

	@Override
	public void _setDefaultState(BlockState state) {
		this.registerDefaultState(state);
	}

	protected StateContainer<Block, BlockState> container;

	@Override
	public void setStateContainer(StateContainer<Block, BlockState> container) {
		this.container = container;
	}

	@Override
	public StateContainer<Block, BlockState> getStateDefinition() {
		return this.container == null ? super.getStateDefinition() : this.container;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, World level, BlockPos pos, Random rand) {
		for (int i = -2; i <= 2; ++i) {
			for (int j = -2; j <= 2; ++j) {
				if (i > -2 && i < 2 && j == -1) {
					j = 2;
				}

				if (rand.nextInt(16) == 0) {
					for (int k = 0; k <= 1; ++k) {
						BlockPos blockpos = pos.offset(i, k, j);
						if (EnchantingStatManager.getEterna(level.getBlockState(blockpos), level, blockpos) > 0) {
							if (!level.isEmptyBlock(pos.offset(i / 2, 0, j / 2))) {
								break;
							}

							level.addParticle(ParticleTypes.ENCHANT, (double) pos.getX() + 0.5D, (double) pos.getY() + 2.0D, (double) pos.getZ() + 0.5D, (double) ((float) i + rand.nextFloat()) - 0.5D, (double) ((float) k - rand.nextFloat() - 1.0F), (double) ((float) j + rand.nextFloat()) - 0.5D);
						}
					}
				}
			}
		}

	}

}