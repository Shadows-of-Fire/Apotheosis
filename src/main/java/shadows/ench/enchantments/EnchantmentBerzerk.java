package shadows.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.potion.PotionEffect;
import shadows.Apotheosis;
import shadows.ench.EnchModule;

public class EnchantmentBerzerk extends Enchantment {

	public EnchantmentBerzerk() {
		super(Rarity.VERY_RARE, EnumEnchantmentType.ARMOR, new EntityEquipmentSlot[] { EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS });
		setName(Apotheosis.MODID + ".berzerk");
	}

	@Override
	public int getMinEnchantability(int level) {
		return 60 + level * 15;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return getMinEnchantability(level) + 40;
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
	public void onUserHurt(EntityLivingBase user, Entity attacker, int level) {
		if (user.getActivePotionEffect(MobEffects.RESISTANCE) == null) {
			user.attackEntityFrom(EnchModule.CORRUPTED, level * level * 0.7F);
			user.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 200, 1));
			user.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 200, 1));
			user.addPotionEffect(new PotionEffect(MobEffects.SPEED, 200, 1));
		}
	}

}
