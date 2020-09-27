package shadows.apotheosis.deadly.loot.modifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.util.WeightedRandom;
import shadows.apotheosis.deadly.loot.modifiers.AffixModifier.AffixOp;

public class Modifiers {

	private static final List<AffixModifier> MODIFIERS = new ArrayList<>();

	public static final AffixModifier HALF = register(new AffixModifier("halved", AffixOp.MULTIPLY, 0.5F, 0)).dontEditName();
	public static final AffixModifier PLUS_FIFTY = register(new AffixModifier("plus_fifty", AffixOp.MULTIPLY, 1.5F, 16)).dontEditName();
	public static final AffixModifier DOUBLE = register(new AffixModifier("double", AffixOp.MULTIPLY, 2F, 8)).dontEditName();
	public static final AffixModifier MIN = register(new MinModifier(0)).dontEditName();
	public static final AffixModifier MAX = register(new MaxModifier(1)).dontEditName();

	private static AffixModifier register(AffixModifier modif) {
		MODIFIERS.add(modif);
		return modif;
	}

	public static AffixModifier getRandomModifier(Random rand) {
		return WeightedRandom.getRandomItem(rand, MODIFIERS);
	}
}