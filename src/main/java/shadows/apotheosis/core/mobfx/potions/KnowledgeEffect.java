package shadows.apotheosis.core.mobfx.potions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.apotheosis.core.mobfx.MobFxLib;
import shadows.apotheosis.core.mobfx.api.MFEffects;

public class KnowledgeEffect extends MobEffect {

    public KnowledgeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF4EE42);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void xp(LivingExperienceDropEvent e) {
        if (e.getAttackingPlayer() != null && e.getAttackingPlayer().hasEffect(MFEffects.KNOWLEDGE.get())) {
            int level = e.getAttackingPlayer().getEffect(MFEffects.KNOWLEDGE.get()).getAmplifier() + 1;
            int curXp = e.getDroppedExperience();
            int newXp = curXp + e.getOriginalExperience() * level * MobFxLib.knowledgeMult;
            e.setDroppedExperience(newXp);
        }
    }

}
