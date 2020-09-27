package shadows.apotheosis.potion;

import net.minecraft.client.Minecraft;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import shadows.apotheosis.Apotheosis.ApotheosisClientSetup;
import shadows.apotheosis.ApotheosisObjects;

@SuppressWarnings("deprecation")
public class PotionModuleClient {

	@SubscribeEvent
	public void colors(ApotheosisClientSetup e) {
		DeferredWorkQueue.runLater(() -> {
			Minecraft.getInstance().getItemColors().register((stack, tint) -> {
				return PotionUtils.getColor(stack);
			}, ApotheosisObjects.POTION_CHARM);
		});
	}

}