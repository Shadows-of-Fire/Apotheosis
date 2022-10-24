package shadows.apotheosis.adventure.affix.effect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
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

	private static Set<UUID> breakers = new HashSet<>();

	protected final Map<LootRarity, List<RadialData>> values;

	public RadialAffix(Map<LootRarity, List<RadialData>> values) {
		super(AffixType.EFFECT);
		this.values = values;
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack) == LootCategory.BREAKER && this.values.containsKey(rarity);
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		RadialData data = this.getTrueLevel(rarity, level);
		list.accept(new TranslatableComponent("affix." + this.getId() + ".desc", data.x, data.y).withStyle(ChatFormatting.YELLOW));
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

	private RadialData getTrueLevel(LootRarity rarity, float level) {
		var list = this.values.get(rarity);
		return list.get(Math.min(list.size() - 1, (int) Mth.lerp(level, 0, list.size())));
	}

	static class RadialData {
		final int x, y, xOff, yOff;

		public RadialData(int x, int y, int xOff, int yOff) {
			this.x = x;
			this.y = y;
			this.xOff = xOff;
			this.yOff = yOff;
		}

		public void write(FriendlyByteBuf buf) {
			buf.writeVarIntArray(new int[] { x, y, xOff, yOff });
		}

		public static RadialData read(FriendlyByteBuf buf) {
			int[] arr = buf.readVarIntArray();
			return new RadialData(arr[0], arr[1], arr[2], arr[3]);
		}
	}

	public static Affix read(JsonObject obj) {
		Map<LootRarity, List<RadialData>> values = GSON.fromJson(GsonHelper.getAsJsonObject(obj, "values"), new TypeToken<Map<LootRarity, List<RadialData>>>() {
		}.getType());
		return new RadialAffix(values);
	}

	public JsonObject write() {
		return new JsonObject();
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeMap(this.values, (b, key) -> b.writeUtf(key.id()), (b, list) -> {
			b.writeByte(list.size());
			list.forEach(d -> d.write(b));
		});
	}

	public static Affix read(FriendlyByteBuf buf) {
		Map<LootRarity, List<RadialData>> values = buf.readMap(b -> LootRarity.byId(b.readUtf()), b -> {
			int size = b.readByte();
			List<RadialData> list = new ArrayList<>();
			for (int i = 0; i < size; i++)
				list.add(RadialData.read(b));
			return list;
		});
		return new RadialAffix(values);
	}

	/**
	 * Performs the actual extra breaking of blocks
	 * @param player The player breaking the block
	 * @param pos The position of the originally broken block
	 * @param tool The tool being used (which has this affix on it)
	 * @param level The level of this affix, in this case, the mode of operation.
	 */
	public static void breakExtraBlocks(ServerPlayer player, BlockPos pos, ItemStack tool, RadialData level, float hardness) {
		if (!breakers.add(player.getUUID())) return; //Prevent multiple break operations from cascading, and don't execute when sneaking.ew
		if (!player.isShiftKeyDown()) try {
			breakBlockRadius(player, pos, level.x, level.y, level.xOff, level.yOff, hardness);
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
