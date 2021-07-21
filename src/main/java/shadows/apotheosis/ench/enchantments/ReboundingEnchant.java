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
	public int getMinCost(int level) {
		return 30 + level * 13;
	}

	@Override
	public int getMaxCost(int level) {
		return this.getMinCost(level) + 30;
	}

	@Override
	public void doPostHurt(LivingEntity user, Entity attacker, int level) {
		if (attacker != null && user.distanceToSqr(attacker) <= 4D && attacker.canChangeDimensions()) {
			level = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.REBOUNDING, user);
			Vector3d vec = new Vector3d(attacker.getX() - user.getX(), attacker.getY() - user.getY(), attacker.getZ() - user.getZ());
			attacker.push(vec.x * 2 * level, vec.y * 3 * level, vec.z * 2 * level);
		}
	}

}