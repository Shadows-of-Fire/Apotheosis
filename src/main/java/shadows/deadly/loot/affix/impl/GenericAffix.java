package shadows.deadly.loot.affix.impl;

import java.util.Random;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import shadows.deadly.loot.AffixModifier;
import shadows.deadly.loot.affix.Affix;
import shadows.deadly.loot.affix.AffixHelper;

public class GenericAffix extends Affix {

	public GenericAffix(boolean prefix, int weight) {
		super(prefix, weight);
	}

	@Override
	public float apply(ItemStack stack, Random rand, AffixModifier modifier) {
		AffixHelper.addLore(stack, new TextComponentTranslation("affix." + this.getRegistryName() + ".desc").getFormattedText());
		return 0;
	}

}
