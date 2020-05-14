package shadows.apotheosis.deadly.loot.affix.impl.armor;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import shadows.apotheosis.deadly.loot.EquipmentType;
import shadows.apotheosis.deadly.loot.affix.impl.AttributeAffix;

/**
 * Increases armor.
 */
public class ArmorAffix extends AttributeAffix {

	public ArmorAffix(int weight) {
		super(SharedMonsterAttributes.ARMOR, 0.5F, 4F, Operation.ADDITION, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.ARMOR;
	}

	@Override
	public float getMax() {
		return 6F;
	}

}
