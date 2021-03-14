package shadows.apotheosis.deadly.affix.impl.armor;

import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.impl.AttributeAffix;

/**
 * Increases armor toughness.
 */
public class ArmorToughnessAffix extends AttributeAffix {

	public ArmorToughnessAffix(int weight) {
		super(Attributes.ARMOR_TOUGHNESS, 0.5F, 2F, Operation.ADDITION, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.ARMOR || type == EquipmentType.SHIELD;
	}

	@Override
	public float getMax() {
		return 3;
	}

}