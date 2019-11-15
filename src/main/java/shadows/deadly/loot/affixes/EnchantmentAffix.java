package shadows.deadly.loot.affixes;

import java.util.Map;
import java.util.Random;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import shadows.deadly.loot.AffixModifier;

public class EnchantmentAffix extends Affix {

	protected final Enchantment ench;
	protected final int level;

	public EnchantmentAffix(Enchantment ench, int level, int weight) {
		super(weight);
		this.ench = ench;
		this.level = level;
	}

	@Override
	public float apply(ItemStack stack, Random rand, AffixModifier modifier) {
		Map<Enchantment, Integer> enchMap = EnchantmentHelper.getEnchantments(stack);
		enchMap.put(ench, level);
		EnchantmentHelper.setEnchantments(enchMap, stack);
		return 0;
	}

}
