package shadows.apotheosis.deadly.affix.impl.heavy;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
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
		if (user instanceof Player && target instanceof LivingEntity) {
			target.hurt(DamageSource.playerAttack((Player) user), ((LivingEntity) target).getHealth() * Apotheosis.localAtkStrength * level);
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