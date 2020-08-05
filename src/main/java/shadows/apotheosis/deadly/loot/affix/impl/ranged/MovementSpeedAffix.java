package shadows.apotheosis.deadly.loot.affix.impl.ranged;

import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import shadows.apotheosis.deadly.loot.EquipmentType;
import shadows.apotheosis.deadly.loot.affix.impl.AttributeAffix;

/**
 * Ranged Movement Speed Affix.  Provies 50% - 150% additional movement speed while holding the weapon.
 */
public class MovementSpeedAffix extends AttributeAffix {

	public MovementSpeedAffix(int weight) {
		super(Attributes.field_233821_d_, 0.05F, 0.75F, Operation.MULTIPLY_TOTAL, weight);
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
