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
	public ActionResultType onItemUse(ItemUseContext ctx) {
		World world = ctx.getWorld();
		if (world.isRemote) return ActionResultType.SUCCESS;
		BossItem item = BossItemManager.INSTANCE.getRandomItem(world.getRandom());
		BlockPos pos = ctx.getPos().offset(ctx.getFace());
		if (!world.hasNoCollisions(item.getSize().offset(pos))) {
			pos = pos.up();
			if (!world.hasNoCollisions(item.getSize().offset(pos))) return ActionResultType.FAIL;
		}
		world.addEntity(item.createBoss((ServerWorld) world, pos, world.getRandom()));
		ctx.getItem().shrink(1);
		return ActionResultType.SUCCESS;
	}

}
