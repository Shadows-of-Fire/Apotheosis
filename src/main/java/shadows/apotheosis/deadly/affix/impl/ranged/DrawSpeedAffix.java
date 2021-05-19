package shadows.apotheosis.deadly.affix.impl.ranged;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.item.ItemStack;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.attributes.CustomAttributes;
import shadows.apotheosis.deadly.affix.impl.AttributeAffix;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

/**
 * Decreases how long it takes to fully charge a bow.
 */
public class DrawSpeedAffix extends AttributeAffix {

	private static final float[] values = { 0.1F, 0.2F, 0.25F, 0.33F, 0.5F, 1.0F, 1.1F, 1.2F, 1.25F, 1.33F, 1.5F };

	public DrawSpeedAffix(int weight) {
		super(CustomAttributes.DRAW_SPEED, 0.1F, 1.5F, Operation.MULTIPLY_TOTAL, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.RANGED;
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		float lvl = values[rand.nextInt(values.length)];
		return lvl;
	}

	public float upgradeLevel(float curLvl, float newLvl) {
		int curIdx = 0, newIdx = 0;
		for (int i = 0; i < values.length; i++) {
			if (values[i] == curLvl) curIdx = i;
			if (values[i] == newLvl) newIdx = i;
		}
		return values[Math.min(values.length - 1, curIdx > newIdx ? curIdx + newIdx / 2 : curIdx / 2 + newIdx)];
	}

	/**
	 * Generates a new level, as if the passed level were to be split in two.
	 */
	public float obliterateLevel(float level) {
		int idx = 0;
		for (int i = 0; i < values.length; i++) {
			if (values[i] == level) {
				idx = i;
				break;
			}
		}
		return values[Math.max(0, idx / 2)];
	}
}