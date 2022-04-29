package shadows.apotheosis.deadly.gen;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.config.DeadlyConfig;

import java.util.function.Supplier;

import static shadows.apotheosis.deadly.gen.DeadlyConfiguredFeatures.*;

public final class DeadlyFeatures {
    private DeadlyFeatures() {}

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Apotheosis.MODID);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> TOME_TOWER = register("tome_tower", TomeTowerFeature::new);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ROGUE_SPAWNER = register("rogue_spawner", RogueSpawnerFeature::new);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> BOSS_DUNGEON = register("boss_dungeon", BossDungeonFeature::new);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> BOSS_DUNGEON_2 = register("boss_dungeon_2", BossDungeonFeature2::new);

    public static void init () {
        FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(DeadlyFeatures::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(DeadlyFeatures::onBiomeLoad);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            DeadlyConfiguredFeatures.registerConfiguredFeatures();
            DeadlyConfiguredFeatures.registerPlacedFeatures();
        });
    }

    private static void onBiomeLoad(BiomeLoadingEvent event) {
        if (!DeadlyConfig.BIOME_BLACKLIST.contains(event.getName())) {
            event.getGeneration()
                    .addFeature(Decoration.UNDERGROUND_STRUCTURES, Holder.direct(ROGUE_SPAWNER_PLACED))
                    .addFeature(Decoration.UNDERGROUND_STRUCTURES, Holder.direct(BOSS_DUNGEON_PLACED))
                    .addFeature(Decoration.UNDERGROUND_STRUCTURES, Holder.direct(BOSS_DUNGEON_2_PLACED));
//                    .addFeature(Decoration.UNDERGROUND_STRUCTURES, Holder.direct(ORE_TROVE_PLACED));

            if (Apotheosis.enableEnch && DeadlyConfig.tomeTowerChance > 0)
                event.getGeneration().addFeature(Decoration.SURFACE_STRUCTURES, Holder.direct(TOME_TOWER_PLACED));
        }
    }
    private static <T extends Feature<?>> RegistryObject<T> register(String name, Supplier<T> feature) {
        return FEATURES.register(name, feature);
    }
}
