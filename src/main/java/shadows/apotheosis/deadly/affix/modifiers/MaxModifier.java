package shadows.apotheosis.deadly.affix.modifiers;

import shadows.apotheosis.deadly.affix.Affix;

public class MaxModifier extends AffixModifier {

	public MaxModifier(int weight) {
		super("max", AffixOp.ADD, 0, weight);
	}

	@Override
	public float editLevel(Affix affix, float level) {
		return affix.getMax();
	}

}