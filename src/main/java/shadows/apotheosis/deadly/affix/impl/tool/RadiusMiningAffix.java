package shadows.apotheosis.deadly.affix.impl.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeMod;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.affix.Affixes;
import shadows.apotheosis.deadly.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;
import shadows.placebo.util.PlaceboUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class RadiusMiningAffix extends RangedAffix {

	private static final Set<UUID> breakers = new HashSet<>();

	public RadiusMiningAffix(LootRarity rarity, int min, int max, int weight) {
		super(rarity, min, max, weight);
	}

	@Override
	public boolean isPrefix() {
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, float level, Consumer<Component> list) {
		list.accept(loreComponent("affix." + this.getRegistryName() + ".desc" + (int) level));
	}

	@Override
	public Component getDisplayName(float level) {
		return new TranslatableComponent("affix." + this.getRegistryName() + ".name" + (int) level).withStyle(ChatFormatting.GRAY);
	}

	@Override
	public float upgradeLevel(float curLvl, float newLvl) {
		return (int) super.upgradeLevel(curLvl, newLvl);
	}

	@Override
	public boolean canApply(LootCategory lootCategory) { return lootCategory == LootCategory.BREAKER; }

	/**
	 * Performs the actual extra breaking of blocks
	 * @param player The player breaking the block
	 * @param pos The position of the originally broken block
	 * @param tool The tool being used (which has this affix on it)
	 * @param level The level of this affix, in this case, the mode of operation.
	 */
	public static void breakExtraBlocks(ServerPlayer player, BlockPos pos, ItemStack tool, int level, float hardness) {
		if (!breakers.add(player.getUUID())) return; //Prevent multiple break operations from cascading, and don't execute when sneaking.ew
		if (!player.isShiftKeyDown()) try {
			if (level == 1) {
				breakBlockRadius(player, pos, 1, 2, 0, 1, hardness);
			} else if (level == 2) {
				breakBlockRadius(player, pos, 3, 2, 0, 1, hardness);
			} else {
				breakBlockRadius(player, pos, 3, 3, 0, 0, hardness);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		breakers.remove(player.getUUID());
	}

	public static void breakBlockRadius(ServerPlayer player, BlockPos pos, int x, int y, int xOff, int yOff, float hardness) {
		var world = player.level;
		if (x < 2 && y < 2) return;
		int lowerY = (int) Math.ceil(-y / 2D), upperY = (int) Math.round(y / 2D);
		int lowerX = (int) Math.ceil(-x / 2D), upperX = (int) Math.round(x / 2D);

		var base = player.getEyePosition(0);
		var look = player.getLookAngle();
		double reach = player.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
		var target = base.add(look.x * reach, look.y * reach, look.z * reach);
		var trace = world.clip(new ClipContext(base, target, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

		if (trace.getType() != HitResult.Type.BLOCK) return;

		Direction face = trace.getDirection(); //Face of the block currently being looked at by the player.

		for (int iy = lowerY; iy < upperY; iy++) {
			for (int ix = lowerX; ix < upperX; ix++) {
				BlockPos genPos = new BlockPos(pos.getX() + ix + xOff, pos.getY() + iy + yOff, pos.getZ());

				if (player.getDirection().getAxis() == Direction.Axis.X) {
					genPos = new BlockPos(genPos.getX() - (ix + xOff), genPos.getY(), genPos.getZ() + ix + xOff);
				}

				if (face.getAxis().isVertical()) {
					genPos = rotateDown(genPos, iy + yOff, player.getDirection());
				}

				if (genPos.equals(pos)) continue;
				BlockState state = world.getBlockState(genPos);
				float stateHardness = state.getDestroySpeed(world, genPos);
				if (!state.isAir() && stateHardness != -1 && stateHardness <= hardness * 3F && isEffective(state, player)) PlaceboUtil.tryHarvestBlock(player, genPos);
			}
		}

	}

	static BlockPos rotateDown(BlockPos pos, int y, Direction horizontal) {
		var vec = horizontal.getNormal();
		return new BlockPos(pos.getX() + vec.getX() * y, pos.getY() - y, pos.getZ() + vec.getZ() * y);
	}

	static boolean isEffective(BlockState state, Player player) {
		if (player.getMainHandItem().getItem().isCorrectToolForDrops(state)) return true;
		if (AffixHelper.getAffixes(player.getMainHandItem()).containsKey(Apoth.Affixes.OMNITOOL)) return Items.DIAMOND_PICKAXE.isCorrectToolForDrops(state) || Items.DIAMOND_SHOVEL.isCorrectToolForDrops(state) || Items.DIAMOND_AXE.isCorrectToolForDrops(state);
		return false;
	}

}
