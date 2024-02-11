package dev.shadowsoffire.apotheosis.ench.compat;

import dev.shadowsoffire.apotheosis.ench.anvil.AnvilTile;
import dev.shadowsoffire.apotheosis.util.CommonTooltipUtil;
import dev.shadowsoffire.placebo.compat.TOPCompat;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class EnchTOPPlugin implements TOPCompat.Provider {

    public static void register() {
        TOPCompat.registerProvider(new EnchTOPPlugin());
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level level, BlockState state, IProbeHitData hitData) {
        if (level.getBlockEntity(hitData.getPos()) instanceof AnvilTile anvil) {
            Object2IntMap<Enchantment> enchants = anvil.getEnchantments();
            for (Object2IntMap.Entry<Enchantment> e : enchants.object2IntEntrySet()) {
                info.text(e.getKey().getFullname(e.getIntValue()));
            }
        }
        CommonTooltipUtil.appendBlockStats(level, state, hitData.getPos(), info::mcText);
        if (state.getBlock() == Blocks.ENCHANTING_TABLE) CommonTooltipUtil.appendTableStats(level, hitData.getPos(), info::mcText);
    }

}
