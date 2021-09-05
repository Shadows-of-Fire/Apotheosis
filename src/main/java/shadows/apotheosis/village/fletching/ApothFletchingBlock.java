package shadows.apotheosis.village.fletching;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FletchingTableBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import shadows.placebo.util.IReplacementBlock;

public class ApothFletchingBlock extends FletchingTableBlock implements IReplacementBlock {

	public static final ITextComponent NAME = new TranslationTextComponent("apotheosis.recipes.fletching");

	public ApothFletchingBlock() {
		super(AbstractBlock.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD));
		this.setRegistryName("minecraft", "fletching_table");
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (worldIn.isClientSide) return ActionResultType.SUCCESS;
		player.openMenu(this.getMenuProvider(state, worldIn, pos));
		return ActionResultType.CONSUME;
	}

	@Override
	public INamedContainerProvider getMenuProvider(BlockState state, World world, BlockPos pos) {
		return What.getMenuProvider(state, world, pos);
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

	//I literally cannot fathom why this is necessary https://github.com/Shadows-of-Fire/Apotheosis/issues/441
	//TODO: Remove after update to FG 5 - appears to have been bug in SpecialSource.
	private static class What {

		static INamedContainerProvider getMenuProvider(BlockState state, World world, BlockPos pos) {
			return new SimpleNamedContainerProvider((id, inv, player) -> new FletchingContainer(id, inv, world, pos), NAME);
		}
	}
}