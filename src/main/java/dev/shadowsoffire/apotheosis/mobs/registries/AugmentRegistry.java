package dev.shadowsoffire.apotheosis.mobs.registries;

import java.util.Set;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.mobs.types.Augmentation;
import dev.shadowsoffire.placebo.dynreg.DynamicRegistry;
import dev.shadowsoffire.placebo.dynreg.RegistrySerializer;

public class AugmentRegistry extends DynamicRegistry<Augmentation> {

    public static final AugmentRegistry INSTANCE = new AugmentRegistry();

    public AugmentRegistry() {
        super(Apotheosis.LOGGER, Apotheosis.loc("apothic_augments"), RegistrySerializer.simple(Augmentation.CODEC));
    }

    public static Set<Augmentation> getAll() {
        return INSTANCE.registry.values();
    }

}
