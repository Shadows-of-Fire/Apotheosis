package shadows.ench.objects;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.item.BookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AnvilUpdateEvent;
import shadows.Apotheosis;
import shadows.ench.EnchModule;

public class ItemTypedBook extends BookItem {

	final ItemStack rep;
	final EnchantmentType type;

	public ItemTypedBook(Item rep, EnchantmentType type) {
		super(new Item.Properties().group(ItemGroup.MISC));
		this.type = type;
		this.rep = new ItemStack(rep);
		this.setRegistryName(Apotheosis.MODID, (type == null ? "null" : type.name().toLowerCase(Locale.ROOT)) + "_book");
		EnchModule.TYPED_BOOKS.add(this);
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return stack.getCount() == 1;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		if (type == null) return EnchModule.TYPED_BOOKS.stream().filter(b -> b != this).allMatch(b -> !enchantment.canApply(new ItemStack(b)));
		return enchantment.type == type || enchantment.canApplyAtEnchantingTable(rep);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent("info.apotheosis." + getRegistryName().getPath()));
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return !stack.isEnchanted() ? super.getRarity(stack) : Rarity.UNCOMMON;
	}

	public static boolean updateAnvil(AnvilUpdateEvent ev) {
		ItemStack book = ev.getRight();
		ItemStack weapon = ev.getLeft();
		if (!(book.getItem() instanceof BookItem) || !book.isEnchanted() || !weapon.getItem().isEnchantable(weapon)) return false;
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
				if (!isCompat) continue;
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
			return true;
		}
		return false;
	}

}
