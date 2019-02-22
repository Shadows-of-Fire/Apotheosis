package shadows.ench;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import shadows.Apotheosis;

public class ItemTypedBook extends ItemBook {

	final ItemStack rep;
	final EnumEnchantmentType type;

	public ItemTypedBook(Item rep, EnumEnchantmentType type) {
		this.type = type;
		this.rep = new ItemStack(rep);
		this.setRegistryName(Apotheosis.MODID, (type == null ? "null" : type.name().toLowerCase(Locale.ROOT)) + "_book");
		this.setTranslationKey(Apotheosis.MODID + "." + getRegistryName().getPath());
		EnchModule.TYPED_BOOKS.add(this);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return enchantment.type == type || enchantment.canApplyAtEnchantingTable(rep);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(I18n.format("info.apotheosis." + getRegistryName().getPath()));
	}

	public static void updateAnvil(AnvilUpdateEvent ev) {
		ItemStack book = ev.getRight();
		ItemStack weapon = ev.getLeft();
		if (!book.isItemEnchanted() || !weapon.getItem().isEnchantable(weapon)) return;
		Map<Enchantment, Integer> bookEnch = EnchantmentHelper.getEnchantments(book);
		Map<Enchantment, Integer> wepEnch = EnchantmentHelper.getEnchantments(weapon);
		int cost = 0;

		for (Enchantment ench : bookEnch.keySet()) {
			if (ench == null) continue;

			int level = bookEnch.containsKey(ench) ? bookEnch.get(ench) : 0;
			int curLevel = wepEnch.containsKey(ench) ? wepEnch.get(ench) : 0;
			if (level > 0 && level == curLevel) level = Math.min(EnchModule.getEnchInfo(ench).getMaxLevel(), level + 1);
			if (curLevel > level) level = curLevel;

			if (ench.canApply(weapon)) {
				boolean isCompat = true;
				for (Enchantment ench2 : wepEnch.keySet()) {
					if (ench != ench2 && !ench.isCompatibleWith(ench2)) isCompat = false;
				}
				if (!isCompat) return;
				wepEnch.put(ench, level);
				int addition = 0;
				switch (ench.getRarity()) {
				case COMMON:
					addition += 1 * level;
					break;
				case UNCOMMON:
					addition += 2 * level;
					break;
				case RARE:
					addition += 4 * level;
					break;
				case VERY_RARE:
					addition += 8 * level;
				}
				cost += Math.max(1, addition / 2);
			}
		}
		if (cost > 0) {
			cost += weapon.getRepairCost();
			ItemStack out = weapon.copy();
			out.setRepairCost(weapon.getRepairCost() * 2 + 1);
			EnchantmentHelper.setEnchantments(wepEnch, out);
			ev.setMaterialCost(1);
			ev.setCost(cost);
			ev.setOutput(out);
		}
	}

}
