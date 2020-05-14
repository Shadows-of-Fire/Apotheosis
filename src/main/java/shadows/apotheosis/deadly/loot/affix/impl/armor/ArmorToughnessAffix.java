package shadows.apotheosis.deadly.loot.affix.impl.armor;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import shadows.apotheosis.deadly.loot.EquipmentType;
import shadows.apotheosis.deadly.loot.affix.impl.AttributeAffix;

/**
 * Increases armor toughness.
 */
public class ArmorToughnessAffix extends AttributeAffix {

	public ArmorToughnessAffix(int weight) {
		super(SharedMonsterAttributes.MAX_HEALTH, 0.5F, 2F, Operation.ADDITION, weight);
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
