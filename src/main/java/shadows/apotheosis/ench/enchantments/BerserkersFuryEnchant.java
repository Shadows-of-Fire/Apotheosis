package shadows.apotheosis.ench.enchantments;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import shadows.apotheosis.ench.EnchModule;

public class BerserkersFuryEnchant extends Enchantment {

	public BerserkersFuryEnchant() {
		super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR, new EquipmentSlot[] { EquipmentSlot.CHEST, EquipmentSlot.LEGS });
	}

	@Override
	public int getMinCost(int level) {
		return 40 + level * 30;
	}

	@Override
	public int getMaxCost(int level) {
		return this.getMinCost(level) + 40;
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
	public Component getFullname(int level) {
		return ((MutableComponent) super.getFullname(level)).withStyle(ChatFormatting.DARK_RED);
	}

	/**
	 * Handles the application of Berserker's Fury.
	 */
	public void livingHurt(LivingHurtEvent e) {
		LivingEntity user = e.getEntityLiving();
		if (e.getSource().getEntity() instanceof Entity && user.getEffect(MobEffects.DAMAGE_RESISTANCE) == null) {
			int level = EnchantmentHelper.getEnchantmentLevel(this, user);
			if (level > 0) {
				user.invulnerableTime = 0;
				user.hurt(EnchModule.CORRUPTED, level * level);
				user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200 * level, level - 1));
				user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200 * level, level - 1));
				user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200 * level, level - 1));
			}
		}
	}

}