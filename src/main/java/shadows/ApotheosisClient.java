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
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IRegistryDelegate;
import shadows.Apotheosis.ApotheosisInit;
import shadows.ench.EnchModule;
import shadows.placebo.Placebo;
import shadows.placebo.util.PlaceboUtil;
import shadows.potion.PotionModule;
import shadows.spawn.SpawnerModule;

@EventBusSubscriber(modid = Apotheosis.MODID, value = Side.CLIENT)
public class ApotheosisClient {

	public static final Item COBWEB = Item.getItemFromBlock(Blocks.WEB);

	private static final Map<IRegistryDelegate<Enchantment>, List<String>> ENCH_TOOLTIPS = new HashMap<>();

	@SubscribeEvent
	public static void tooltips(ItemTooltipEvent e) {
		Item i = e.getItemStack().getItem();
		if (i == COBWEB) e.getToolTip().add(I18n.format("info.apotheosis.cobweb"));
		else if (i == EnchModule.PRISMATIC_COBWEB) e.getToolTip().add(I18n.format("info.apotheosis.prismatic_cobweb"));
		else if (i == Items.ENCHANTED_BOOK) {
			for (Map.Entry<IRegistryDelegate<Enchantment>, List<String>> ent : ENCH_TOOLTIPS.entrySet()) {
				if (onlyHasEnchant(e.getItemStack(), ent.getKey().get())) {
					ent.getValue().forEach(s -> e.getToolTip().add(I18n.format(s)));
					return;
				}
			}
		}
	}

	private static boolean onlyHasEnchant(ItemStack book, Enchantment ench) {
		NBTTagList list = ItemEnchantedBook.getEnchantments(book);
		if (list.tagCount() == 1) {
			NBTTagCompound tag = list.getCompoundTagAt(0);
			int id = tag.getShort("id");
			Enchantment enchantment = Enchantment.getEnchantmentByID(id);
			if (enchantment == ench) return true;
		}
		return false;
	}

	@SubscribeEvent
	public static void init(ApotheosisInit e) {
		String masterwork = TextFormatting.DARK_GREEN + I18n.format("info.apotheosis.masterwork");
		String twisted = TextFormatting.DARK_PURPLE + I18n.format("info.apotheosis.twisted");
		String corrupted = TextFormatting.DARK_RED + I18n.format("info.apotheosis.corrupted");
		if (Apotheosis.enchTooltips) {
			if (Apotheosis.enableSpawner) registerTooltip(SpawnerModule.CAPTURING, "enchantment.apotheosis.capturing.desc");
			if (Apotheosis.enablePotion) registerTooltip(PotionModule.TRUE_INFINITY, masterwork, "", "enchantment.apotheosis.true_infinity.desc");
			if (Apotheosis.enableEnch) {
				registerTooltip(EnchModule.HELL_INFUSION, masterwork, "", "enchantment.apotheosis.hell_infusion.desc");
				registerTooltip(EnchModule.MOUNTED_STRIKE, "enchantment.apotheosis.mounted_strike.desc");
				registerTooltip(EnchModule.DEPTH_MINER, twisted, "", "enchantment.apotheosis.depth_miner.desc");
				registerTooltip(EnchModule.STABLE_FOOTING, "enchantment.apotheosis.stable_footing.desc");
				registerTooltip(EnchModule.SCAVENGER, masterwork, "", "enchantment.apotheosis.scavenger.desc");
				registerTooltip(EnchModule.LIFE_MENDING, corrupted, "", "enchantment.apotheosis.life_mending.desc");
				registerTooltip(EnchModule.ICY_THORNS, "enchantment.apotheosis.icy_thorns.desc");
				registerTooltip(EnchModule.TEMPTING, "enchantment.apotheosis.tempting.desc");
				registerTooltip(EnchModule.SHIELD_BASH, "enchantment.apotheosis.shield_bash.desc");
				registerTooltip(EnchModule.REFLECTIVE, "enchantment.apotheosis.reflective.desc");
			}
		} else {
			if (Apotheosis.enablePotion) registerTooltip(PotionModule.TRUE_INFINITY, masterwork);
			if (Apotheosis.enableEnch) {
				registerTooltip(EnchModule.HELL_INFUSION, masterwork);
				registerTooltip(EnchModule.DEPTH_MINER, twisted);
				registerTooltip(EnchModule.SCAVENGER, masterwork);
				registerTooltip(EnchModule.LIFE_MENDING, corrupted);
			}
		}
	}

	@SubscribeEvent
	public static void models(ModelRegistryEvent e) {
		if (Apotheosis.enableEnch) {
			Placebo.PROXY.useRenamedMapper(EnchModule.HELLSHELF, "hellshelf", "", "normal");
			PlaceboUtil.sMRL(EnchModule.PRISMATIC_COBWEB, 0, "inventory");
		}
	}

	public static void registerTooltip(Enchantment e, String... keys) {
		List<String> tips = ENCH_TOOLTIPS.computeIfAbsent(e.delegate, d -> new ArrayList<>());
		for (String s : keys)
			tips.add(s);
	}

}
