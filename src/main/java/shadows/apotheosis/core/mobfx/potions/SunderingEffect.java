package shadows.apotheosis.core.mobfx.potions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import shadows.apotheosis.mixin.LivingEntityMixin;

/**
 * Applied via {@link LivingEntityMixin}
 */
public class SunderingEffect extends MobEffect {

    public SunderingEffect() {
        super(MobEffectCategory.HARMFUL, 0x989898);
    }

}
