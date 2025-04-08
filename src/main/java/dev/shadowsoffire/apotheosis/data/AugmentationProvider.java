package dev.shadowsoffire.apotheosis.data;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.mobs.registries.AugmentRegistry;
import dev.shadowsoffire.apotheosis.mobs.types.Augmentation;
import dev.shadowsoffire.apotheosis.mobs.util.EntityModifier.RandomAffixItemModifier;
import dev.shadowsoffire.apotheosis.mobs.util.SpawnCondition.IsMonsterCondition;
import dev.shadowsoffire.apotheosis.mobs.util.SpawnCondition.NotCondition;
import dev.shadowsoffire.apotheosis.mobs.util.SpawnCondition.SpawnTypeCondition;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.MobSpawnType;

public class AugmentationProvider extends DynamicRegistryProvider<Augmentation> {

    public AugmentationProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries, AugmentRegistry.INSTANCE);
    }

    @Override
    public String getName() {
        return "Augmentations";
    }

    @Override
    public void generate() {
        add("random_affix_items", b -> b
            .chance(0.12F)
            .conditions(
                new NotCondition(SpawnTypeCondition.of(MobSpawnType.SPAWNER, MobSpawnType.TRIAL_SPAWNER)),
                new IsMonsterCondition())
            .modifiers(new RandomAffixItemModifier()));
    }

    private void add(String path, UnaryOperator<Augmentation.Builder> config) {
        this.add(Apotheosis.loc(path), config.apply(Augmentation.builder()).build());
    }
}
