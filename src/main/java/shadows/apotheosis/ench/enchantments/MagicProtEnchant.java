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
	public int getMinCost(int lvl) {
		return 40 + (lvl - 1) * 25;
	}

	@Override
	public int getMaxCost(int lvl) {
		return this.getMinCost(lvl + 1);
	}

	@Override
	public int getMaxLevel() {
		return 4;
	}

	@Override
	public int getDamageProtection(int level, DamageSource source) {
		return source.isMagic() && !source.isBypassMagic() ? level * 2 : 0;
	}

	@Override
	public ITextComponent getFullname(int level) {
		return ((IFormattableTextComponent) super.getFullname(level)).withStyle(TextFormatting.DARK_PURPLE);
	}

	@Override
	protected boolean checkCompatibility(Enchantment ench) {
		return ench != this && ench != Enchantments.FIRE_PROTECTION && ench != Enchantments.BLAST_PROTECTION && ench != Enchantments.PROJECTILE_PROTECTION;
	}

}