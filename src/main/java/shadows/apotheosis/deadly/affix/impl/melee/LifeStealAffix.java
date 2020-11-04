package shadows.apotheosis.deadly.affix.impl.melee;

import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.attributes.CustomAttributes;
import shadows.apotheosis.deadly.affix.impl.AttributeAffix;

/**
 * Damage dealt is returned as health.
 */
public class LifeStealAffix extends AttributeAffix {

	public LifeStealAffix(int weight) {
		super(CustomAttributes.LIFE_STEAL, 0.1F, 0.25F, Operation.MULTIPLY_TOTAL, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.SWORD || type == EquipmentType.AXE;
	}

	@Override
	public float getMin() {
		return 0.05F;
	}

	@Override
	public float getMax() {
		return 0.75F;
	}
}