package shadows.apotheosis.deadly.loot.affix.impl;

import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import shadows.apotheosis.deadly.loot.AffixModifier;
import shadows.apotheosis.deadly.loot.affix.Affix;

//TODO: Revise if epics will gain something more useful than just a phat enchantment.
public class EnchantmentAffix extends Affix {

	protected Enchantment ench;
	protected Supplier<Enchantment> enchSup;
	protected final int level;

	public EnchantmentAffix(Enchantment ench, int level, boolean prefix, int weight) {
		super(prefix, weight);
		this.ench = ench;
		this.level = level;
	}

	public EnchantmentAffix(Supplier<Enchantment> ench, int level, boolean prefix, int weight) {
		super(prefix, weight);
		this.ench = null;
		this.enchSup = ench;
		this.level = level;
	}

	@Override
	public float apply(ItemStack stack, Random rand, AffixModifier modifier) {
		Map<Enchantment, Integer> enchMap = EnchantmentHelper.getEnchantments(stack);
		if (ench == null) ench = enchSup.get();
		int lvl = level;
		if (modifier != null) lvl = (int) Math.max(level, modifier.editLevel(lvl));
		enchMap.put(ench, lvl);
		EnchantmentHelper.setEnchantments(enchMap, stack);
		return lvl;
	}

}
