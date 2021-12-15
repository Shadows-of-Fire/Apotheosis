package shadows.apotheosis.ench.replacements;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import shadows.apotheosis.ApotheosisObjects;

public class DefenseEnchant extends ProtectionEnchantment {

	public DefenseEnchant(Rarity rarity, Type type, EquipmentSlot... slots) {
		super(rarity, type, slots);
	}

	@Override
	public int getDamageProtection(int level, DamageSource source) {
		if (source.isBypassInvul()) {
			return 0;
		} else if (this.type == ProtectionEnchantment.Type.ALL) {
			return level;
		} else if (this.type == ProtectionEnchantment.Type.FIRE && source.isFire()) {
			return level;
		} else if (this.type == ProtectionEnchantment.Type.FALL && source == DamageSource.FALL) {
			return level * 3;
		} else if (this.type == ProtectionEnchantment.Type.EXPLOSION && source.isExplosion()) {
			return level * 2;
		} else {
			return this.type == ProtectionEnchantment.Type.PROJECTILE && source.isProjectile() ? level : 0;
		}
	}

	/**
	 * Determines if the enchantment passed can be applyied together with this enchantment.
	 */
	@Override
	public boolean checkCompatibility(Enchantment ench) {
		if (this == Enchantments.FALL_PROTECTION) return ench != this;
		if (this == Enchantments.ALL_DAMAGE_PROTECTION) return ench != this;
		if (ench instanceof ProtectionEnchantment) {
			ProtectionEnchantment pEnch = (ProtectionEnchantment) ench;
			if (ench == this) return false;
			return pEnch.type == Type.ALL || pEnch.type == Type.FALL;
		}
		if (ench == ApotheosisObjects.MAGIC_PROTECTION) return false;
		return ench != this;
	}

}