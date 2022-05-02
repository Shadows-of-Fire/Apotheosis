package shadows.apotheosis.ench.compat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.floats.Float2FloatMap;
import it.unimi.dsi.fastutil.floats.Float2FloatOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import shadows.apotheosis.ench.table.EnchantingStatManager;

public class CommonTooltipUtil {

	public static void appendBlockStats(World world, BlockState state, Consumer<ITextComponent> tooltip) {
		float maxEterna = EnchantingStatManager.getMaxEterna(state, world, BlockPos.ZERO);
		float eterna = EnchantingStatManager.getEterna(state, world, BlockPos.ZERO);
		float quanta = EnchantingStatManager.getQuanta(state, world, BlockPos.ZERO);
		float arcana = EnchantingStatManager.getArcana(state, world, BlockPos.ZERO);
		float rectification = EnchantingStatManager.getQuantaRectification(state, world, BlockPos.ZERO);
		int clues = EnchantingStatManager.getBonusClues(state, world, BlockPos.ZERO);
		if (eterna != 0 || quanta != 0 || arcana != 0 || rectification != 0 || clues != 0) {
			tooltip.accept(new TranslationTextComponent("info.apotheosis.ench_stats").withStyle(TextFormatting.GOLD));
		}
		if (eterna != 0) {
			if (eterna > 0) {
				tooltip.accept(new TranslationTextComponent("info.apotheosis.eterna.p", String.format("%.2f", eterna), String.format("%.2f", maxEterna)).withStyle(TextFormatting.GREEN));
			} else tooltip.accept(new TranslationTextComponent("info.apotheosis.eterna", String.format("%.2f", eterna)).withStyle(TextFormatting.GREEN));
		}
		if (quanta != 0) {
			tooltip.accept(new TranslationTextComponent("info.apotheosis.quanta" + (quanta > 0 ? ".p" : ""), String.format("%.2f", quanta)).withStyle(TextFormatting.RED));
		}
		if (arcana != 0) {
			tooltip.accept(new TranslationTextComponent("info.apotheosis.arcana" + (arcana > 0 ? ".p" : ""), String.format("%.2f", arcana)).withStyle(TextFormatting.DARK_PURPLE));
		}
		if (rectification != 0) {
			tooltip.accept(new TranslationTextComponent("info.apotheosis.rectification" + (rectification > 0 ? ".p" : ""), String.format("%.2f", rectification)).withStyle(TextFormatting.YELLOW));
		}
		if (clues != 0) {
			tooltip.accept(new TranslationTextComponent("info.apotheosis.clues" + (clues > 0 ? ".p" : ""), String.format("%d", clues)).withStyle(TextFormatting.DARK_AQUA));
		}
	}

	public static void appendTableStats(World world, BlockPos pos, Consumer<ITextComponent> tooltip) {
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
		tooltip.accept(new TranslationTextComponent("info.apotheosis.eterna.t", String.format("%.2f", stats[0]), String.format("%.2f", EnchantingStatManager.getAbsoluteMaxEterna())).withStyle(TextFormatting.GREEN));
		tooltip.accept(new TranslationTextComponent("info.apotheosis.quanta.t", String.format("%.2f", Math.min(100, stats[1]))).withStyle(TextFormatting.RED));
		tooltip.accept(new TranslationTextComponent("info.apotheosis.arcana.t", String.format("%.2f", Math.min(100, stats[2]))).withStyle(TextFormatting.DARK_PURPLE));
		tooltip.accept(new TranslationTextComponent("info.apotheosis.rectification.t", String.format("%.2f", MathHelper.clamp(stats[3], -100, 100))).withStyle(TextFormatting.YELLOW));
		tooltip.accept(new TranslationTextComponent("info.apotheosis.clues.t", String.format("%d", (int) stats[4] + 1)).withStyle(TextFormatting.DARK_AQUA));
	}

	@SuppressWarnings("deprecation")
	public static void gatherStats(Float2FloatMap eternaMap, float[] stats, World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (state.isAir(world, pos)) return;
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
