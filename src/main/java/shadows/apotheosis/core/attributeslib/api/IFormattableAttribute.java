package shadows.apotheosis.core.attributeslib.api;

import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
	default Component valueToComponent(AttributeModifier modif, double formattedValue, TooltipFlag flag) {
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
	default Component toComponent(AttributeModifier modif, TooltipFlag flag) {
		Attribute attr = (Attribute) this;
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
	 * Helper method to invoke {@link IFormattableAttribute#toComponent(AttributeModifier, TooltipFlag)}.
	 */
	public static Component toComponent(Attribute attr, AttributeModifier modif, TooltipFlag flag) {
		return ((IFormattableAttribute) attr).toComponent(modif, flag);
	}

	/**
	 * Helper method to invoke {@link IFormattableAttribute#toComponent(AttributeModifier, TooltipFlag)}.<br>
	 * Uses the TooltipFlag from the client options.
	 */
	public static Component toComponent(Attribute attr, AttributeModifier modif) {
		return toComponent(attr, modif, AttributesLib.getTooltipFlag());
	}

}
