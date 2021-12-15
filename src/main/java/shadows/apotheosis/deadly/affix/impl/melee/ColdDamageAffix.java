package shadows.apotheosis.deadly.affix.impl.melee;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
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
		if (target instanceof LivingEntity) ((LivingEntity) target).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * Math.max(3, (int) (level / 1.5F)), 1));
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