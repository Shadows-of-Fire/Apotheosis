package dev.shadowsoffire.apotheosis.adventure.boss;

import dev.shadowsoffire.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.IDimensional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
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
        Player player = ctx.getPlayer();
        ApothBoss item = BossRegistry.INSTANCE.getRandomItem(world.getRandom(), ctx.getPlayer().getLuck(), IDimensional.matches(world), IStaged.matches(player));
        if (item == null) return InteractionResult.FAIL;
        BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
        if (!world.noCollision(item.getSize().move(pos))) {
            pos = pos.above();
            if (!world.noCollision(item.getSize().move(pos))) return InteractionResult.FAIL;
        }
        Mob boss = item.createBoss((ServerLevel) world, pos, world.getRandom(), player.getLuck());
        boss.setTarget(player);
        ((ServerLevel) world).addFreshEntityWithPassengers(boss);
        ctx.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }

}
