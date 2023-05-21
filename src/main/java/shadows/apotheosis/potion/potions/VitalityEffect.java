package shadows.apotheosis.potion.potions;

import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VitalityEffect extends MobEffect {

	public VitalityEffect() {
		super(MobEffectCategory.BENEFICIAL, ChatFormatting.RED.getColor());
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void heal(LivingHealEvent e) {
		if (e.getEntity().hasEffect(this)) {
			int level = e.getEntity().getEffect(this).getAmplifier() + 1;
			e.setAmount(e.getAmount() * (1 + level * 0.2F));
		}
	}

}