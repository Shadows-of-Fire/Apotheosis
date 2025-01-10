package dev.shadowsoffire.apotheosis.spawn.compat;

import dev.shadowsoffire.apotheosis.spawn.modifiers.SpawnerStats;
import dev.shadowsoffire.apotheosis.spawn.spawner.ApothSpawnerTile;
import dev.shadowsoffire.placebo.compat.TOPCompat;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnerTOPPlugin implements TOPCompat.Provider {

    public static void register() {
        TOPCompat.registerProvider(new SpawnerTOPPlugin());
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level level, BlockState state, IProbeHitData hitData) {
        if (level.getBlockEntity(hitData.getPos()) instanceof ApothSpawnerTile spw) {
            if (Screen.hasControlDown()) {
                SpawnerStats.generateTooltip(spw, info::mcText);
            }
            else {
                info.mcText(Component.translatable("misc.apotheosis.ctrl_stats"));
            }
        }
    }

}
