package shadows;

import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import shadows.spawn.SpawnerModule;

@EventBusSubscriber(modid = Apotheosis.MODID, value = Side.CLIENT)
public class ClientEvents {

	public static final Item COBWEB = Item.getItemFromBlock(Blocks.WEB);

	@SubscribeEvent
	public static void tooltips(ItemTooltipEvent e) {
		if (e.getItemStack().getItem() == COBWEB) e.getToolTip().add(I18n.format("info.apotheosis.cobweb"));
		if (Apotheosis.enableSpawner && e.getItemStack().getItem() == Items.ENCHANTED_BOOK && hasCapturing(e.getItemStack())) {
			e.getToolTip().add(I18n.format("info.spw.capturing"));
		}
	}

	private static boolean hasCapturing(ItemStack book) {
		NBTTagList list = ItemEnchantedBook.getEnchantments(book);
		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			int id = tag.getShort("id");
			Enchantment enchantment = Enchantment.getEnchantmentByID(id);
			if (enchantment == SpawnerModule.CAPTURING) return true;
		}
		return false;
	}

}
