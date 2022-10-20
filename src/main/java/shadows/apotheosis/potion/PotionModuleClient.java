package shadows.apotheosis.potion;

import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.apotheosis.Apoth;

public class PotionModuleClient {

	@SubscribeEvent
	public void colors(RegisterColorHandlersEvent.Item e) {
		e.register((stack, tint) -> PotionUtils.getColor(stack), Apoth.Items.POTION_CHARM.get());
	}

}