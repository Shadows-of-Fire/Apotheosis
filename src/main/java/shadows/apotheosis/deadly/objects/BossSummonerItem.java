package shadows.apotheosis.deadly.objects;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import shadows.apotheosis.deadly.gen.BossGenerator;
import shadows.apotheosis.deadly.gen.BossFeatureItem;

public class BossSummonerItem extends Item {

	public BossSummonerItem(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext ctx) {
		World world = ctx.getWorld();
		if (world.isRemote) return ActionResultType.SUCCESS;
		BossFeatureItem item = WeightedRandom.getRandomItem(world.getRandom(), BossGenerator.BOSS_ITEMS);
		BlockPos pos = ctx.getPos().offset(ctx.getFace());
		if (!world.hasNoCollisions(item.getAABB((ServerWorld) world).offset(pos))) {
			pos = pos.up();
			if (!world.hasNoCollisions(item.getAABB((ServerWorld) world).offset(pos))) return ActionResultType.FAIL;
		}
		item.spawnBoss((ServerWorld) world, pos, world.getRandom());
		ctx.getItem().shrink(1);
		return ActionResultType.SUCCESS;
	}

}
