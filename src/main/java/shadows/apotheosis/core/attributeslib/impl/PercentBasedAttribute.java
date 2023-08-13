package shadows.apotheosis.core.attributeslib.impl;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import shadows.apotheosis.core.attributeslib.api.IFormattableAttribute;

/**
 * A Percentile Based Attribute is one which always displays modifiers as percentages, even addition ones.<br>
 * This is used for attributes that would not make sense being displayed as flat additions (ex +0.05 Life Steal).
 */
public class PercentBasedAttribute extends RangedAttribute implements IFormattableAttribute {

    public PercentBasedAttribute(String pDescriptionId, double pDefaultValue, double pMin, double pMax) {
        super(pDescriptionId, pDefaultValue, pMin, pMax);
    }

    @Override
    public MutableComponent toValueComponent(Operation op, double value, TooltipFlag flag) {
        return Component.translatable("attributeslib.value.percent", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(value * 100));
    }

}
