package shadows.apotheosis.spawn.compat;

import java.util.List;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.spawn.spawner.ApothSpawnerBlock;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;

@WailaPlugin
public class SpawnerHwylaPlugin implements IWailaPlugin, IComponentProvider, IServerDataProvider<BlockEntity> {

	public static final String STATS = "spw_stats";

	@Override
	public void register(IRegistrar reg) {
		if (!Apotheosis.enableSpawner) return;
		reg.registerComponentProvider(this, TooltipPosition.BODY, ApothSpawnerBlock.class);
		reg.registerBlockDataProvider(this, ApothSpawnerBlock.class);
	}

	@Override
	public void appendBody(List<Component> tooltip, IDataAccessor accessor, IPluginConfig config) {
		if (Minecraft.getInstance().options.keyShift.isDown()) {
			int[] stats = accessor.getServerData().getIntArray(STATS);
			if (stats.length != 10) return;
			tooltip.add(new TranslatableComponent("waila.spw.mindelay", stats[0]));
			tooltip.add(new TranslatableComponent("waila.spw.maxdelay", stats[1]));
			tooltip.add(new TranslatableComponent("waila.spw.spawncount", stats[2]));
			tooltip.add(new TranslatableComponent("waila.spw.maxnearby", stats[3]));
			tooltip.add(new TranslatableComponent("waila.spw.playerrange", stats[4]));
			tooltip.add(new TranslatableComponent("waila.spw.spawnrange", stats[5]));
			if (stats[6] == 1) tooltip.add(new TranslatableComponent("waila.spw.ignoreplayers"));
			if (stats[7] == 1) tooltip.add(new TranslatableComponent("waila.spw.ignoreconditions"));
			if (stats[8] == 1) tooltip.add(new TranslatableComponent("waila.spw.ignorecap"));
			if (stats[9] == 1) tooltip.add(new TranslatableComponent("waila.spw.redstone"));
		} else tooltip.add(new TranslatableComponent("waila.spw.sneak"));
	}

	@Override
	public void appendServerData(CompoundTag tag, ServerPlayer player, Level world, BlockEntity te) {
		if (te instanceof ApothSpawnerTile) {
			ApothSpawnerTile spw = (ApothSpawnerTile) te;
			BaseSpawner logic = spw.getSpawner();
			tag.putIntArray(STATS, new int[] { logic.minSpawnDelay, logic.maxSpawnDelay, logic.spawnCount, logic.maxNearbyEntities, logic.requiredPlayerRange, logic.spawnRange, spw.ignoresPlayers ? 1 : 0, spw.ignoresConditions ? 1 : 0, spw.ignoresCap ? 1 : 0, spw.redstoneEnabled ? 1 : 0 });
		}
	}

}