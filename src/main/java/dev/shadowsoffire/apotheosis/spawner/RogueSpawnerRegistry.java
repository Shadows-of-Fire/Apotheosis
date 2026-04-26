package dev.shadowsoffire.apotheosis.spawner;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.placebo.dynreg.RegistrySerializer;
import dev.shadowsoffire.placebo.dynreg.WeightedDynamicRegistry;

public class RogueSpawnerRegistry extends WeightedDynamicRegistry<RogueSpawner> {

    public static final RogueSpawnerRegistry INSTANCE = new RogueSpawnerRegistry();

    public RogueSpawnerRegistry() {
        super(Apotheosis.LOGGER, Apotheosis.loc("rogue_spawners"), RegistrySerializer.simple(RogueSpawner.CODEC));
    }

}
