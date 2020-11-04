package shadows.apotheosis.deadly.affix.impl.melee;

import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraftforge.common.ForgeMod;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.impl.AttributeAffix;

/**
 * Increases reach distance.
 */
public class ReachDistanceAffix extends AttributeAffix {

	public ReachDistanceAffix(int weight) {
		super(ForgeMod.REACH_DISTANCE, 0.5F, 2.0F, Operation.ADDITION, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.SWORD || type == EquipmentType.PICKAXE || type == EquipmentType.SHOVEL;
	}

	@Override
	public float getMin() {
		return 0.25F;
	}

	@Override
	public float getMax() {
		return 3.0F;
	}

}