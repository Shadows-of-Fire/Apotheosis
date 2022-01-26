package shadows.apotheosis.spawn.compat;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import shadows.apotheosis.compat.TOPCompat;
import shadows.apotheosis.spawn.modifiers.SpawnerStats;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;

public class SpawnerTOPPlugin implements TOPCompat.Provider {

	public static void register() {
		TOPCompat.registerProvider(new SpawnerTOPPlugin());
	}

	private Component concat(Object... args) {
		return new TranslatableComponent("misc.apotheosis.value_concat", args).withStyle(ChatFormatting.GRAY);
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level level, BlockState state, IProbeHitData hitData) {
		if (level.getBlockEntity(hitData.getPos()) instanceof ApothSpawnerTile spw) {
			info.text(concat(SpawnerStats.MIN_DELAY.name(), spw.spawner.minSpawnDelay));
			info.text(concat(SpawnerStats.MAX_DELAY.name(), spw.spawner.maxSpawnDelay));
			info.text(concat(SpawnerStats.SPAWN_COUNT.name(), spw.spawner.spawnCount));
			info.text(concat(SpawnerStats.MAX_NEARBY_ENTITIES.name(), spw.spawner.maxNearbyEntities));
			info.text(concat(SpawnerStats.REQ_PLAYER_RANGE.name(), spw.spawner.requiredPlayerRange));
			info.text(concat(SpawnerStats.SPAWN_RANGE.name(), spw.spawner.spawnRange));
			if (spw.ignoresPlayers) info.text(SpawnerStats.IGNORE_PLAYERS.name());
			if (spw.ignoresConditions) info.text(SpawnerStats.IGNORE_CONDITIONS.name());
			if (spw.redstoneControl) info.text(SpawnerStats.REDSTONE_CONTROL.name());
			if (spw.ignoresLight) info.text(SpawnerStats.IGNORE_LIGHT.name());
			if (spw.hasNoAI) info.text(SpawnerStats.NO_AI.name());
		}
	}

}