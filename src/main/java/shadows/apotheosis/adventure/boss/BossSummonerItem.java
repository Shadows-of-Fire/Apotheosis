package shadows.apotheosis.adventure.boss;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class BossSummonerItem extends Item {

	public BossSummonerItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level world = ctx.getLevel();
		if (world.isClientSide) return InteractionResult.SUCCESS;
		BossItem item = BossItemManager.INSTANCE.getRandomItem(world.getRandom(), (ServerLevel) world);
		BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
		if (!world.noCollision(item.getSize().move(pos))) {
			pos = pos.above();
			if (!world.noCollision(item.getSize().move(pos))) return InteractionResult.FAIL;
		}
		world.addFreshEntity(item.createBoss((ServerLevel) world, pos, world.getRandom()));
		ctx.getItemInHand().shrink(1);
		return InteractionResult.SUCCESS;
	}

}