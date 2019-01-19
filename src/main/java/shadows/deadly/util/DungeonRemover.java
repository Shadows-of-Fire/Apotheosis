package shadows.deadly.util;

import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import shadows.deadly.config.DeadlyConfig;

/**
 * Removes normal dungeons to pave the way for DeadlyWorld dungeons.
 * @author FatherToast
 *
 */
public class DungeonRemover {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onChunkPopulate(PopulateChunkEvent.Populate event) {
		if (DeadlyConfig.removeDungeons && event.getType() == PopulateChunkEvent.Populate.EventType.DUNGEON && !event.getWorld().isRemote) {
			event.setResult(Event.Result.DENY);
		}
	}
}