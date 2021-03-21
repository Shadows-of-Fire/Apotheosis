package shadows.apotheosis.deadly.affix.impl;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.RandomValueRange;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

public abstract class RangedAffix extends Affix {

	protected final RandomValueRange range;

	public RangedAffix(RandomValueRange range, int weight) {
		super(weight);
		this.range = range;
	}

	public RangedAffix(float min, float max, int weight) {
		this(new RandomValueRange(min, max), weight);
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		float lvl = this.range.generateFloat(rand);
		if (modifier != null) lvl = modifier.editLevel(this, lvl);
		AffixHelper.addLore(stack, loreComponent("affix." + this.getRegistryName() + ".desc", String.format("%.2f", lvl)));
		return lvl;
	}

	@Override
	public float getMin() {
		return this.range.getMin();
	}

	@Override
	public float getMax() {
		return this.range.getMax();
	}

}