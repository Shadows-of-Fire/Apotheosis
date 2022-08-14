package shadows.apotheosis.adventure.affix.effect;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.floats.Float2IntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.world.BlockEvent;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixInstance;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.util.PlaceboUtil;

public class RadialAffix extends Affix {

	protected static final Float2IntFunction SIZE_FUNC = AffixHelper.step(1, 2, 1);
	private static Set<UUID> breakers = new HashSet<>();

	public RadialAffix() {
		super(AffixType.EFFECT);
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack) == LootCategory.BREAKER;
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		list.accept(new TranslatableComponent("affix." + this.getRegistryName() + ".desc." + getTrueLevel(rarity, level)).withStyle(ChatFormatting.YELLOW));
	}

	// EventPriority.LOW
	public void onBreak(BlockEvent.BreakEvent e) {
		Player player = e.getPlayer();
		ItemStack tool = player.getMainHandItem();
		Level world = player.level;
		if (!world.isClientSide && tool.hasTag()) {
			AffixInstance inst = AffixHelper.getAffixes(tool).get(this);
			if (inst != null) {
				float hardness = e.getState().getDestroySpeed(e.getWorld(), e.getPos());
				breakExtraBlocks((ServerPlayer) player, e.getPos(), tool, getTrueLevel(inst.rarity(), inst.level()), hardness);
			}
		}
	}

	private static int getTrueLevel(LootRarity rarity, float level) {
		return Math.min(4, (rarity.ordinal() - LootRarity.RARE.ordinal()) + SIZE_FUNC.get(level));
	}

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
			} else if (level == 3) {
				breakBlockRadius(player, pos, 3, 3, 0, 0, hardness);
			} else {
				breakBlockRadius(player, pos, 5, 5, 0, 0, hardness);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		breakers.remove(player.getUUID());
	}

	@SuppressWarnings("deprecation")
	public static void breakBlockRadius(ServerPlayer player, BlockPos pos, int x, int y, int xOff, int yOff, float hardness) {
		Level world = player.level;
		if (x < 2 && y < 2) return;
		int lowerY = (int) Math.ceil(-y / 2D), upperY = (int) Math.round(y / 2D);
		int lowerX = (int) Math.ceil(-x / 2D), upperX = (int) Math.round(x / 2D);

		Vec3 base = player.getEyePosition(0);
		Vec3 look = player.getLookAngle();
		double reach = player.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
		Vec3 target = base.add(look.x * reach, look.y * reach, look.z * reach);
		HitResult trace = world.clip(new ClipContext(base, target, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

		if (trace == null || trace.getType() != Type.BLOCK) return;
		BlockHitResult res = (BlockHitResult) trace;

		Direction face = res.getDirection(); //Face of the block currently being looked at by the player.

		for (int iy = lowerY; iy < upperY; iy++) {
			for (int ix = lowerX; ix < upperX; ix++) {
				BlockPos genPos = new BlockPos(pos.getX() + ix + xOff, pos.getY() + iy + yOff, pos.getZ());

				if (player.getDirection().getAxis() == Axis.X) {
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
		Vec3i vec = horizontal.getNormal();
		return new BlockPos(pos.getX() + vec.getX() * y, pos.getY() - y, pos.getZ() + vec.getZ() * y);
	}

	static boolean isEffective(BlockState state, Player player) {
		return player.hasCorrectToolForDrops(state);
	}

}
