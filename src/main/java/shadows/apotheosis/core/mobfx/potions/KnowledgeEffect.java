package shadows.apotheosis.core.mobfx.potions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import shadows.apotheosis.core.attributeslib.api.ALAttributes;
import shadows.apotheosis.potion.PotionModule;

public class KnowledgeEffect extends MobEffect {

    public KnowledgeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF4EE42);
        this.addAttributeModifier(ALAttributes.EXPERIENCE_GAINED.get(), "55688e2f-7db8-4d0b-bc90-eff194546c04", PotionModule.knowledgeMult, Operation.MULTIPLY_TOTAL);
    }

    public double getAttributeModifierValue(int amp, AttributeModifier modifier) {
        return (++amp * amp) * PotionModule.knowledgeMult;
    }

}
