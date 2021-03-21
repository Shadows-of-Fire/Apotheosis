package shadows.apotheosis.deadly.affix.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
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
	protected final Map<EquipmentSlotType, UUID> uuidCache;

	public AttributeAffix(Supplier<Attribute> attr, RandomValueRange range, Operation op, int weight) {
		super(weight);
		this.attr = attr;
		this.range = range;
		this.op = op;
		this.uuidCache = new HashMap<>();
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
		float lvl = this.range.generateFloat(rand);
		if (modifier != null) lvl = modifier.editLevel(this, lvl);
		return lvl;
	}

	@Override
	public void addInformation(ItemStack stack, float level, Consumer<ITextComponent> list) {
	}

	@Override
	public ITextComponent getDisplayName(float level) {
		if (this.op == Operation.ADDITION) return super.getDisplayName(level);
		return new TranslationTextComponent("affix." + this.getRegistryName() + ".name", fmt(level * 100)).mergeStyle(TextFormatting.GRAY);
	}

	@Override
	public void addModifiers(ItemStack stack, float level, EquipmentSlotType type, BiConsumer<Attribute, AttributeModifier> map) {
		EquipmentSlotType ourType = EquipmentType.getTypeFor(stack).getSlot(stack);
		if (ourType == type) {
			AttributeModifier modif = new AttributeModifier(this.uuidCache.computeIfAbsent(type, k -> UUID.randomUUID()), "Apotheosis affix bonus: " + this.getRegistryName(), level, this.op);
			map.accept(this.attr.get(), modif);
		}
	}

	@Override
	public float getMin() {
		return this.range.getMin();
	}

	@Override
	public float getMax() {
		return this.range.getMax();
	}

}