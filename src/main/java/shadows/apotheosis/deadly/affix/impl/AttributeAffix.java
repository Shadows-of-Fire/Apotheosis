package shadows.apotheosis.deadly.affix.impl;

import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.RandomValueRange;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

/**
 * Helper class for affixes that modify attributes, as the apply method is the same for most of those.
 */
public abstract class AttributeAffix extends Affix {

	protected final Supplier<Attribute> attr;
	protected final RandomValueRange range;
	protected final Operation op;

	public AttributeAffix(Supplier<Attribute> attr, RandomValueRange range, Operation op, int weight) {
		super(weight);
		this.attr = attr;
		this.range = range;
		this.op = op;
	}

	public AttributeAffix(Supplier<Attribute> attr, float min, float max, Operation op, int weight) {
		this(attr, new RandomValueRange(min, max), op, weight);
	}

	public AttributeAffix(Attribute attr, RandomValueRange range, Operation op, int weight) {
		this(() -> attr, range, op, weight);
	}

	public AttributeAffix(Attribute attr, float min, float max, Operation op, int weight) {
		this(() -> attr, new RandomValueRange(min, max), op, weight);
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		float lvl = range.generateFloat(rand);
		if (modifier != null) lvl = modifier.editLevel(this, lvl);
		return lvl;
	}

	@Override
	public void addModifiers(ItemStack stack, float level, EquipmentSlotType type, Multimap<Attribute, AttributeModifier> map) {
		EquipmentSlotType ourType = EquipmentType.getTypeFor(stack).getSlot(stack);
		if (ourType == type) {
			AttributeModifier modif = new AttributeModifier(this.getRegistryName() + "_" + attr.get().getRegistryName(), level, op);
			map.put(attr.get(), modif);
		}
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