package shadows.apotheosis.deadly.affix.impl.melee;

import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.attributes.CustomAttributes;
import shadows.apotheosis.deadly.affix.impl.AttributeAffix;

/**
 * Increases crit damage.
 */
public class CritDamageAffix extends AttributeAffix {

	public CritDamageAffix(int weight) {
		super(CustomAttributes.CRIT_DAMAGE, 0.3F, 1.2F, Operation.MULTIPLY_TOTAL, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.SWORD || type == EquipmentType.AXE;
	}

	@Override
	public float getMin() {
		return 0.1F;
	}

	@Override
	public float getMax() {
		return 1.5F;
	}

}