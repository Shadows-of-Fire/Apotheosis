package shadows.apotheosis.deadly.affix.impl.heavy;

import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.attributes.CustomAttributes;
import shadows.apotheosis.deadly.affix.impl.AttributeAffix;

/**
 * Critical Strike chance is set to 100%
 */
public class MaxCritAffix extends AttributeAffix {

	public MaxCritAffix(int weight) {
		super(CustomAttributes.CRIT_CHANCE, ConstantFloat.of(1.0F), Operation.MULTIPLY_TOTAL, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.AXE;
	}

}