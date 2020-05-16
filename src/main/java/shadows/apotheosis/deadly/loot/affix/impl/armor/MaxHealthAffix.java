package shadows.apotheosis.deadly.loot.affix.impl.armor;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import shadows.apotheosis.deadly.loot.EquipmentType;
import shadows.apotheosis.deadly.loot.affix.impl.AttributeAffix;

/**
 * Increases max hp.
 */
public class MaxHealthAffix extends AttributeAffix {

	public MaxHealthAffix(int weight) {
		super(SharedMonsterAttributes.MAX_HEALTH, 1F, 5F, Operation.ADDITION, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.ARMOR;
	}

	@Override
	public float getMax() {
		return 8F;
	}

}
