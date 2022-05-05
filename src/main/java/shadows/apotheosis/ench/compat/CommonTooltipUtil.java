package shadows.apotheosis.ench.compat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.floats.Float2FloatMap;
import it.unimi.dsi.fastutil.floats.Float2FloatOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import shadows.apotheosis.ench.table.EnchantingStatManager;

public class CommonTooltipUtil {

	public static void appendBlockStats(Level world, BlockState state, Consumer<Component> tooltip) {
		float maxEterna = EnchantingStatManager.getMaxEterna(state, world, BlockPos.ZERO);
		float eterna = EnchantingStatManager.getEterna(state, world, BlockPos.ZERO);
		float quanta = EnchantingStatManager.getQuanta(state, world, BlockPos.ZERO);
		float arcana = EnchantingStatManager.getArcana(state, world, BlockPos.ZERO);
		float rectification = EnchantingStatManager.getQuantaRectification(state, world, BlockPos.ZERO);
		int clues = EnchantingStatManager.getBonusClues(state, world, BlockPos.ZERO);
		if (eterna != 0 || quanta != 0 || arcana != 0 || rectification != 0 || clues != 0) {
			tooltip.accept(new TranslatableComponent("info.apotheosis.ench_stats").withStyle(ChatFormatting.GOLD));
		}
		if (eterna != 0) {
			if (eterna > 0) {
				tooltip.accept(new TranslatableComponent("info.apotheosis.eterna.p", String.format("%.2f", eterna), String.format("%.2f", maxEterna)).withStyle(ChatFormatting.GREEN));
			} else tooltip.accept(new TranslatableComponent("info.apotheosis.eterna", String.format("%.2f", eterna)).withStyle(ChatFormatting.GREEN));
		}
		if (quanta != 0) {
			tooltip.accept(new TranslatableComponent("info.apotheosis.quanta" + (quanta > 0 ? ".p" : ""), String.format("%.2f", quanta)).withStyle(ChatFormatting.RED));
		}
		if (arcana != 0) {
			tooltip.accept(new TranslatableComponent("info.apotheosis.arcana" + (arcana > 0 ? ".p" : ""), String.format("%.2f", arcana)).withStyle(ChatFormatting.DARK_PURPLE));
		}
		if (rectification != 0) {
			tooltip.accept(new TranslatableComponent("info.apotheosis.rectification" + (rectification > 0 ? ".p" : ""), String.format("%.2f", rectification)).withStyle(ChatFormatting.YELLOW));
		}
		if (clues != 0) {
			tooltip.accept(new TranslatableComponent("info.apotheosis.clues" + (clues > 0 ? ".p" : ""), String.format("%d", clues)).withStyle(ChatFormatting.DARK_AQUA));
		}
	}

	public static void appendTableStats(Level world, BlockPos pos, Consumer<Component> tooltip) {
		Float2FloatMap eternaMap = new Float2FloatOpenHashMap();
		float[] stats = { 0, 15F, 0, 0, 0 };
		for (int j = -1; j <= 1; ++j) {
			for (int k = -1; k <= 1; ++k) {
				if ((j != 0 || k != 0) && world.isEmptyBlock(pos.offset(k, 0, j)) && world.isEmptyBlock(pos.offset(k, 1, j))) {
					gatherStats(eternaMap, stats, world, pos.offset(k * 2, 0, j * 2));
					gatherStats(eternaMap, stats, world, pos.offset(k * 2, 1, j * 2));
					if (k != 0 && j != 0) {
						gatherStats(eternaMap, stats, world, pos.offset(k * 2, 0, j));
						gatherStats(eternaMap, stats, world, pos.offset(k * 2, 1, j));
						gatherStats(eternaMap, stats, world, pos.offset(k, 0, j * 2));
						gatherStats(eternaMap, stats, world, pos.offset(k, 1, j * 2));
					}
				}
			}
		}
		List<Float2FloatMap.Entry> entries = new ArrayList<>(eternaMap.float2FloatEntrySet());
		Collections.sort(entries, Comparator.comparing(Float2FloatMap.Entry::getFloatKey));
		for (Float2FloatMap.Entry e : entries) {
			if (e.getFloatKey() > 0) stats[0] = Math.min(e.getFloatKey(), stats[0] + e.getFloatValue());
			else stats[0] += e.getFloatValue();
		}
		tooltip.accept(new TranslatableComponent("info.apotheosis.eterna.t", String.format("%.2f", stats[0]), String.format("%.2f", EnchantingStatManager.getAbsoluteMaxEterna())).withStyle(ChatFormatting.GREEN));
		tooltip.accept(new TranslatableComponent("info.apotheosis.quanta.t", String.format("%.2f", Math.min(100, stats[1]))).withStyle(ChatFormatting.RED));
		tooltip.accept(new TranslatableComponent("info.apotheosis.arcana.t", String.format("%.2f", Math.min(100, stats[2]))).withStyle(ChatFormatting.DARK_PURPLE));
		tooltip.accept(new TranslatableComponent("info.apotheosis.rectification.t", String.format("%.2f", Mth.clamp(stats[3], -100, 100))).withStyle(ChatFormatting.YELLOW));
		tooltip.accept(new TranslatableComponent("info.apotheosis.clues.t", String.format("%d", (int) stats[4] + 1)).withStyle(ChatFormatting.DARK_AQUA));
	}

	public static void gatherStats(Float2FloatMap eternaMap, float[] stats, Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (state.isAir()) return;
		float max = EnchantingStatManager.getMaxEterna(state, world, pos);
		float eterna = EnchantingStatManager.getEterna(state, world, pos);
		eternaMap.put(max, eternaMap.getOrDefault(max, 0) + eterna);
		float quanta = EnchantingStatManager.getQuanta(state, world, pos);
		stats[1] += quanta;
		float arcana = EnchantingStatManager.getArcana(state, world, pos);
		stats[2] += arcana;
		float quantaRec = EnchantingStatManager.getQuantaRectification(state, world, pos);
		stats[3] += quantaRec;
		int clues = EnchantingStatManager.getBonusClues(state, world, pos);
		stats[4] += clues;
	}
}