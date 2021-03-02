package shadows.apotheosis.deadly.affix.impl.shield;

import java.util.Random;

import net.minecraft.item.ItemStack;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

public class EnragedBlockAffix extends Affix {

	public EnragedBlockAffix(int weight) {
		super(weight);
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, AffixModifier modifier) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.SHIELD;
	}

	@Override
	public float getMin() {
		return 1;
	}

	@Override
	public float getMax() {
		return 3;
	}

}
