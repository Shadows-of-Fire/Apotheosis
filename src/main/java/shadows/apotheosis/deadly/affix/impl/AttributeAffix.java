package shadows.apotheosis.deadly.affix.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

/**
 * Helper class for affixes that modify attributes, as the apply method is the same for most of those.
 */
public abstract class AttributeAffix extends Affix {

	protected final Supplier<Attribute> attr;
	protected final FloatProvider range;
	protected final Operation op;
	protected final Map<EquipmentSlot, UUID> uuidCache;

	public AttributeAffix(Supplier<Attribute> attr, FloatProvider range, Operation op, int weight) {
		super(weight);
		this.attr = attr;
		this.range = range;
		this.op = op;
		this.uuidCache = new HashMap<>();
	}

	public AttributeAffix(Supplier<Attribute> attr, float min, float max, Operation op, int weight) {
		this(attr, UniformFloat.of(min, max), op, weight);
	}

	public AttributeAffix(Attribute attr, FloatProvider range, Operation op, int weight) {
		this(() -> attr, range, op, weight);
	}

	public AttributeAffix(Attribute attr, float min, float max, Operation op, int weight) {
		this(() -> attr, UniformFloat.of(min, max), op, weight);
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		float lvl = this.range.sample(rand);
		if (modifier != null) lvl = modifier.editLevel(this, lvl);
		return lvl;
	}

	@Override
	public void addInformation(ItemStack stack, float level, Consumer<Component> list) {
	}

	@Override
	public Component getDisplayName(float level) {
		if (this.op == Operation.ADDITION) return super.getDisplayName(level);
		return new TranslatableComponent("affix." + this.getRegistryName() + ".name", fmt(level * 100)).withStyle(ChatFormatting.GRAY);
	}

	@Override
	public void addModifiers(ItemStack stack, float level, EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map) {
		EquipmentType eType = EquipmentType.getTypeFor(stack);
		if (eType == null) {
			DeadlyModule.LOGGER.info("Attempted to apply the attributes of affix {} on item {}, but it is not an affix-compatible item!", this.getRegistryName(), stack.getHoverName());
			return;
		}
		EquipmentSlot ourType = eType.getSlot(stack);
		if (ourType == type) {
			AttributeModifier modif = new AttributeModifier(this.uuidCache.computeIfAbsent(type, k -> UUID.randomUUID()), "Apotheosis affix bonus: " + this.getRegistryName(), level, this.op);
			map.accept(this.attr.get(), modif);
		}
	}

	@Override
	public float getMin() {
		return this.range.getMinValue();
	}

	@Override
	public float getMax() {
		return this.range.getMaxValue();
	}

}