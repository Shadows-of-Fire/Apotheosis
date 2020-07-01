package shadows.apotheosis.potion;

import net.minecraft.potion.PotionUtils;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.apotheosis.ApotheosisObjects;

public class PotionModuleClient {

	@SubscribeEvent
	public void colors(ColorHandlerEvent.Item e) {
		e.getItemColors().register((stack, tint) -> {
			return PotionUtils.getColor(stack);
		}, ApotheosisObjects.POTION_CHARM);
	}

}
