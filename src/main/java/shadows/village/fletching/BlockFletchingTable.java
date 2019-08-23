package shadows.village.fletching;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FletchingTableBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class BlockFletchingTable extends FletchingTableBlock {

	public static final ITextComponent NAME = new TranslationTextComponent(Blocks.FLETCHING_TABLE.getTranslationKey());

	public BlockFletchingTable() {
		super(Block.Properties.create(Material.WOOD).hardnessAndResistance(2.5F).sound(SoundType.WOOD));
		this.setRegistryName("minecraft", "fletching_table");
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		player.openContainer(state.getContainer(worldIn, pos));
		return true;
	}

	@Override
	public INamedContainerProvider getContainer(BlockState state, World world, BlockPos pos) {
		return new SimpleNamedContainerProvider((id, inv, player) -> {
			return new FletchingContainer(id, inv, world, pos);
		}, NAME);
	}

}
