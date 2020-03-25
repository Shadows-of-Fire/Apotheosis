package shadows.apotheosis.deadly.loot.affix.impl.heavy;

import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import shadows.apotheosis.deadly.loot.EquipmentType;
import shadows.apotheosis.deadly.loot.affix.impl.AttributeAffix;
import shadows.apotheosis.deadly.loot.attributes.CustomAttributes;

public class MaxCritAffix extends AttributeAffix {

	public MaxCritAffix(int weight) {
		super(CustomAttributes.CRIT_CHANCE, 1F, 1.0F, Operation.MULTIPLY_TOTAL, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.AXE;
	}

}
