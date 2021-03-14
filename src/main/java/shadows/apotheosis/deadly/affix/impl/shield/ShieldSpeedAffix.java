package shadows.apotheosis.deadly.affix.impl.shield;

import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.impl.AttributeAffix;

/**
 * Shield Movement Speed Affix.  Provies 10% - 65% additional movement speed while holding the shield.
 */
public class ShieldSpeedAffix extends AttributeAffix {

	public ShieldSpeedAffix(int weight) {
		super(Attributes.MOVEMENT_SPEED, 0.1F, 0.4F, Operation.MULTIPLY_TOTAL, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.SHIELD;
	}

	@Override
	public float getMax() {
		return 0.65F;
	}

}
