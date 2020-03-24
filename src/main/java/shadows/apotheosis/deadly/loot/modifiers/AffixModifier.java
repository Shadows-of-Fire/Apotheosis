package shadows.apotheosis.deadly.loot.modifiers;

import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.MathHelper;
import shadows.apotheosis.deadly.loot.affix.Affix;

public class AffixModifier extends WeightedRandom.Item {

	/**
	 * The language key for this modifier.
	 */
	protected final String key;

	/**
	 * What this modifier does to the underlying affix.
	 */
	protected final AffixOp op;

	/**
	 * The value of this modifier.
	 */
	protected final float value;

	protected boolean editName = true;

	public AffixModifier(String key, AffixOp op, float value, int weight) {
		super(weight);
		this.key = key;
		this.op = op;
		this.value = value;
	}

	public AffixModifier dontEditName() {
		editName = false;
		return this;
	}

	/**
	 * Adjusts the passed level, according to the operation of this modifier.
	 */
	public float editLevel(Affix affix, float level) {
		float newLevel = op == AffixOp.ADD ? level + value : op == AffixOp.MULTIPLY ? level * value : value;
		return MathHelper.clamp(newLevel, affix.getMin(), affix.getMax());
	}

	/**
	 * Get the translation key of this modifier.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * If this modifier should edit the name of the affix.
	 */
	public boolean editName() {
		return editName;
	}

	public static enum AffixOp {
		ADD,
		MULTIPLY,
		SET;
	}

}
