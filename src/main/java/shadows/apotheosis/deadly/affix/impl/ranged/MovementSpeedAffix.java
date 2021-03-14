package shadows.apotheosis.deadly.affix.impl.ranged;

import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.impl.AttributeAffix;

/**
 * Ranged Movement Speed Affix.  Provies 25% - 150% additional movement speed while holding the weapon.
 */
public class MovementSpeedAffix extends AttributeAffix {

	public MovementSpeedAffix(int weight) {
		super(Attributes.MOVEMENT_SPEED, 0.25F, 0.75F, Operation.MULTIPLY_TOTAL, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.RANGED;
	}

	@Override
	public float getMax() {
		return 1.5F;
	}

}