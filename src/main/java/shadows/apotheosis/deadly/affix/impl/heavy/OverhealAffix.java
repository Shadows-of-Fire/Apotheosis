package shadows.apotheosis.deadly.affix.impl.heavy;

import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.attributes.CustomAttributes;
import shadows.apotheosis.deadly.affix.impl.AttributeAffix;

/**
 * Excess damage is converted into absorption hearts.  A max of 10 hearts can be stored this way.
 */
public class OverhealAffix extends AttributeAffix {

	public OverhealAffix(int weight) {
		super(CustomAttributes.OVERHEALING, 0.1F, 0.25F, Operation.MULTIPLY_TOTAL, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.AXE;
	}

	@Override
	public float getMin() {
		return 0.05F;
	}

	@Override
	public float getMax() {
		return 0.5F;
	}
}