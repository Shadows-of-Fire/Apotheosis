package shadows.apotheosis.deadly.affix.impl.ranged;

import java.util.Random;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;
import shadows.apotheosis.deadly.loot.EquipmentType;

/**
 * Arrow damage is converted into magic damage.
 */
public class MagicArrowAffix extends Affix {

	public MagicArrowAffix(int weight) {
		super(weight);
	}

	@Override
	public float apply(ItemStack stack, Random rand, AffixModifier modifier) {
		AffixHelper.addLore(stack, new TranslationTextComponent("affix." + this.getRegistryName() + ".desc"));
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
	public float getMax() {
		return 1;
	}

}