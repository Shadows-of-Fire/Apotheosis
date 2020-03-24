package shadows.apotheosis.deadly.loot.affix.impl.melee;

import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.player.PlayerEntity;
import shadows.apotheosis.deadly.loot.EquipmentType;
import shadows.apotheosis.deadly.loot.affix.impl.AttributeAffix;

public class ReachDistanceAffix extends AttributeAffix {

	public ReachDistanceAffix(int weight) {
		super(PlayerEntity.REACH_DISTANCE, 0.5F, 2.0F, Operation.ADDITION, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.SWORD;
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
