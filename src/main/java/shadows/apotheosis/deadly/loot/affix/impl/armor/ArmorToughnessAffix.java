package shadows.apotheosis.deadly.loot.affix.impl.armor;

import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import shadows.apotheosis.deadly.loot.EquipmentType;
import shadows.apotheosis.deadly.loot.affix.impl.AttributeAffix;

/**
 * Increases armor toughness.
 */
public class ArmorToughnessAffix extends AttributeAffix {

	public ArmorToughnessAffix(int weight) {
		super(Attributes.field_233827_j_, 0.5F, 2F, Operation.ADDITION, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.ARMOR;
	}

	@Override
	public float getMax() {
		return 3;
	}

}
