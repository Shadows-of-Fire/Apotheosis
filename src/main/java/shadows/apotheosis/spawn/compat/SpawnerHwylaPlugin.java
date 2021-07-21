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
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.spawn.spawner.ApothSpawnerBlock;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;

@WailaPlugin
public class SpawnerHwylaPlugin implements IWailaPlugin, IComponentProvider, IServerDataProvider<TileEntity> {

	public static final String STATS = "spw_stats";

	@Override
	public void register(IRegistrar reg) {
		if (!Apotheosis.enableSpawner) return;
		reg.registerComponentProvider(this, TooltipPosition.BODY, ApothSpawnerBlock.class);
		reg.registerBlockDataProvider(this, ApothSpawnerBlock.class);
	}

	@Override
	public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
		if (Minecraft.getInstance().options.keyShift.isDown()) {
			int[] stats = accessor.getServerData().getIntArray(STATS);
			if (stats.length != 10) return;
			tooltip.add(new TranslationTextComponent("waila.spw.mindelay", stats[0]));
			tooltip.add(new TranslationTextComponent("waila.spw.maxdelay", stats[1]));
			tooltip.add(new TranslationTextComponent("waila.spw.spawncount", stats[2]));
			tooltip.add(new TranslationTextComponent("waila.spw.maxnearby", stats[3]));
			tooltip.add(new TranslationTextComponent("waila.spw.playerrange", stats[4]));
			tooltip.add(new TranslationTextComponent("waila.spw.spawnrange", stats[5]));
			if (stats[6] == 1) tooltip.add(new TranslationTextComponent("waila.spw.ignoreplayers"));
			if (stats[7] == 1) tooltip.add(new TranslationTextComponent("waila.spw.ignoreconditions"));
			if (stats[8] == 1) tooltip.add(new TranslationTextComponent("waila.spw.ignorecap"));
			if (stats[9] == 1) tooltip.add(new TranslationTextComponent("waila.spw.redstone"));
		} else tooltip.add(new TranslationTextComponent("waila.spw.sneak"));
	}

	@Override
	public void appendServerData(CompoundNBT tag, ServerPlayerEntity player, World world, TileEntity te) {
		if (te instanceof ApothSpawnerTile) {
			ApothSpawnerTile spw = (ApothSpawnerTile) te;
			AbstractSpawner logic = spw.getSpawner();
			tag.putIntArray(STATS, new int[] { logic.minSpawnDelay, logic.maxSpawnDelay, logic.spawnCount, logic.maxNearbyEntities, logic.requiredPlayerRange, logic.spawnRange, spw.ignoresPlayers ? 1 : 0, spw.ignoresConditions ? 1 : 0, spw.ignoresCap ? 1 : 0, spw.redstoneEnabled ? 1 : 0 });
		}
	}

}