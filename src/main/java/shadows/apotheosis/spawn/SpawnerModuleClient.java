package shadows.apotheosis.spawn;

import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SpawnerModuleClient {

	@SubscribeEvent
	public void handleTooltips(ItemTooltipEvent e) {
		ItemStack s = e.getItemStack();
		if (s.getItem() instanceof SpawnEggItem) {
			SpawnEggItem egg = (SpawnEggItem) s.getItem();
			EntityType<?> type = egg.getType(s.getTag());
			if (SpawnerModule.bannedMobs.contains(type.getRegistryName())) e.getToolTip().add(new TranslationTextComponent("misc.apotheosis.banned").withStyle(TextFormatting.GRAY));
		}
	}

}
