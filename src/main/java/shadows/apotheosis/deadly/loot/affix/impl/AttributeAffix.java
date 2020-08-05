package shadows.apotheosis.deadly.loot.affix.impl;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.RandomValueRange;
import shadows.apotheosis.deadly.loot.EquipmentType;
import shadows.apotheosis.deadly.loot.affix.Affix;
import shadows.apotheosis.deadly.loot.modifiers.AffixModifier;

/**
 * Helper class for affixes that modify attributes, as the apply method is the same for most of those.
 */
public abstract class AttributeAffix extends Affix {

	protected final Attribute attr;
	protected final RandomValueRange range;
	protected final Operation op;

	public AttributeAffix(Attribute attr, RandomValueRange range, Operation op, int weight) {
		super(weight);
		this.attr = attr;
		this.range = range;
		this.op = op;
	}

	public AttributeAffix(Attribute attr, float min, float max, Operation op, int weight) {
		this(attr, new RandomValueRange(min, max), op, weight);
	}

	@Override
	public float apply(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		EquipmentSlotType type = EquipmentType.getTypeFor(stack).getSlot(stack);
		float lvl = range.generateFloat(rand);
		if (modifier != null) lvl = modifier.editLevel(this, lvl);
		AttributeModifier modif = new AttributeModifier(this.getRegistryName() + "_" + attr.getRegistryName(), lvl, op);
		stack.addAttributeModifier(attr, modif, type);
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
