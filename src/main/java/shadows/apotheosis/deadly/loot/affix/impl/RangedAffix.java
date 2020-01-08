package shadows.apotheosis.deadly.loot.affix.impl;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.storage.loot.RandomValueRange;
import shadows.apotheosis.deadly.loot.AffixModifier;
import shadows.apotheosis.deadly.loot.affix.Affix;
import shadows.apotheosis.deadly.loot.affix.AffixHelper;

public class RangedAffix extends Affix {

	protected final RandomValueRange range;

	public RangedAffix(RandomValueRange range, boolean prefix, int weight) {
		super(prefix, weight);
		this.range = range;
	}

	public RangedAffix(float min, float max, boolean prefix, int weight) {
		this(new RandomValueRange(min, max), prefix, weight);
	}

	@Override
	public float apply(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		float lvl = range.generateFloat(rand);
		if (modifier != null) lvl = modifier.editLevel(lvl);
		AffixHelper.addLore(stack, new TranslationTextComponent("affix." + this.getRegistryName() + ".desc", lvl));
		return lvl;
	}

}
