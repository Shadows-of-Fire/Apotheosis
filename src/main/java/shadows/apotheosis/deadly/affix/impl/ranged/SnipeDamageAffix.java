package shadows.apotheosis.deadly.affix.impl.ranged;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.attributes.CustomAttributes;
import shadows.apotheosis.deadly.affix.impl.AttributeAffix;

/**
 * Targets more than 30 blocks away take additional damage.
 */
public class SnipeDamageAffix extends AttributeAffix {

	public SnipeDamageAffix(int weight) {
		super(CustomAttributes.SNIPE_DAMAGE, 2, 10, Operation.ADDITION, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.RANGED;
	}

	@Override
	public void onArrowImpact(AbstractArrow arrow, HitResult res, Type type, float level) {
		Entity shooter = arrow.getOwner();
		if (shooter != null && type == Type.ENTITY) {
			if (shooter.distanceToSqr(((EntityHitResult) res).getEntity()) > 30 * 30) {
				arrow.setBaseDamage(arrow.getBaseDamage() + level);
			}
		}
	}

	@Override
	public float getMin() {
		return 1;
	}

	@Override
	public float getMax() {
		return 15;
	}

}