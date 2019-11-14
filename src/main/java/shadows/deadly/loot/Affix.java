package shadows.deadly.loot;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public abstract class Affix extends WeightedRandom.Item {

	/**
	 * The language key for this affix.
	 */
	protected final String key;

	/**
	 * If this affix is a prefix or a suffix.
	 */
	protected final boolean isPrefix;

	/**
	 * @param weight The weight of this affix, relative to other affixes in the same group.
	 */
	public Affix(String key, boolean prefix, int weight) {
		super(weight);
		this.key = key;
		this.isPrefix = prefix;
	}

	/**
	 * Apply the modifiers of this affix to the given stack.
	 * @param stack The stack to be modified.
	 * @param AffixModifier A modifier to be applied to this affix, or null, if no modifier is applied.  The values applied should reflect the modifier.
	 */
	public abstract void apply(ItemStack stack, Random rand, @Nullable AffixModifier modifier);

	/**
	 * Chain the name of this affix to the existing name.  If this is a prefix, it should be applied to the front.
	 * If this is a suffix, it should be applied to the black.
	 * @param name The current name, which may have been modified by other affixes.
	 * @return The new name, consuming the old name in the process.
	 */
	public ITextComponent chainName(ITextComponent name, @Nullable AffixModifier modifier) {
		return new TextComponentTranslation(key, name);
	}

}
