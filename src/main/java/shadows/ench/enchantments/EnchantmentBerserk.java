package shadows.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.potion.PotionEffect;
import shadows.Apotheosis;
import shadows.ench.EnchModule;

public class EnchantmentBerserk extends Enchantment {

	public EnchantmentBerserk() {
		super(Rarity.VERY_RARE, EnumEnchantmentType.ARMOR, new EntityEquipmentSlot[] { EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS });
		setName(Apotheosis.MODID + ".berserk");
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
		return 4;
	}

	@Override
	public boolean isCurse() {
		return true;
	}

	@Override
	public void onUserHurt(EntityLivingBase user, Entity attacker, int level) {
		if (user.getActivePotionEffect(MobEffects.RESISTANCE) == null) {
			level = EnchantmentHelper.getMaxEnchantmentLevel(EnchModule.BERSERK, user);
			user.attackEntityFrom(EnchModule.CORRUPTED, level * level);
			user.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 200 * level, level - 1));
			user.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 200 * level, level - 1));
			user.addPotionEffect(new PotionEffect(MobEffects.SPEED, 200 * level, level - 1));
		}
	}

}
