package dev.shadowsoffire.apotheosis.item;

import dev.shadowsoffire.apotheosis.mobs.registries.InvaderRegistry;
import dev.shadowsoffire.apotheosis.mobs.types.Invader;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
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
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = ctx.getPlayer();
        GenContext gCtx = GenContext.forPlayer(player);

        Invader item = InvaderRegistry.INSTANCE.getRandomItem(gCtx);
        if (item == null) {
            return InteractionResult.FAIL;
        }

        BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
        if (!world.noCollision(item.size().move(pos))) {
            pos = pos.above();
            if (!world.noCollision(item.size().move(pos))) {
                return InteractionResult.FAIL;
            }
        }

        Mob boss = item.createBoss((ServerLevel) world, pos, gCtx);
        boss.setTarget(player);
        ((ServerLevel) world).addFreshEntityWithPassengers(boss);
        ctx.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }

}
