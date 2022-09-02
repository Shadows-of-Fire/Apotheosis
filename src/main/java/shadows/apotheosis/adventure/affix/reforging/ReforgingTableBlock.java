package shadows.apotheosis.adventure.affix.reforging;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ReforgingTableBlock extends Block {
	private static final Component TITLE = new TranslatableComponent("apotheosis.container.reforge");

	public ReforgingTableBlock(BlockBehaviour.Properties p_56420_) {
		super(p_56420_);
	}

	@Override
	public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
		return new SimpleMenuProvider((id, inv, player) -> {
			return new ReforgingMenu(id, inv, ContainerLevelAccess.create(pLevel, pPos));
		}, TITLE);
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
		if (pLevel.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			pPlayer.openMenu(pState.getMenuProvider(pLevel, pPos));
			return InteractionResult.CONSUME;
		}
	}
}