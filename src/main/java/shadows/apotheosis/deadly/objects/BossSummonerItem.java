package shadows.apotheosis.deadly.objects;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import shadows.apotheosis.deadly.gen.BossItem;
import shadows.apotheosis.deadly.reload.BossItemManager;

public class BossSummonerItem extends Item {

	public BossSummonerItem(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResultType useOn(ItemUseContext ctx) {
		World world = ctx.getLevel();
		if (world.isClientSide) return ActionResultType.SUCCESS;
		BossItem item = BossItemManager.INSTANCE.getRandomItem(world.getRandom());
		BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
		if (!world.noCollision(item.getSize().move(pos))) {
			pos = pos.above();
			if (!world.noCollision(item.getSize().move(pos))) return ActionResultType.FAIL;
		}
		world.addFreshEntity(item.createBoss((ServerWorld) world, pos, world.getRandom()));
		ctx.getItemInHand().shrink(1);
		return ActionResultType.SUCCESS;
	}

}
