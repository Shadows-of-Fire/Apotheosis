package dev.shadowsoffire.apotheosis.spawn.compat;

import dev.shadowsoffire.apotheosis.spawn.modifiers.SpawnerStats;
import dev.shadowsoffire.apotheosis.spawn.spawner.ApothSpawnerBlock;
import dev.shadowsoffire.apotheosis.spawn.spawner.ApothSpawnerTile;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import dev.shadowsoffire.placebo.compat.TOPCompat;

public class SpawnerTOPPlugin implements TOPCompat.Provider {

    public static void register() {
        TOPCompat.registerProvider(new SpawnerTOPPlugin());
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level level, BlockState state, IProbeHitData hitData) {
        if (level.getBlockEntity(hitData.getPos()) instanceof ApothSpawnerTile spw) {
            info.mcText(ApothSpawnerBlock.concat(SpawnerStats.MIN_DELAY.name(), spw.spawner.minSpawnDelay));
            info.mcText(ApothSpawnerBlock.concat(SpawnerStats.MAX_DELAY.name(), spw.spawner.maxSpawnDelay));
            info.mcText(ApothSpawnerBlock.concat(SpawnerStats.SPAWN_COUNT.name(), spw.spawner.spawnCount));
            info.mcText(ApothSpawnerBlock.concat(SpawnerStats.MAX_NEARBY_ENTITIES.name(), spw.spawner.maxNearbyEntities));
            info.mcText(ApothSpawnerBlock.concat(SpawnerStats.REQ_PLAYER_RANGE.name(), spw.spawner.requiredPlayerRange));
            info.mcText(ApothSpawnerBlock.concat(SpawnerStats.SPAWN_RANGE.name(), spw.spawner.spawnRange));
            if (spw.ignoresPlayers) info.mcText(SpawnerStats.IGNORE_PLAYERS.name().withStyle(ChatFormatting.DARK_GREEN));
            if (spw.ignoresConditions) info.mcText(SpawnerStats.IGNORE_CONDITIONS.name().withStyle(ChatFormatting.DARK_GREEN));
            if (spw.redstoneControl) info.mcText(SpawnerStats.REDSTONE_CONTROL.name().withStyle(ChatFormatting.DARK_GREEN));
            if (spw.ignoresLight) info.mcText(SpawnerStats.IGNORE_LIGHT.name().withStyle(ChatFormatting.DARK_GREEN));
            if (spw.hasNoAI) info.mcText(SpawnerStats.NO_AI.name().withStyle(ChatFormatting.DARK_GREEN));
            if (spw.silent) info.mcText(SpawnerStats.SILENT.name().withStyle(ChatFormatting.DARK_GREEN));
        }
    }

}
