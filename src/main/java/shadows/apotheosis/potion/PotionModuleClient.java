package shadows.apotheosis.potion;

import net.minecraft.client.Minecraft;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import shadows.apotheosis.ApotheosisObjects;

public class PotionModuleClient {

	@SubscribeEvent
	public void colors(FMLClientSetupEvent e) {
		e.enqueueWork(() -> {
			Minecraft.getInstance().getItemColors().register((stack, tint) -> PotionUtils.getColor(stack), ApotheosisObjects.POTION_CHARM);
		});
	}

}