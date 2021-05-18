package shadows.apotheosis.deadly.affix.impl.tool;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.affix.Affixes;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;
import shadows.placebo.util.PlaceboUtil;

public class RadiusMiningAffix extends Affix {

	private static Set<UUID> breakers = new HashSet<>();

	public RadiusMiningAffix(int weight) {
		super(weight);
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		int lvl = 1 + rand.nextInt(2);
		if (modifier != null) lvl = (int) modifier.editLevel(this, lvl);
		return lvl;
	}

	@Override
	public void addInformation(ItemStack stack, float level, Consumer<ITextComponent> list) {
		list.accept(loreComponent("affix." + this.getRegistryName() + ".desc" + (int) level));
	}

	@Override
	public ITextComponent getDisplayName(float level) {
		return new TranslationTextComponent("affix." + this.getRegistryName() + ".name" + (int) level).mergeStyle(TextFormatting.GRAY);
	}

	@Override
	public float getMin() {
		return 1;
	}

	@Override
	public float getMax() {
		return 3;
	}

	@Override
	public float upgradeLevel(float curLvl, float newLvl) {
		return (int) super.upgradeLevel(curLvl, newLvl);
	}

	@Override
	public float obliterateLevel(float level) {
		return (int) super.obliterateLevel(level);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.PICKAXE || type == EquipmentType.SHOVEL;
	}

	/**
	 * Performs the actual extra breaking of blocks
	 * @param player The player breaking the block
	 * @param pos The position of the originally broken block
	 * @param tool The tool being used (which has this affix on it)
	 * @param level The level of this affix, in this case, the mode of operation.
	 */
	public static void breakExtraBlocks(ServerPlayerEntity player, BlockPos pos, ItemStack tool, int level, float hardness) {
		if (!breakers.add(player.getUniqueID())) return; //Prevent multiple break operations from cascading, and don't execute when sneaking.ew
		if (!player.isSneaking()) try {
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
		breakers.remove(player.getUniqueID());
	}

	@SuppressWarnings("deprecation")
	public static void breakBlockRadius(ServerPlayerEntity player, BlockPos pos, int x, int y, int xOff, int yOff, float hardness) {
		World world = player.world;
		if (x < 2 && y < 2) return;
		int lowerY = (int) Math.ceil(-y / 2D), upperY = (int) Math.round(y / 2D);
		int lowerX = (int) Math.ceil(-x / 2D), upperX = (int) Math.round(x / 2D);

		Vector3d base = player.getEyePosition(0);
		Vector3d look = player.getLookVec();
		double reach = player.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
		Vector3d target = base.add(look.x * reach, look.y * reach, look.z * reach);
		RayTraceResult trace = world.rayTraceBlocks(new RayTraceContext(base, target, BlockMode.OUTLINE, FluidMode.NONE, player));

		if (trace == null || trace.getType() != Type.BLOCK) return;
		BlockRayTraceResult res = (BlockRayTraceResult) trace;

		Direction face = res.getFace(); //Face of the block currently being looked at by the player.

		for (int iy = lowerY; iy < upperY; iy++) {
			for (int ix = lowerX; ix < upperX; ix++) {
				BlockPos genPos = new BlockPos(pos.getX() + ix + xOff, pos.getY() + iy + yOff, pos.getZ());

				if (player.getHorizontalFacing().getAxis() == Axis.X) {
					genPos = new BlockPos(genPos.getX() - (ix + xOff), genPos.getY(), genPos.getZ() + ix + xOff);
				}

				if (face.getAxis().isVertical()) {
					genPos = rotateDown(genPos, iy + yOff, player.getHorizontalFacing());
				}

				if (genPos.equals(pos)) continue;
				BlockState state = world.getBlockState(genPos);
				float stateHardness = state.getBlockHardness(world, genPos);
				if (!state.isAir() && stateHardness != -1 && stateHardness <= hardness * 3F && isEffective(state, player)) PlaceboUtil.tryHarvestBlock(player, genPos);
			}
		}

	}

	static BlockPos rotateDown(BlockPos pos, int y, Direction horizontal) {
		Vector3i vec = horizontal.getDirectionVec();
		return new BlockPos(pos.getX() + vec.getX() * y, pos.getY() - y, pos.getZ() + vec.getZ() * y);
	}

	static boolean isEffective(BlockState state, PlayerEntity player) {
		if (player.getHeldItemMainhand().getToolTypes().stream().anyMatch(state::isToolEffective)) return true;
		if (AffixHelper.getAffixLevel(player.getHeldItemMainhand(), Affixes.OMNITOOL) > 0) return Items.DIAMOND_PICKAXE.canHarvestBlock(state) || Items.DIAMOND_SHOVEL.canHarvestBlock(state) || Items.DIAMOND_AXE.canHarvestBlock(state);
		return false;
	}

}
