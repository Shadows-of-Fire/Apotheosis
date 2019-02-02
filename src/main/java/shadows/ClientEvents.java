package shadows;

import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(modid = Apotheosis.MODID, value = Side.CLIENT)
public class ClientEvents {

	public static final Item COBWEB = Item.getItemFromBlock(Blocks.WEB);

	@SubscribeEvent
	public static void tooltips(ItemTooltipEvent e) {
		if (e.getItemStack().getItem() == COBWEB) e.getToolTip().add(I18n.format("info.apotheosis.cobweb"));
	}

}
