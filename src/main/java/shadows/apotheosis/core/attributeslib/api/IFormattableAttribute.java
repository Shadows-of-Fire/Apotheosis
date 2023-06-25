package shadows.apotheosis.core.attributeslib.api;

import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import shadows.apotheosis.core.attributeslib.AttributesLib;

/**
 * A Formattable Attribute is one which elects to have control over its tooltip representation.<br>
 * This interface also serves as the primary means of displaying attribute modifiers.
 */
public interface IFormattableAttribute {

	/**
	 * Converts the value of an attribute modifier to the value that will be displayed.<p>
	 * For multiplication modifiers, this method is responsible for converting the value to percentage form.<br>
	 * The only vanilla attribute which performs value formatting is Knockback Resistance.<br>
	 * @param modif The Attribute Modifier whose value is being formatted.
	 * @param flag The tooltip flag.
	 * @return The formatted attribute modifier value.
	 */
	default double formatValue(AttributeModifier modif, TooltipFlag flag) {
		double value = modif.getAmount();

		if (modif.getOperation() == Operation.ADDITION) {
			// Knockback Resist displays addition modifiers as 10x higher than the real value.
			if (this == Attributes.KNOCKBACK_RESISTANCE) value *= 10.0D;
		} else {
			// Multiplication modifiers display the value as a percentage.
			value *= 100.0D;
		}

		return value;
	}

	/**
	 * Converts the formatted value of an attribute modifier into a Component.
	 * @param modif The Attribute Modifier whose value is being formatted.
	 * @param formattedValue The formatted value, from {@link #formatValue(AttributeModifier, TooltipFlag)}
	 * @param flag The tooltip flag.
	 * @return The component form of the formatted value.
	 */
	default MutableComponent valueToComponent(AttributeModifier modif, double formattedValue, TooltipFlag flag) {
		String key = modif.getOperation() == Operation.ADDITION ? "attributeslib.value.flat" : "attributeslib.value.percent";
		return Component.translatable(key, ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(formattedValue));
	}

	/**
	 * Converts an attribute modifier into its tooltip representation.<p>
	 * This method does not handle formatting of "base" modifiers, such as Attack Damage or Attack Speed.<p>
	 * @param modif The attribute modifier being converted to a component.
	 * @param flag The tooltip flag.
	 * @return The component representation of the passed attribute modifier.
	 */
	default MutableComponent toComponent(AttributeModifier modif, TooltipFlag flag) {
		Attribute attr = ths();
		double value = formatValue(modif, flag);

		Component debugInfo = CommonComponents.EMPTY;

		if (flag.isAdvanced()) {
			// Advanced Tooltips show the operation and the "real" (unformatted) value.
			debugInfo = Component.literal(" ").append(Component.translatable(AttributesLib.MODID + ".adv." + modif.getOperation().name().toLowerCase(Locale.ROOT), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(modif.getAmount())).withStyle(ChatFormatting.GRAY));
		}

		MutableComponent comp;

		if (value > 0.0D) {
			comp = Component.translatable("attributeslib.modifier.plus", valueToComponent(modif, value, flag), Component.translatable(attr.getDescriptionId())).withStyle(ChatFormatting.BLUE);
		} else {
			value *= -1.0D;
			comp = Component.translatable("attributeslib.modifier.take", valueToComponent(modif, value, flag), Component.translatable(attr.getDescriptionId())).withStyle(ChatFormatting.RED);
		}

		return comp.append(debugInfo);
	}

	/**
	 * Gets the specific UUID that represents a "base" (green) modifier for this attribute.
	 * @param modif The attribute modifier being checked.
	 * @param flag The tooltip flag.
	 * @return The UUID of the "base" modifier, or null, if no such modifier may exist.
	 */
	@Nullable
	default UUID getBaseUUID() {
		if (this == Attributes.ATTACK_DAMAGE) return AttributeHelper.BASE_ATTACK_DAMAGE;
		else if (this == Attributes.ATTACK_SPEED) return AttributeHelper.BASE_ATTACK_SPEED;
		return null;
	}

	/**
	 * Converts an attribute modifier into its tooltip representation.<p>
	 * This method does not handle formatting of "base" modifiers, such as Attack Damage or Attack Speed.<p>
	 * @param modif The attribute modifier being converted to a component.
	 * @param flag The tooltip flag.
	 * @return The component representation of the passed attribute modifier.
	 */
	default MutableComponent toBaseComponent(double value, double entityBase, boolean merged, TooltipFlag flag) {
		Attribute attr = ths();

		Component debugInfo = CommonComponents.EMPTY;

		if (flag.isAdvanced() && !merged) {
			// Advanced Tooltips cause us to emit the entity's base value and the base value of the item.
			debugInfo = Component.literal(" ").append(Component.translatable(AttributesLib.MODID + ".adv.base", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(entityBase), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(value - entityBase)).withStyle(ChatFormatting.GRAY));
		}

		MutableComponent comp = Component.translatable("attribute.modifier.equals.0", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(value), Component.translatable(attr.getDescriptionId()));

		return comp.append(debugInfo);
	}

	/**
	 * Certain attributes, such as Attack Damage and Projectile Damage, are increased by an Enchantment that
	 * doesn't actually apply an attribute modifier.<br>
	 * This method allows for including certain additional variables in the computation of "base" attribute values.
	 * @param stack The stack in question.
	 * @return Any bonus value to be applied to the attribute's value, after all modifiers have been applied.
	 */
	default double getBonusBaseValue(ItemStack stack) {
		if (this == Attributes.ATTACK_DAMAGE) return EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
		return 0;
	}

	/**
	 * This method is invoked when {@link #getBonusBaseValue(ItemStack)} returns a value higher than zero.<br>
	 * It is responsible for adding tooltip lines that explain where the bonus values from {@link #getBonusBaseValue(ItemStack)} are from.
	 * @param stack The stack in question.
	 * @param tooltip The tooltip consumer.
	 * @param flag The tooltip flag.
	 */
	default void addBonusTooltips(ItemStack stack, Consumer<Component> tooltip, TooltipFlag flag) {
		if (this == Attributes.ATTACK_DAMAGE) {
			float sharpness = EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
			Component debugInfo = CommonComponents.EMPTY;
			if (flag.isAdvanced()) {
				// Show the user that this fake modifier is from Sharpness.
				debugInfo = Component.literal(" ").append(Component.translatable(AttributesLib.MODID + ".adv.sharpness_bonus", sharpness).withStyle(ChatFormatting.GRAY));
			}
			MutableComponent comp = AttributeHelper.list().append(Component.translatable("attribute.modifier.plus.0", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(sharpness), Component.translatable(ths().getDescriptionId())).withStyle(ChatFormatting.BLUE));
			tooltip.accept(comp.append(debugInfo));
		}
	}

	default Attribute ths() {
		return (Attribute) this;
	}

	/**
	 * Helper method to invoke {@link #toComponent(AttributeModifier, TooltipFlag)}.
	 */
	public static MutableComponent toComponent(Attribute attr, AttributeModifier modif, TooltipFlag flag) {
		return ((IFormattableAttribute) attr).toComponent(modif, flag);
	}

	/**
	 * Helper method to invoke {@link #toBaseComponent(double, double, boolean, TooltipFlag)}
	 */
	public static MutableComponent toBaseComponent(Attribute attr, double value, double entityBase, boolean merged, TooltipFlag flag) {
		return ((IFormattableAttribute) attr).toBaseComponent(value, entityBase, merged, flag);
	}

}
