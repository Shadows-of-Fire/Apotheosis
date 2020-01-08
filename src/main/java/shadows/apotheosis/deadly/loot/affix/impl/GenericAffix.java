package shadows.apotheosis.deadly.loot.affix.impl;

import java.util.Random;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;
import shadows.apotheosis.deadly.loot.AffixModifier;
import shadows.apotheosis.deadly.loot.affix.Affix;
import shadows.apotheosis.deadly.loot.affix.AffixHelper;

public class GenericAffix extends Affix {

	public GenericAffix(boolean prefix, int weight) {
		super(prefix, weight);
	}

	@Override
	public float apply(ItemStack stack, Random rand, AffixModifier modifier) {
		AffixHelper.addLore(stack, new TranslationTextComponent("affix." + this.getRegistryName() + ".desc"));
		return 0;
	}

}
