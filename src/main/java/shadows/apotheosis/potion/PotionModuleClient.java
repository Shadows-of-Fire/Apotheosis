package shadows.apotheosis.potion;

import net.minecraft.potion.PotionUtils;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import shadows.apotheosis.ApotheosisObjects;

@SuppressWarnings("deprecation")
public class PotionModuleClient {

	@SubscribeEvent
	public void colors(ColorHandlerEvent.Item e) {
		DeferredWorkQueue.runLater(() -> {
			e.getItemColors().register((stack, tint) -> {
				return PotionUtils.getColor(stack);
			}, ApotheosisObjects.POTION_CHARM);
		});
	}

}
