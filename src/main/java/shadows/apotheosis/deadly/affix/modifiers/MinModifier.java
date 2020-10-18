package shadows.apotheosis.deadly.affix.modifiers;

import shadows.apotheosis.deadly.affix.Affix;

public class MinModifier extends AffixModifier {

	public MinModifier(int weight) {
		super("min", AffixOp.ADD, 0, weight);
	}

	@Override
	public float editLevel(Affix affix, float level) {
		return affix.getMin();
	}

}