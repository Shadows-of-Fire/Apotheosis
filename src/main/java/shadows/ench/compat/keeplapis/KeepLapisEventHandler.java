package shadows.ench.compat.keeplapis;

import net.crazysnailboy.mods.enchantingtable.inventory.ContainerEnchantment;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class KeepLapisEventHandler {

	@SubscribeEvent
	public static void enchContainer(PlayerContainerEvent.Open e) {
		if (!e.getEntityPlayer().world.isRemote && e.getContainer().getClass() == ContainerEnchantment.class) {
			ContainerEnchantment old = (ContainerEnchantment) e.getContainer();
			KeepLapisContainerExt newC = new KeepLapisContainerExt(e.getEntityPlayer().inventory, old.world, old.position);
			newC.windowId = old.windowId;
			newC.addListener((EntityPlayerMP) e.getEntityPlayer());
			e.getEntityPlayer().openContainer = newC;
		}
	}

}
