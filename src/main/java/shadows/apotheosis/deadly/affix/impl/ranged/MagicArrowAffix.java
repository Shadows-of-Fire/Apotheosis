package shadows.apotheosis.deadly.affix.impl.ranged;

import java.util.Random;

import net.minecraft.item.ItemStack;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

/**
 * Arrow damage is converted into magic damage.
 */
public class MagicArrowAffix extends Affix {

	public MagicArrowAffix(int weight) {
		super(weight);
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, AffixModifier modifier) {
		return 1;
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.RANGED;
	}

	@Override
	public float getMin() {
		return 1;
	}
	
	@Override
	public float upgradeLevel(float curLvl, float newLvl) {
		return 1;
	}

	@Override
	public float obliterateLevel(float level) {
		return 1;
	}

	@Override
	public float getMax() {
		return 1;
	}

}