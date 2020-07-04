package shadows.apotheosis.ench.table;

import javax.annotation.Nullable;

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

public class EnchantingTableBlockExt extends EnchantingTableBlock {

	public EnchantingTableBlockExt() {
		super(Block.Properties.create(Material.ROCK, MaterialColor.RED).hardnessAndResistance(5.0F, 1200.0F));
	}

	@Override
	@Nullable
	public INamedContainerProvider getContainer(BlockState state, World world, BlockPos pos) {
		TileEntity tileentity = world.getTileEntity(pos);
		if (tileentity instanceof EnchantingTableTileEntityExt) {
			ITextComponent itextcomponent = ((INameable) tileentity).getDisplayName();
			return new SimpleNamedContainerProvider((p_220147_2_, p_220147_3_, p_220147_4_) -> {
				return new EnchantmentContainerExt(p_220147_2_, p_220147_3_, IWorldPosCallable.of(world, pos), (EnchantingTableTileEntityExt) tileentity);
			}, itextcomponent);
		} else {
			return null;
		}
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader p_196283_1_) {
		return new EnchantingTableTileEntityExt();
	}

	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean p_196243_5_) {
		if (state.getBlock() != newState.getBlock()) {
			TileEntity tileentity = world.getTileEntity(pos);
			if (tileentity instanceof EnchantingTableTileEntityExt) {
				Block.spawnAsEntity(world, pos, ((EnchantingTableTileEntityExt) tileentity).inv.getStackInSlot(0));
				world.removeTileEntity(pos);
			}
		}
	}

}
