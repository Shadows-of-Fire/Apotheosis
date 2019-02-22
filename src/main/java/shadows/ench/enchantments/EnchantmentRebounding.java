package shadows.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.Vec3d;
import shadows.Apotheosis;
import shadows.ApotheosisObjects;

public class EnchantmentRebounding extends Enchantment {

	public EnchantmentRebounding() {
		super(Rarity.RARE, EnumEnchantmentType.ARMOR, new EntityEquipmentSlot[] { EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS });
		setName(Apotheosis.MODID + ".rebounding");
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public int getMinEnchantability(int level) {
		return 30 + 7 * level;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return getMinEnchantability(level) + 30;
	}

	@Override
	public void onUserHurt(EntityLivingBase user, Entity attacker, int level) {
		if (user.getDistanceSq(attacker) <= 4D) {
			level = EnchantmentHelper.getMaxEnchantmentLevel(ApotheosisObjects.REBOUNDING, user);
			Vec3d vec = new Vec3d(attacker.posX - user.posX, attacker.posY - user.posY, attacker.posZ - user.posZ);
			attacker.addVelocity(vec.x * 2 * level, vec.y * 3 * level, vec.z * 2 * level);
		}
	}

}
