package shadows.deadly;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.Apotheosis;
import shadows.Apotheosis.ApotheosisConstruction;
import shadows.Apotheosis.ApotheosisSetup;
import shadows.advancement.AdvancementTriggers;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.gen.BossFeature;
import shadows.deadly.gen.BrutalSpawner;
import shadows.deadly.gen.SwarmSpawner;
import shadows.deadly.gen.WorldGenerator;
import shadows.placebo.config.Configuration;
import shadows.util.ArmorSet;

public class DeadlyModule {

	public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Deadly");

	@SubscribeEvent
	public void preInit(ApotheosisConstruction e) {
		DeadlyConfig.config = new Configuration(new File(Apotheosis.configDir, "deadly.cfg"));
	}

	@SubscribeEvent
	public void init(ApotheosisSetup e) {
		DeadlyConfig.init();
		BrutalSpawner.init();
		BossFeature.init();
		DeadlyLoot.init();
		SwarmSpawner.init();
		WorldGenerator.init();
		ArmorSet.sortSets();
		MinecraftForge.EVENT_BUS.addListener(this::death);
	}

	public void death(LivingDeathEvent e) {
		if (e.getEntity().getEntityData().getBoolean("apoth_boss")) {
			DamageSource source = e.getSource();
			if (source.getTrueSource() instanceof ServerPlayerEntity) {
				AdvancementTriggers.BOSS_TRIGGER.trigger(((ServerPlayerEntity) source.getTrueSource()).getAdvancements());
			}
		}
	}

}