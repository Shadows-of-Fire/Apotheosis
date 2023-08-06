package dev.shadowsoffire.apotheosis.util;

import java.util.UUID;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

public class BlockUtil {

    /**
     * Vanilla Copy: {@link PlayerInteractionManager#tryHarvestBlock} <br>
     * Attempts to harvest a block as if the player with the given uuid
     * harvested it while holding the passed item.
     *
     * @param world    The world the block is in.
     * @param pos      The position of the block.
     * @param mainhand The main hand item that the player is supposibly holding.
     * @param source   The UUID of the breaking player.
     * @return If the block was successfully broken.
     */
    public static boolean breakExtraBlock(ServerLevel world, BlockPos pos, ItemStack mainhand, @Nullable UUID source) {
        BlockState blockstate = world.getBlockState(pos);
        FakePlayer player;
        if (source != null) {
            player = FakePlayerFactory.get(world, new GameProfile(source, UsernameCache.getLastKnownUsername(source)));
            Player realPlayer = world.getPlayerByUUID(source);
            if (realPlayer != null) player.setPos(realPlayer.position());
        }
        else player = FakePlayerFactory.getMinecraft(world);
        player.getInventory().items.set(player.getInventory().selected, mainhand);
        // player.setPos(pos.getX(), pos.getY(), pos.getZ());

        if (blockstate.getDestroySpeed(world, pos) < 0 || !blockstate.canHarvestBlock(world, pos, player)) return false;

        GameType type = player.getAbilities().instabuild ? GameType.CREATIVE : GameType.SURVIVAL;
        int exp = net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(world, type, player, pos);
        if (exp == -1) {
            return false;
        }
        else {
            BlockEntity tileentity = world.getBlockEntity(pos);
            Block block = blockstate.getBlock();
            if ((block instanceof CommandBlock || block instanceof StructureBlock || block instanceof JigsawBlock) && !player.canUseGameMasterBlocks()) {
                world.sendBlockUpdated(pos, blockstate, blockstate, 3);
                return false;
            }
            else if (player.getMainHandItem().onBlockStartBreak(pos, player)) {
                return false;
            }
            else if (player.blockActionRestricted(world, pos, type)) {
                return false;
            }
            else {
                if (player.getAbilities().instabuild) {
                    removeBlock(world, player, pos, false);
                    return true;
                }
                else {
                    ItemStack itemstack = player.getMainHandItem();
                    ItemStack itemstack1 = itemstack.copy();
                    boolean canHarvest = blockstate.canHarvestBlock(world, pos, player);
                    itemstack.mineBlock(world, blockstate, pos, player);
                    if (itemstack.isEmpty() && !itemstack1.isEmpty()) net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, itemstack1, InteractionHand.MAIN_HAND);
                    boolean removed = removeBlock(world, player, pos, canHarvest);

                    if (removed && canHarvest) {
                        block.playerDestroy(world, player, pos, blockstate, tileentity, itemstack1);
                    }

                    if (removed && exp > 0) blockstate.getBlock().popExperience(world, pos, exp);

                    return true;
                }
            }
        }
    }

    /**
     * Vanilla Copy: {@link PlayerInteractionManager#removeBlock}
     *
     * @param world      The world
     * @param player     The removing player
     * @param pos        The block location
     * @param canHarvest If the player can actually harvest this block.
     * @return If the block was actually removed.
     */
    public static boolean removeBlock(ServerLevel world, ServerPlayer player, BlockPos pos, boolean canHarvest) {
        BlockState state = world.getBlockState(pos);
        boolean removed = state.onDestroyedByPlayer(world, pos, player, canHarvest, world.getFluidState(pos));
        if (removed) state.getBlock().destroy(world, pos, state);
        return removed;
    }

}
