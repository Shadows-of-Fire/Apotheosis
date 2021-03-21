package shadows.apotheosis.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import shadows.apotheosis.ench.EnchModule;

public class MagicProtEnchant extends Enchantment {

	public MagicProtEnchant() {
		super(Rarity.UNCOMMON, EnchantmentType.ARMOR, EnchModule.ARMOR);
	}

	@Override
	public int getMinEnchantability(int lvl) {
		return 40 + (lvl - 1) * 25;
	}

	@Override
	public int getMaxEnchantability(int lvl) {
		return this.getMinEnchantability(lvl + 1);
	}

	@Override
	public int getMaxLevel() {
		return 4;
	}

	@Override
	public int calcModifierDamage(int level, DamageSource source) {
		return source.isMagicDamage() && !source.isDamageAbsolute() ? level * 2 : 0;
	}

	@Override
	public ITextComponent getDisplayName(int level) {
		return ((IFormattableTextComponent) super.getDisplayName(level)).mergeStyle(TextFormatting.DARK_PURPLE);
	}

	@Override
	protected boolean canApplyTogether(Enchantment ench) {
		return ench != this && ench != Enchantments.FIRE_PROTECTION && ench != Enchantments.BLAST_PROTECTION && ench != Enchantments.PROJECTILE_PROTECTION;
	}

}