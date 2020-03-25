package shadows.apotheosis.deadly.loot.affix.impl.heavy;

import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import shadows.apotheosis.deadly.loot.EquipmentType;
import shadows.apotheosis.deadly.loot.affix.impl.AttributeAffix;
import shadows.apotheosis.deadly.loot.attributes.CustomAttributes;

public class OverhealAffix extends AttributeAffix {

	public OverhealAffix(int weight) {
		super(CustomAttributes.OVERHEALING, 0.1F, 1, Operation.MULTIPLY_TOTAL, weight);
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
		return 1.5F;
	}
}
