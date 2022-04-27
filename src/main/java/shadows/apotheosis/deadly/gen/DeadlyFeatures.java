package shadows.apotheosis.deadly.gen;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
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

import static shadows.apotheosis.deadly.gen.DeadlyConfiguredFeatures.TOME_TOWER_PLACED;

public final class DeadlyFeatures {
    private DeadlyFeatures() {}

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Apotheosis.MODID);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> TOME_TOWER = register("tome_tower", TomeTowerFeature::new);

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
        if(DeadlyConfig.tomeTowerChance != 0 && checkBiome(event.getName())) {
            event.getGeneration().getFeatures(GenerationStep.Decoration.SURFACE_STRUCTURES).add(Holder.direct(TOME_TOWER_PLACED));
        }
    }

    static boolean checkBiome(ResourceLocation biomeRL){
        return !DeadlyConfig.BIOME_BLACKLIST.contains(biomeRL);
    }

    private static <T extends Feature<?>> RegistryObject<T> register(String name, Supplier<T> feature) {
        return FEATURES.register(name, feature);
    }
}
