package shadows.apotheosis.deadly.loot.affix.impl.ranged;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import shadows.apotheosis.deadly.loot.EquipmentType;
import shadows.apotheosis.deadly.loot.affix.impl.AttributeAffix;
import shadows.apotheosis.deadly.loot.attributes.CustomAttributes;

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
	public void onArrowImpact(AbstractArrowEntity arrow, RayTraceResult res, Type type, float level) {
		Entity shooter = arrow.func_234616_v_();
		if (shooter != null && type == Type.ENTITY) {
			if (shooter.getDistanceSq(((EntityRayTraceResult) res).getEntity()) > 30 * 30) {
				arrow.setDamage(arrow.getDamage() + level);
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
