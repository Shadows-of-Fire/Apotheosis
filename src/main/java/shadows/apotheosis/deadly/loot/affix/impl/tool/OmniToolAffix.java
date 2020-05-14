package shadows.apotheosis.deadly.loot.affix.impl.tool;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;
import shadows.apotheosis.deadly.loot.EquipmentType;
import shadows.apotheosis.deadly.loot.affix.Affix;
import shadows.apotheosis.deadly.loot.affix.AffixHelper;
import shadows.apotheosis.deadly.loot.modifiers.AffixModifier;

/**
 * Allows this tool to mine anything that a diamond shovel/axe/pickaxe could.
 */
public class OmniToolAffix extends Affix {

	public OmniToolAffix(int weight) {
		super(weight);
	}

	@Override
	public float apply(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		AffixHelper.addLore(stack, new TranslationTextComponent("affix." + this.getRegistryName() + ".desc"));
		return 1;
	}

	@Override
	public float getMin() {
		return 1;
	}

	@Override
	public float getMax() {
		return 1;
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.PICKAXE || type == EquipmentType.SHOVEL;
	}

}
