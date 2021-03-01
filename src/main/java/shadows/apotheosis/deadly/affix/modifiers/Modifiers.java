package shadows.apotheosis.deadly.affix.modifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.util.WeightedRandom;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier.AffixOp;

public class Modifiers {

	private static final List<AffixModifier> MODIFIERS = new ArrayList<>();

	//Good
	public static final AffixModifier MUL_QUARTER = register(new AffixModifier("mul_quarter", AffixOp.MULTIPLY, 1.25F, 12)).dontEditName();
	public static final AffixModifier MUL_HALF = register(new AffixModifier("mul_half", AffixOp.MULTIPLY, 1.5F, 8)).dontEditName();
	public static final AffixModifier PLUS_PT_FIVE = register(new AffixModifier("plus_pt_five", AffixOp.ADD, 0.5F, 4)).dontEditName();
	public static final AffixModifier DOUBLE = register(new AffixModifier("double", AffixOp.MULTIPLY, 2F, 6)).dontEditName();
	public static final AffixModifier MAX = register(new MaxModifier(2)).dontEditName();

	//Bad
	public static final AffixModifier HALF = register(new AffixModifier("halved", AffixOp.MULTIPLY, 0.5F, 0)).dontEditName();
	public static final AffixModifier MINUS_PT_FIVE = register(new AffixModifier("minus_pt_five", AffixOp.ADD, -0.5F, 0)).dontEditName();
	public static final AffixModifier MIN = register(new MinModifier(0)).dontEditName();

	private static AffixModifier register(AffixModifier modif) {
		MODIFIERS.add(modif);
		return modif;
	}

	public static AffixModifier getRandomModifier(Random rand) {
		return WeightedRandom.getRandomItem(rand, MODIFIERS);
	}

	public static AffixModifier getBadModifier() {
		return MODIFIERS.stream().filter(m -> m.itemWeight == 0).findAny().get();
	}
}