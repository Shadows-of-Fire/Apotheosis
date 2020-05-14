package shadows.apotheosis.ench.anvil.compat;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import shadows.apotheosis.ench.anvil.BlockAnvilExt;

public class BlockTfarAnvil extends BlockAnvilExt implements IAnvilBlock {

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new TileTfarAnvil();
	}

}