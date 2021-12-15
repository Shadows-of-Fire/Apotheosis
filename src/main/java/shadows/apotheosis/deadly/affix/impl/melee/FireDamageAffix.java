package shadows.apotheosis.deadly.affix.impl.melee;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.attributes.CustomAttributes;
import shadows.apotheosis.deadly.affix.impl.AttributeAffix;

/*
 * Adds fire damage, and attacks set enemies on fire.
 */
public class FireDamageAffix extends AttributeAffix {

	public FireDamageAffix(int weight) {
		super(CustomAttributes.FIRE_DAMAGE, 3, 7, Operation.ADDITION, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.SWORD;
	}

	@Override
	public void onEntityDamaged(LivingEntity user, Entity target, float level) {
		target.setSecondsOnFire(Math.max(3, (int) (level / 1.5F)));
		target.hurt(DamageSource.ON_FIRE, Apotheosis.localAtkStrength * level);
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