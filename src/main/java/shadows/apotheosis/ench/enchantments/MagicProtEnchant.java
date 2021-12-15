package shadows.apotheosis.ench.enchantments;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import shadows.apotheosis.ench.EnchModule;

public class MagicProtEnchant extends Enchantment {

	public MagicProtEnchant() {
		super(Rarity.UNCOMMON, EnchantmentCategory.ARMOR, EnchModule.ARMOR);
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
	public Component getFullname(int level) {
		return ((MutableComponent) super.getFullname(level)).withStyle(ChatFormatting.DARK_PURPLE);
	}

	@Override
	protected boolean checkCompatibility(Enchantment ench) {
		return ench != this && ench != Enchantments.FIRE_PROTECTION && ench != Enchantments.BLAST_PROTECTION && ench != Enchantments.PROJECTILE_PROTECTION;
	}

}