package shadows.apotheosis.ench.table;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shadows.placebo.util.IReplacementBlock;

public class ApothEnchantBlock extends EnchantmentTableBlock implements IReplacementBlock {

	public ApothEnchantBlock() {
		super(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_RED).strength(5.0F, 1200.0F));
		this.setRegistryName("minecraft:enchanting_table");
	}

	@Override
	@Nullable
	public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
		BlockEntity tileentity = world.getBlockEntity(pos);
		if (tileentity instanceof ApothEnchantTile) {
			Component itextcomponent = ((Nameable) tileentity).getDisplayName();
			return new SimpleMenuProvider((id, inventory, player) -> new ApothEnchantContainer(id, inventory, ContainerLevelAccess.create(world, pos), (ApothEnchantTile) tileentity), itextcomponent);
		} else {
			return null;
		}
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new ApothEnchantTile(pPos, pState);
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			BlockEntity tileentity = world.getBlockEntity(pos);
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

	protected StateDefinition<Block, BlockState> container;

	@Override
	public void setStateContainer(StateDefinition<Block, BlockState> container) {
		this.container = container;
	}

	@Override
	public StateDefinition<Block, BlockState> getStateDefinition() {
		return this.container == null ? super.getStateDefinition() : this.container;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, Level level, BlockPos pos, Random rand) {
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