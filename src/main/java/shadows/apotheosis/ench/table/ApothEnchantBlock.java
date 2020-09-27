package shadows.apotheosis.ench.table;

import javax.annotation.Nullable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.INameable;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class ApothEnchantBlock extends EnchantingTableBlock {

	public ApothEnchantBlock() {
		super(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.RED).hardnessAndResistance(5.0F, 1200.0F));
	}

	@Override
	@Nullable
	public INamedContainerProvider getContainer(BlockState state, World world, BlockPos pos) {
		TileEntity tileentity = world.getTileEntity(pos);
		if (tileentity instanceof ApothEnchantTile) {
			ITextComponent itextcomponent = ((INameable) tileentity).getDisplayName();
			return new SimpleNamedContainerProvider((id, inventory, player) -> {
				return new ApothEnchantContainer(id, inventory, IWorldPosCallable.of(world, pos), (ApothEnchantTile) tileentity);
			}, itextcomponent);
		} else {
			return null;
		}
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return new ApothEnchantTile();
	}

	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			TileEntity tileentity = world.getTileEntity(pos);
			if (tileentity instanceof ApothEnchantTile) {
				Block.spawnAsEntity(world, pos, ((ApothEnchantTile) tileentity).inv.getStackInSlot(0));
				world.removeTileEntity(pos);
			}
		}
	}

}