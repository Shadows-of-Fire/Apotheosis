package shadows.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.DamageSource;
import shadows.Apotheosis;
import shadows.ench.EnchModule;

public class EnchantmentLifeMend extends Enchantment {

	public EnchantmentLifeMend() {
		super(Rarity.VERY_RARE, EnumEnchantmentType.BREAKABLE, EntityEquipmentSlot.values());
		setName(Apotheosis.MODID + ".life_mending");
	}

	@Override
	public int getMinEnchantability(int level) {
		return 80 + level * 15;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return getMinEnchantability(level) + 50;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public boolean isCurse() {
		return true;
	}

	@Override
	public void onEntityDamaged(EntityLivingBase user, Entity target, int level) {
		if (target instanceof EntityLivingBase) {
			user.getHeldItemMainhand().damageItem(35, user);
			DamageSource src = user instanceof EntityPlayer ? DamageSource.causePlayerDamage((EntityPlayer) user) : DamageSource.GENERIC;
			((EntityLivingBase) target).attackEntityFrom(src, EnchModule.localAtkStrength * 2.35F * level);
		}
	}

}
