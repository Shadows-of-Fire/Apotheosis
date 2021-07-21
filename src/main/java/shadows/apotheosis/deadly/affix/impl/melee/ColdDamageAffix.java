package shadows.apotheosis.deadly.affix.impl.melee;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.attributes.CustomAttributes;
import shadows.apotheosis.deadly.affix.impl.AttributeAffix;

/**
 * Adds cold damage, and slows hit enemies.
 */
public class ColdDamageAffix extends AttributeAffix {

	public static final DamageSource COLD = new DamageSource("apoth.frozen_solid").setMagic().bypassMagic();

	public ColdDamageAffix(int weight) {
		super(CustomAttributes.COLD_DAMAGE, 3, 7, Operation.ADDITION, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.SWORD;
	}

	@Override
	public void onEntityDamaged(LivingEntity user, Entity target, float level) {
		if (target instanceof LivingEntity) ((LivingEntity) target).addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 20 * Math.max(3, (int) (level / 1.5F)), 1));
		target.hurt(COLD, Apotheosis.localAtkStrength * level);
	}

	@Override
	public float getMin() {
		return 1;
	}

	@Override
	public float getMax() {
		return 10;
	}

}