package shadows.apotheosis.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.math.vector.Vector3d;
import shadows.apotheosis.ApotheosisObjects;

public class ReboundingEnchant extends Enchantment {

	public ReboundingEnchant() {
		super(Rarity.RARE, EnchantmentType.ARMOR, new EquipmentSlotType[] { EquipmentSlotType.CHEST, EquipmentSlotType.LEGS });
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public int getMinEnchantability(int level) {
		return 30 + level * 13;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return this.getMinEnchantability(level) + 30;
	}

	@Override
	public void onUserHurt(LivingEntity user, Entity attacker, int level) {
		if (attacker != null && user.getDistanceSq(attacker) <= 4D && attacker.isNonBoss()) {
			level = EnchantmentHelper.getMaxEnchantmentLevel(ApotheosisObjects.REBOUNDING, user);
			Vector3d vec = new Vector3d(attacker.getPosX() - user.getPosX(), attacker.getPosY() - user.getPosY(), attacker.getPosZ() - user.getPosZ());
			attacker.addVelocity(vec.x * 2 * level, vec.y * 3 * level, vec.z * 2 * level);
		}
	}

}