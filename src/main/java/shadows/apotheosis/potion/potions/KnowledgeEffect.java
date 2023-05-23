package shadows.apotheosis.potion.potions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.potion.PotionModule;

public class KnowledgeEffect extends MobEffect {

	public KnowledgeEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xF4EE42);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void xp(LivingExperienceDropEvent e) {
		if (e.getAttackingPlayer() != null && e.getAttackingPlayer().hasEffect(Apoth.Effects.KNOWLEDGE.get())) {
			int level = e.getAttackingPlayer().getEffect(Apoth.Effects.KNOWLEDGE.get()).getAmplifier() + 1;
			int curXp = e.getDroppedExperience();
			int newXp = curXp + e.getOriginalExperience() * level * PotionModule.knowledgeMult;
			e.setDroppedExperience(newXp);
		}
	}

}