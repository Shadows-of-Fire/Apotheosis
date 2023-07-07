package shadows.apotheosis.core.mobfx;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.core.mobfx.api.MFEffects;
import shadows.apotheosis.core.mobfx.potions.BleedingEffect;
import shadows.apotheosis.core.mobfx.potions.FlamingDetonationEffect;
import shadows.apotheosis.core.mobfx.potions.GrievousEffect;
import shadows.apotheosis.core.mobfx.potions.KnowledgeEffect;
import shadows.apotheosis.core.mobfx.potions.SunderingEffect;
import shadows.apotheosis.core.mobfx.potions.VitalityEffect;
import shadows.placebo.util.RegObjHelper;
import shadows.placebo.util.RegistryEvent.Register;

public class MobFxLib {

	public static final String MODID = "mobfx";
	public static final RegObjHelper REG_OBJS = new RegObjHelper(Apotheosis.MODID);

	public static int knowledgeMult = 4;

	@SubscribeEvent
	public void init(FMLCommonSetupEvent e) {
		MinecraftForge.EVENT_BUS.register(MFEffects.KNOWLEDGE.get());
	}

	@SubscribeEvent
	public void potions(Register<MobEffect> e) {
		e.getRegistry().register(new SunderingEffect(), "sundering");
		e.getRegistry().register(new KnowledgeEffect(), "knowledge");
		e.getRegistry().register(new VitalityEffect(), "vitality");
		e.getRegistry().register(new GrievousEffect(), "grievous");
		e.getRegistry().register(new BleedingEffect(), "bleeding");
		e.getRegistry().register(new FlamingDetonationEffect(), "detonation");
	}

}