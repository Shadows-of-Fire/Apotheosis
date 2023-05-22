package shadows.apotheosis.potion.potions;

import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GrievousEffect extends MobEffect {

	public GrievousEffect() {
		super(MobEffectCategory.HARMFUL, ChatFormatting.DARK_RED.getColor());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void heal(LivingHealEvent e) {
		if (e.getEntity().hasEffect(this)) {
			int level = e.getEntity().getEffect(this).getAmplifier() + 1;
			e.setAmount(e.getAmount() * Math.max(0, 1 - level * 0.4F));
			if (e.getAmount() <= 0.001F) e.setCanceled(true);
		}
	}

}