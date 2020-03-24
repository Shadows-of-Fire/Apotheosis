package shadows.apotheosis.deadly.loot.modifiers;

import shadows.apotheosis.deadly.loot.affix.Affix;

public class MaxModifier extends AffixModifier {

	public MaxModifier(int weight) {
		super("max", AffixOp.ADD, 0, weight);
	}

	@Override
	public float editLevel(Affix affix, float level) {
		return affix.getMax();
	}

}
