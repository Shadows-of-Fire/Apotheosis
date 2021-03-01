package shadows.apotheosis.deadly.affix.impl.heavy;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

/**
 * Baseline affix for all heavy weapons.  Damage is converted into armor piercing damage.
 */
public class PiercingAffix extends Affix {

	public PiercingAffix(int weight) {
		super(weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.AXE;
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		return 1;
	}

	@Override
	public float getMin() {
		return 0;
	}

	@Override
	public float getMax() {
		return 0;
	}
}