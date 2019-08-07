package shadows.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.util.DamageSource;
import shadows.ench.EnchModule;

public class EnchantmentMagicProt extends Enchantment {

	public EnchantmentMagicProt() {
		super(Rarity.UNCOMMON, EnchantmentType.ARMOR, EnchModule.ARMOR);
	}

	@Override
	public int getMinEnchantability(int lvl) {
		return 40 + (lvl - 1) * 25;
	}

	@Override
	public int getMaxEnchantability(int lvl) {
		return getMinEnchantability(lvl + 1);
	}

	@Override
	public int getMaxLevel() {
		return 4;
	}

	@Override
	public int calcModifierDamage(int level, DamageSource source) {
		return source.isMagicDamage() && !source.isDamageAbsolute() ? level * 2 : 0;
	}

}
