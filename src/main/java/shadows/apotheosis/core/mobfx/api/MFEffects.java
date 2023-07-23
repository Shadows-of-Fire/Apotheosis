package shadows.apotheosis.core.mobfx.api;

import net.minecraftforge.registries.RegistryObject;
import shadows.apotheosis.core.mobfx.MobFxLib;
import shadows.apotheosis.core.mobfx.potions.BleedingEffect;
import shadows.apotheosis.core.mobfx.potions.GrievousEffect;
import shadows.apotheosis.core.mobfx.potions.KnowledgeEffect;
import shadows.apotheosis.core.mobfx.potions.SunderingEffect;
import shadows.apotheosis.core.mobfx.potions.VitalityEffect;

public class MFEffects {

    /**
     * Sundering is the inverse of resistance. It increases damage taken by 20%/level.<br>
     * Each point of sundering cancels out a single point of resistance, if present.
     */
    public static final RegistryObject<SunderingEffect> SUNDERING = MobFxLib.REG_OBJS.effect("sundering");

    /**
     * Ancient Knowledge multiplies experience dropped by mobs by level * {@link MobFxLib#knowledgeMult}.<br>
     * The multiplier is configurable.
     */
    public static final RegistryObject<KnowledgeEffect> KNOWLEDGE = MobFxLib.REG_OBJS.effect("knowledge");

    /**
     * Bursting Vitality increases healing received by 20%/level.
     */
    public static final RegistryObject<VitalityEffect> VITALITY = MobFxLib.REG_OBJS.effect("vitality");

    /**
     * Grievous Wounds reduces healing received by 40%/level.
     */
    public static final RegistryObject<GrievousEffect> GRIEVOUS = MobFxLib.REG_OBJS.effect("grievous");

    /**
     * Bleeding inflicts 1 + level damage every two seconds. Things that apply bleeding usually stack.
     */
    public static final RegistryObject<BleedingEffect> BLEEDING = MobFxLib.REG_OBJS.effect("bleeding");

}
