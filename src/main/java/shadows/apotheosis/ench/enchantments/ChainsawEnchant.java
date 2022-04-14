package shadows.apotheosis.ench.enchantments;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import shadows.apotheosis.ench.EnchModule;
import shadows.apotheosis.util.BlockUtil;
import shadows.placebo.util.PlaceboTaskQueue;

public class ChainsawEnchant extends Enchantment {

	public ChainsawEnchant() {
		super(Rarity.VERY_RARE, EnchModule.AXE, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
	}

	@Override
	public int getMaxLevel() {
		return 1;
	}

	@Override
	public int getMinCost(int level) {
		return 55;
	}

	@Override
	public int getMaxCost(int enchantmentLevel) {
		return 200;
	}

	@Override
	public ITextComponent getFullname(int level) {
		return ((IFormattableTextComponent) super.getFullname(level)).withStyle(TextFormatting.DARK_GREEN);
	}

	public void chainsaw(BreakEvent e) {
		PlayerEntity player = e.getPlayer();
		World level = player.level;
		ItemStack stack = player.getMainHandItem();
		int enchLevel = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
		if (player.getClass() == ServerPlayerEntity.class && enchLevel > 0 && !level.isClientSide && this.isTree(level, e.getPos(), e.getState())) {
			if (!player.abilities.instabuild) PlaceboTaskQueue.submitTask("apotheosis:chainsaw_task", new ChainsawTask(player.getUUID(), stack, level, e.getPos()));
		}
	}

	private boolean isTree(World level, BlockPos pos, BlockState state) {
		if (!state.is(BlockTags.LOGS)) return false;
		while (state.is(BlockTags.LOGS)) {
			state = level.getBlockState(pos = pos.above());
		}
		for (BlockPos p : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
			if (level.getBlockState(p).is(BlockTags.LEAVES)) return true;
		}
		return false;
	}

	private static class ChainsawTask implements BooleanSupplier {

		UUID owner;
		ItemStack axe;
		ServerWorld level;
		Int2ObjectMap<Queue<BlockPos>> hits = new Int2ObjectOpenHashMap<>();
		int ticks = 0;

		public ChainsawTask(UUID owner, ItemStack axe, World level, BlockPos pos) {
			this.owner = owner;
			this.axe = axe;
			this.level = (ServerWorld) level;
			this.hits.computeIfAbsent(pos.getY(), i -> new ArrayDeque<>()).add(pos);
		}

		@Override
		public boolean getAsBoolean() {
			if (++this.ticks % 2 != 0) return false;
			if (this.axe.isEmpty()) return true;
			int minY = this.hits.keySet().stream().sorted().findFirst().orElseThrow(() -> new RuntimeException());
			Queue<BlockPos> queue = this.hits.remove(minY);
			while (!queue.isEmpty()) {
				BlockPos pos = queue.poll();
				for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 1, 1))) {
					BlockState state = this.level.getBlockState(p);
					if (state.is(BlockTags.LOGS)) {
						BlockUtil.breakExtraBlock(this.level, p, this.axe, this.owner);
						this.hits.computeIfAbsent(p.getY(), i -> new ArrayDeque<>()).add(p.immutable());
					}
				}
			}
			return this.hits.isEmpty();
		}

	}
}
