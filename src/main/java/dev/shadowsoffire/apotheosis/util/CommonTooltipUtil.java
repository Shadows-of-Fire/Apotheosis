package dev.shadowsoffire.apotheosis.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.base.Predicates;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.ench.table.EnchantingStatRegistry;
import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import it.unimi.dsi.fastutil.floats.Float2FloatMap;
import it.unimi.dsi.fastutil.floats.Float2FloatOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;

public class CommonTooltipUtil {

    public static void appendBossData(Level level, LivingEntity entity, Consumer<Component> tooltip) {
        DynamicHolder<LootRarity> rarity = RarityRegistry.byLegacyId(entity.getPersistentData().getString("apoth.rarity"));
        if (!rarity.isBound()) return;
        tooltip.accept(Component.translatable("info.apotheosis.boss", rarity.get().toComponent()).withStyle(ChatFormatting.GRAY));
        if (FMLEnvironment.production) return;
        tooltip.accept(CommonComponents.EMPTY);
        tooltip.accept(Component.translatable("info.apotheosis.boss_modifiers").withStyle(ChatFormatting.GRAY));
        AttributeMap map = entity.getAttributes();
        ForgeRegistries.ATTRIBUTES.getValues().stream().map(map::getInstance).filter(Predicates.notNull()).forEach(inst -> {
            for (AttributeModifier modif : inst.getModifiers()) {
                if (modif.getName().startsWith("placebo_random_modifier_")) {
                    tooltip.accept(IFormattableAttribute.toComponent(inst.getAttribute(), modif, AttributesLib.getTooltipFlag()));
                }
            }
        });
    }

    public static void appendBlockStats(Level world, BlockState state, Consumer<Component> tooltip) {
        float maxEterna = EnchantingStatRegistry.getMaxEterna(state, world, BlockPos.ZERO);
        float eterna = EnchantingStatRegistry.getEterna(state, world, BlockPos.ZERO);
        float quanta = EnchantingStatRegistry.getQuanta(state, world, BlockPos.ZERO);
        float arcana = EnchantingStatRegistry.getArcana(state, world, BlockPos.ZERO);
        float rectification = EnchantingStatRegistry.getQuantaRectification(state, world, BlockPos.ZERO);
        int clues = EnchantingStatRegistry.getBonusClues(state, world, BlockPos.ZERO);
        if (eterna != 0 || quanta != 0 || arcana != 0 || rectification != 0 || clues != 0) {
            tooltip.accept(Component.translatable("info.apotheosis.ench_stats").withStyle(ChatFormatting.GOLD));
        }
        if (eterna != 0) {
            if (eterna > 0) {
                tooltip.accept(Component.translatable("info.apotheosis.eterna.p", String.format("%.2f", eterna), String.format("%.2f", maxEterna)).withStyle(ChatFormatting.GREEN));
            }
            else tooltip.accept(Component.translatable("info.apotheosis.eterna", String.format("%.2f", eterna)).withStyle(ChatFormatting.GREEN));
        }
        if (quanta != 0) {
            tooltip.accept(Component.translatable("info.apotheosis.quanta" + (quanta > 0 ? ".p" : ""), String.format("%.2f", quanta)).withStyle(ChatFormatting.RED));
        }
        if (arcana != 0) {
            tooltip.accept(Component.translatable("info.apotheosis.arcana" + (arcana > 0 ? ".p" : ""), String.format("%.2f", arcana)).withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (rectification != 0) {
            tooltip.accept(Component.translatable("info.apotheosis.rectification" + (rectification > 0 ? ".p" : ""), String.format("%.2f", rectification)).withStyle(ChatFormatting.YELLOW));
        }
        if (clues != 0) {
            tooltip.accept(Component.translatable("info.apotheosis.clues" + (clues > 0 ? ".p" : ""), String.format("%d", clues)).withStyle(ChatFormatting.DARK_AQUA));
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
        tooltip.accept(Component.translatable("info.apotheosis.eterna.t", String.format("%.2f", stats[0]), String.format("%.2f", EnchantingStatRegistry.getAbsoluteMaxEterna())).withStyle(ChatFormatting.GREEN));
        tooltip.accept(Component.translatable("info.apotheosis.quanta.t", String.format("%.2f", Math.min(100, stats[1]))).withStyle(ChatFormatting.RED));
        tooltip.accept(Component.translatable("info.apotheosis.arcana.t", String.format("%.2f", Math.min(100, stats[2]))).withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.accept(Component.translatable("info.apotheosis.rectification.t", String.format("%.2f", Mth.clamp(stats[3], -100, 100))).withStyle(ChatFormatting.YELLOW));
        tooltip.accept(Component.translatable("info.apotheosis.clues.t", String.format("%d", (int) stats[4] + 1)).withStyle(ChatFormatting.DARK_AQUA));
    }

    public static void gatherStats(Float2FloatMap eternaMap, float[] stats, Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) return;
        float max = EnchantingStatRegistry.getMaxEterna(state, world, pos);
        float eterna = EnchantingStatRegistry.getEterna(state, world, pos);
        eternaMap.put(max, eternaMap.getOrDefault(max, 0) + eterna);
        float quanta = EnchantingStatRegistry.getQuanta(state, world, pos);
        stats[1] += quanta;
        float arcana = EnchantingStatRegistry.getArcana(state, world, pos);
        stats[2] += arcana;
        float quantaRec = EnchantingStatRegistry.getQuantaRectification(state, world, pos);
        stats[3] += quantaRec;
        int clues = EnchantingStatRegistry.getBonusClues(state, world, pos);
        stats[4] += clues;
    }
}
