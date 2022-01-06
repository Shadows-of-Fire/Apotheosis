package shadows.apotheosis.potion;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import shadows.apotheosis.Apoth;

public class PotionModuleClient {

	@SubscribeEvent
	public void colors(FMLClientSetupEvent e) {
		e.enqueueWork(() -> {
			Minecraft.getInstance().getItemColors().register((stack, tint) -> PotionUtils.getColor(stack), Apoth.Items.POTION_CHARM);
		});
	}

}