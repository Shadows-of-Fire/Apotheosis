package dev.shadowsoffire.apotheosis.util;

import java.util.UUID;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.UsernameCache;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.EventHooks;

public class BlockUtil {

    /**
     * Vanilla Copy: {@link PlayerInteractionManager#tryHarvestBlock} <br>
     * Attempts to harvest a block as if the player with the given uuid
     * harvested it while holding the passed item.
     *
     * @param level    The world the block is in.
     * @param pos      The position of the block.
     * @param mainhand The main hand item that the player is supposibly holding.
     * @param source   The UUID of the breaking player.
     * @return If the block was successfully broken.
     */
    public static boolean breakExtraBlock(ServerLevel level, BlockPos pos, ItemStack mainhand, @Nullable UUID source) {
        BlockState state = level.getBlockState(pos);
        FakePlayer player;
        if (source != null) {
            player = FakePlayerFactory.get(level, new GameProfile(source, UsernameCache.getLastKnownUsername(source)));
            Player realPlayer = level.getPlayerByUUID(source);
            if (realPlayer != null) {
                player.setPos(realPlayer.position());
            }
        }
        else {
            player = FakePlayerFactory.getMinecraft(level);
        }

        player.getInventory().items.set(player.getInventory().selected, mainhand);
        player.setPos(pos.getX(), pos.getY(), pos.getZ());

        if (state.getDestroySpeed(level, pos) < 0 || !state.canHarvestBlock(level, pos, player)) {
            return false;
        }

        GameType type = player.getAbilities().instabuild ? GameType.CREATIVE : GameType.SURVIVAL;
        var event = net.neoforged.neoforge.common.CommonHooks.fireBlockBreak(level, type, player, pos, state);
        if (event.isCanceled()) {
            return false;
        }
        else {
            BlockEntity tile = level.getBlockEntity(pos);
            Block block = state.getBlock();
            if (block instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
                level.sendBlockUpdated(pos, state, state, 3);
                return false;
            }
            else if (player.blockActionRestricted(level, pos, type)) {
                return false;
            }
            else {
                BlockState newState = block.playerWillDestroy(level, pos, state, player);
                if (type.isCreative()) {
                    removeBlock(level, player, pos, newState, false);
                    return true;
                }
                else {
                    ItemStack held = player.getMainHandItem();
                    ItemStack heldCopy = held.copy();
                    boolean canHarvest = newState.canHarvestBlock(level, pos, player);
                    held.mineBlock(level, state, pos, player);
                    boolean removed = removeBlock(level, player, pos, newState, canHarvest);

                    if (removed && canHarvest) {
                        block.playerDestroy(level, player, pos, newState, tile, heldCopy);
                    }

                    if (held.isEmpty() && !heldCopy.isEmpty()) {
                        EventHooks.onPlayerDestroyItem(player, heldCopy, InteractionHand.MAIN_HAND);
                    }

                    return true;
                }
            }
        }
    }

    /**
     * Vanilla Copy: {@link ServerPlayerGameMode#removeBlock}
     */
    public static boolean removeBlock(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, boolean canHarvest) {
        boolean removed = state.onDestroyedByPlayer(level, pos, player, canHarvest, level.getFluidState(pos));
        if (removed) {
            state.getBlock().destroy(level, pos, state);
        }
        return removed;
    }

}
