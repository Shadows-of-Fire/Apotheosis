package shadows.deadly.loot;

import net.minecraft.util.WeightedRandom;

public abstract class AffixModifier extends WeightedRandom.Item {

	/**
	 * The language key for this modifier.
	 */
	protected final String key;

	/**
	 * If this modifier is for a prefix or a suffix.
	 */
	protected final boolean isPrefix;

	/**
	 * What this modifier does to the underlying affix.
	 */
	protected final Operation op;

	/**
	 * The value of this modifier.
	 */
	protected final double value;

	public AffixModifier(String key, boolean prefix, Operation op, double value, int weight) {
		super(weight);
		this.key = key;
		this.isPrefix = prefix;
		this.op = op;
		this.value = value;
	}

	public static enum Operation {
		ADD,
		MULTIPLY,
		SET;
	}

}
