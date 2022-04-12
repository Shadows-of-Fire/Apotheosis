package shadows.apotheosis.spawn.compat;

import java.util.List;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import shadows.apotheosis.spawn.modifiers.SpawnerStats;
import shadows.apotheosis.spawn.spawner.ApothSpawnerBlock;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;

@WailaPlugin
public class SpawnerHwylaPlugin implements IWailaPlugin, IComponentProvider, IServerDataProvider<TileEntity> {

	public static final String STATS = "spw_stats";

	@Override
	public void register(IRegistrar reg) {
		reg.registerBlockDataProvider(this, ApothSpawnerTile.class);
		reg.registerComponentProvider(this, TooltipPosition.BODY, ApothSpawnerBlock.class);
	}

	@Override
	public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
		if (Screen.hasControlDown()) {
			int[] stats = accessor.getServerData().getIntArray(STATS);
			if (stats.length != 11) return;
			tooltip.add(ApothSpawnerBlock.concat(SpawnerStats.MIN_DELAY.name(), stats[0]));
			tooltip.add(ApothSpawnerBlock.concat(SpawnerStats.MAX_DELAY.name(), stats[1]));
			tooltip.add(ApothSpawnerBlock.concat(SpawnerStats.SPAWN_COUNT.name(), stats[2]));
			tooltip.add(ApothSpawnerBlock.concat(SpawnerStats.MAX_NEARBY_ENTITIES.name(), stats[3]));
			tooltip.add(ApothSpawnerBlock.concat(SpawnerStats.REQ_PLAYER_RANGE.name(), stats[4]));
			tooltip.add(ApothSpawnerBlock.concat(SpawnerStats.SPAWN_RANGE.name(), stats[5]));
			if (stats[6] == 1) tooltip.add(SpawnerStats.IGNORE_PLAYERS.name().withStyle(TextFormatting.DARK_GREEN));
			if (stats[7] == 1) tooltip.add(SpawnerStats.IGNORE_CONDITIONS.name().withStyle(TextFormatting.DARK_GREEN));
			if (stats[8] == 1) tooltip.add(SpawnerStats.REDSTONE_CONTROL.name().withStyle(TextFormatting.DARK_GREEN));
			if (stats[9] == 1) tooltip.add(SpawnerStats.IGNORE_LIGHT.name().withStyle(TextFormatting.DARK_GREEN));
			if (stats[10] == 1) tooltip.add(SpawnerStats.NO_AI.name().withStyle(TextFormatting.DARK_GREEN));
		} else tooltip.add(new TranslationTextComponent("misc.apotheosis.ctrl_stats"));
	}

	@Override
	public void appendServerData(CompoundNBT tag, ServerPlayerEntity player, World world, TileEntity te) {
		if (te instanceof ApothSpawnerTile) {
			ApothSpawnerTile spw = (ApothSpawnerTile) te;
			AbstractSpawner logic = spw.getSpawner();
			//Formatter::off
			tag.putIntArray(STATS, 
				new int[] { 
					logic.minSpawnDelay, 
					logic.maxSpawnDelay, 
					logic.spawnCount, 
					logic.maxNearbyEntities, 
					logic.requiredPlayerRange, 
					logic.spawnRange, 
					spw.ignoresPlayers ? 1 : 0, 
					spw.ignoresConditions ? 1 : 0, 
					spw.redstoneControl ? 1 : 0,
					spw.ignoresLight ? 1 : 0, 
					spw.hasNoAI ? 1 : 0
				});
			//Formatter::on
		}
	}

}