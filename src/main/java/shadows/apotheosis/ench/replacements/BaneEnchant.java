package shadows.apotheosis.ench.replacements;

import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

public class BaneEnchant extends DamageEnchantment {

	protected final CreatureAttribute attrib;

	public BaneEnchant(Enchantment.Rarity rarity, CreatureAttribute attrib, EquipmentSlotType... slots) {
		super(rarity, 0, slots);
		this.attrib = attrib;
	}

	@Override
	public int getMinEnchantability(int level) {
		if (this.attrib == CreatureAttribute.UNDEFINED) return 1 + (level - 1) * 11;
		return 5 + (level - 1) * 8;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return getMinEnchantability(level) + 20;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public float calcDamageByCreature(int level, CreatureAttribute attrib) {
		if (this.attrib == CreatureAttribute.UNDEFINED) return 1 + level * 0.5F;
		if (this.attrib == attrib) return level * 1.5F;
		return 0;
	}

	@Override
	public boolean canApplyTogether(Enchantment ench) {
		if (this.attrib == CreatureAttribute.UNDEFINED) return ench != this;
		return ench == Enchantments.SHARPNESS ? ench != this : !(ench instanceof BaneEnchant);
	}

	/**
	* Called whenever a mob is damaged with an item that has this enchantment on it.
	*/
	@Override
	public void onEntityDamaged(LivingEntity p_151368_1_, Entity p_151368_2_, int p_151368_3_) {
		if (p_151368_2_ instanceof LivingEntity) {
			LivingEntity livingentity = (LivingEntity) p_151368_2_;
			if (this.attrib != CreatureAttribute.UNDEFINED && livingentity.getCreatureAttribute() == this.attrib) {
				int i = 20 + p_151368_1_.getRNG().nextInt(10 * p_151368_3_);
				livingentity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, i, 3));
			}
		}

	}
}