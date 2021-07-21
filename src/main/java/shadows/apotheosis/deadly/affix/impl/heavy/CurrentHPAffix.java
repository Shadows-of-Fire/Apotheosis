package shadows.apotheosis.deadly.affix.impl.heavy;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.attributes.CustomAttributes;
import shadows.apotheosis.deadly.affix.impl.AttributeAffix;

/**
 * Attacks do a percent of the target's current hp as damage.
 */
public class CurrentHPAffix extends AttributeAffix {

	public CurrentHPAffix(int weight) {
		super(CustomAttributes.CURRENT_HP_DAMAGE, 0.05F, 0.2F, Operation.MULTIPLY_TOTAL, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.AXE;
	}

	@Override
	public void onEntityDamaged(LivingEntity user, Entity target, float level) {
		if (user instanceof PlayerEntity && target instanceof LivingEntity) {
			target.hurt(DamageSource.playerAttack((PlayerEntity) user), ((LivingEntity) target).getHealth() * Apotheosis.localAtkStrength * level);
		}
	}

	@Override
	public float getMin() {
		return 0.03F;
	}

	@Override
	public float getMax() {
		return 0.25F;
	}

}