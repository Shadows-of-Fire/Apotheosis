package shadows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IRegistryDelegate;
import shadows.Apotheosis.ApotheosisInit;
import shadows.ench.EnchModule;
import shadows.potion.PotionModule;
import shadows.spawn.SpawnerModule;

@EventBusSubscriber(modid = Apotheosis.MODID, value = Side.CLIENT)
public class ApotheosisClient {

	public static final Item COBWEB = Item.getItemFromBlock(Blocks.WEB);

	private static final Map<IRegistryDelegate<Enchantment>, List<String>> ENCH_TOOLTIPS = new HashMap<>();

	@SubscribeEvent
	public static void tooltips(ItemTooltipEvent e) {
		if (e.getItemStack().getItem() == COBWEB) e.getToolTip().add(I18n.format("info.apotheosis.cobweb"));
		else if (e.getItemStack().getItem() == Items.ENCHANTED_BOOK) {
			for (Map.Entry<IRegistryDelegate<Enchantment>, List<String>> ent : ENCH_TOOLTIPS.entrySet()) {
				if (hasEnchant(e.getItemStack(), ent.getKey().get())) {
					ent.getValue().forEach(s -> e.getToolTip().add(I18n.format(s)));
					return;
				}
			}
		}
	}

	private static boolean hasEnchant(ItemStack book, Enchantment ench) {
		NBTTagList list = ItemEnchantedBook.getEnchantments(book);
		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			int id = tag.getShort("id");
			Enchantment enchantment = Enchantment.getEnchantmentByID(id);
			if (enchantment == ench) return true;
		}
		return false;
	}

	@SubscribeEvent
	public static void init(ApotheosisInit e) {
		String masterwork = TextFormatting.RED + I18n.format("info.apotheosis.masterwork");
		if (Apotheosis.enableSpawner) registerTooltip(SpawnerModule.CAPTURING, "info.spw.capturing");
		if (Apotheosis.enablePotion) registerTooltip(PotionModule.TRUE_INFINITY, masterwork, "", "info.apotheosis.true_infinity");
		if (Apotheosis.enableEnch) registerTooltip(EnchModule.HELL_INFUSION, masterwork, "", "info.apotheosis.hell_infusion");
	}

	public static void registerTooltip(Enchantment e, String... keys) {
		List<String> tips = ENCH_TOOLTIPS.computeIfAbsent(e.delegate, d -> new ArrayList<>());
		for (String s : keys)
			tips.add(s);
	}

}
