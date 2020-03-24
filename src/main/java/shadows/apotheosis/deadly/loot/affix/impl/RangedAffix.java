package shadows.apotheosis.deadly.loot.affix.impl;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.storage.loot.RandomValueRange;
import shadows.apotheosis.deadly.loot.affix.Affix;
import shadows.apotheosis.deadly.loot.affix.AffixHelper;
import shadows.apotheosis.deadly.loot.modifiers.AffixModifier;

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
	public float apply(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		float lvl = range.generateFloat(rand);
		if (modifier != null) lvl = modifier.editLevel(this, lvl);
		AffixHelper.addLore(stack, new TranslationTextComponent("affix." + this.getRegistryName() + ".desc", lvl));
		return lvl;
	}

	@Override
	public float getMin() {
		return range.getMin();
	}

	@Override
	public float getMax() {
		return range.getMax();
	}

}
