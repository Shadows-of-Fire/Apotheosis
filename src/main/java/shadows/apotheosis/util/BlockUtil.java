package shadows.apotheosis.util;

import java.util.UUID;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.StructureBlock;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import shadows.placebo.util.NetHandlerSpaghettiServer;

public class BlockUtil {

	/**
	 * Vanilla Copy: {@link PlayerInteractionManager#tryHarvestBlock} <br>
	 * Attempts to harvest a block as if the player with the given uuid
	 * harvested it while holding the passed item.
	 * @param world The world the block is in.
	 * @param pos The position of the block.
	 * @param mainhand The main hand item that the player is supposibly holding.
	 * @param source The UUID of the breaking player.
	 * @return If the block was successfully broken.
	 */
	public static boolean breakExtraBlock(ServerWorld world, BlockPos pos, ItemStack mainhand, @Nullable UUID source) {
		BlockState blockstate = world.getBlockState(pos);
		FakePlayer player;
		if (source != null) player = FakePlayerFactory.get(world, new GameProfile(source, UsernameCache.getLastKnownUsername(source)));
		else player = FakePlayerFactory.getMinecraft(world);
		if (player.connection == null) player.connection = new NetHandlerSpaghettiServer(player);
		player.inventory.items.set(player.inventory.selected, mainhand);
		player.setPos(pos.getX(), pos.getY(), pos.getZ());

		if (blockstate.getDestroySpeed(world, pos) < 0 || !ForgeHooks.canHarvestBlock(blockstate, player, world, pos)) return false;

		GameType type = player.abilities.instabuild ? GameType.CREATIVE : GameType.SURVIVAL;
		int exp = net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(world, type, player, pos);
		if (exp == -1) {
			return false;
		} else {
			TileEntity tileentity = world.getBlockEntity(pos);
			Block block = blockstate.getBlock();
			if ((block instanceof CommandBlockBlock || block instanceof StructureBlock || block instanceof JigsawBlock) && !player.canUseGameMasterBlocks()) {
				world.sendBlockUpdated(pos, blockstate, blockstate, 3);
				return false;
			} else if (player.getMainHandItem().onBlockStartBreak(pos, player)) {
				return false;
			} else if (player.blockActionRestricted(world, pos, type)) {
				return false;
			} else {
				if (player.abilities.instabuild) {
					removeBlock(world, player, pos, false);
					return true;
				} else {
					ItemStack itemstack = player.getMainHandItem();
					ItemStack itemstack1 = itemstack.copy();
					boolean canHarvest = blockstate.canHarvestBlock(world, pos, player); // previously player.hasCorrectToolForDrops(blockstate)
					itemstack.mineBlock(world, blockstate, pos, player);
					if (itemstack.isEmpty() && !itemstack1.isEmpty()) net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, itemstack1, Hand.MAIN_HAND);
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
	 * @param world The world
	 * @param player The removing player
	 * @param pos The block location
	 * @param canHarvest If the player can actually harvest this block.
	 * @return If the block was actually removed.
	 */
	public static boolean removeBlock(ServerWorld world, ServerPlayerEntity player, BlockPos pos, boolean canHarvest) {
		BlockState state = world.getBlockState(pos);
		boolean removed = state.removedByPlayer(world, pos, player, canHarvest, world.getFluidState(pos));
		if (removed) state.getBlock().destroy(world, pos, state);
		return removed;
	}

}
