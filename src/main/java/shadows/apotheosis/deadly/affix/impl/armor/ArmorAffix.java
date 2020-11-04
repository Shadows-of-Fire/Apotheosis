package shadows.apotheosis.deadly.affix.impl.armor;

import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.impl.AttributeAffix;

/**
 * Increases armor.
 */
public class ArmorAffix extends AttributeAffix {

	public ArmorAffix(int weight) {
		super(Attributes.ARMOR, 0.5F, 4F, Operation.ADDITION, weight);
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