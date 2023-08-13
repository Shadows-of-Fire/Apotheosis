package shadows.apotheosis.core.mobfx.potions;

import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import shadows.apotheosis.core.attributeslib.api.ALAttributes;

public class VitalityEffect extends MobEffect {

    public VitalityEffect() {
        super(MobEffectCategory.BENEFICIAL, ChatFormatting.RED.getColor());
        this.addAttributeModifier(ALAttributes.HEALING_RECEIVED.get(), "a232ff72-b070-42f5-bf84-bd220d45d698", +0.2, Operation.ADDITION);
    }

}
