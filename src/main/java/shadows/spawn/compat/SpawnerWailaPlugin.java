package shadows.spawn.compat;

import java.util.List;

import org.lwjgl.input.Keyboard;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import shadows.Apotheosis;
import shadows.spawn.BlockSpawnerExt;
import shadows.spawn.TileSpawnerExt;

@WailaPlugin
public class SpawnerWailaPlugin implements IWailaPlugin, IWailaDataProvider {

	public static final String STATS = "spw_stats";

	@Override
	public void register(IWailaRegistrar reg) {
		if (!Apotheosis.enableSpawner) return;
		reg.registerBodyProvider(this, BlockSpawnerExt.class);
		reg.registerNBTProvider(this, BlockSpawnerExt.class);
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
		if (Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode())) {
			int[] stats = accessor.getNBTData().getIntArray(STATS);
			if (stats.length != 10) return tooltip;
			tooltip.add(I18n.format("waila.spw.mindelay", stats[0]));
			tooltip.add(I18n.format("waila.spw.maxdelay", stats[1]));
			tooltip.add(I18n.format("waila.spw.spawncount", stats[2]));
			tooltip.add(I18n.format("waila.spw.maxnearby", stats[3]));
			tooltip.add(I18n.format("waila.spw.playerrange", stats[4]));
			tooltip.add(I18n.format("waila.spw.spawnrange", stats[5]));
			if (stats[6] == 1) tooltip.add(I18n.format("waila.spw.ignoreplayers"));
			if (stats[7] == 1) tooltip.add(I18n.format("waila.spw.ignoreconditions"));
			if (stats[8] == 1) tooltip.add(I18n.format("waila.spw.ignorecap"));
			if (stats[9] == 1) tooltip.add(I18n.format("waila.spw.redstone"));
		} else tooltip.add(I18n.format("waila.spw.sneak"));
		return tooltip;
	}

	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
		if (te instanceof TileSpawnerExt) {
			TileSpawnerExt spw = (TileSpawnerExt) te;
			MobSpawnerBaseLogic logic = spw.getSpawnerBaseLogic();
			tag.setIntArray(STATS, new int[] { logic.minSpawnDelay, logic.maxSpawnDelay, logic.spawnCount, logic.maxNearbyEntities, logic.activatingRangeFromPlayer, logic.spawnRange, spw.ignoresPlayers ? 1 : 0, spw.ignoresConditions ? 1 : 0, spw.ignoresCap ? 1 : 0, spw.redstoneEnabled ? 1 : 0 });
		}
		return tag;
	}

}
