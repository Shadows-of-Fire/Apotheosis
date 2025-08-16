package dev.shadowsoffire.apotheosis.data.gateways;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.compat.gateways.AffixItemReward;
import dev.shadowsoffire.apotheosis.compat.gateways.AffixWaveModifier;
import dev.shadowsoffire.apotheosis.compat.gateways.GemReward;
import dev.shadowsoffire.apotheosis.compat.gateways.InvaderWaveEntity;
import dev.shadowsoffire.apotheosis.compat.gateways.tiered_gate.TieredGateway;
import dev.shadowsoffire.apotheosis.data.Rarities;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.gateways.gate.Gateway;
import dev.shadowsoffire.gateways.gate.GatewayRegistry;
import dev.shadowsoffire.gateways.gate.Reward.ExperienceReward;
import dev.shadowsoffire.gateways.gate.Reward.StackReward;
import dev.shadowsoffire.gateways.gate.StandardWaveEntity;
import dev.shadowsoffire.gateways.gate.WaveModifier.AttributeModifier;
import dev.shadowsoffire.gateways.gate.WaveModifier.GearSetModifier;
import dev.shadowsoffire.gateways.gate.normal.NormalGateway;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public class ApothGateProvider extends DynamicRegistryProvider<Gateway> {

    public ApothGateProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries, GatewayRegistry.INSTANCE);
    }

    @Override
    public String getName() {
        return "Apotheosis Gateways";
    }

    @Override
    public void generate() {
        tieredGateway("tiered/frontier", b -> b
            .settings(c -> c
                .tier(WorldTier.FRONTIER)
                .size(NormalGateway.Size.SMALL)
                .color(0x33FF33))
            .rules(c -> c
                .lives(3)
                .requiresNearbyPlayer(true))
            .wave(w -> w
                .maxWaveTime(2400)
                .setupTime(100)
                .entity(StandardWaveEntity
                    .builder(EntityType.ZOMBIE)
                    .count(3)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("frontier/iron")))
                    .addModifier(AffixWaveModifier.create())
                    .desc("wave_entity.apotheosis.affixed_armored_zombie")
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.SKELETON)
                    .count(2)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("frontier/ranged/chain")))
                    .addModifier(AffixWaveModifier.create())
                    .desc("wave_entity.apotheosis.affixed_armored_skeleton")
                    .build())
                .reward(AffixItemReward.create())
                .reward(GemReward.create()))
            .wave(w -> w
                .maxWaveTime(2400)
                .setupTime(160)
                .entity(StandardWaveEntity
                    .builder(EntityType.ZOMBIE)
                    .count(3)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("frontier/iron")))
                    .addModifier(AffixWaveModifier.create())
                    .desc("wave_entity.apotheosis.affixed_armored_zombie")
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.SKELETON)
                    .count(2)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("frontier/ranged/chain")))
                    .addModifier(AffixWaveModifier.create())
                    .desc("wave_entity.apotheosis.affixed_armored_skeleton")
                    .build())
                .reward(AffixItemReward.create(Rarities.UNCOMMON))
                .reward(GemReward.create(Purity.CHIPPED))
                .reward(new ExperienceReward(350, 25))
                .modifier(AttributeModifier.create(Attributes.MAX_HEALTH, Operation.ADD_MULTIPLIED_TOTAL, 0.10F))
                .modifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 2.5F))
                .modifier(AttributeModifier.create(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.15F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.PROJECTILE_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.15F))
                .modifier(AttributeModifier.create(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, 0.10F))
                .modifier(AttributeModifier.create(Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_TOTAL, 0.05F)))
            .wave(w -> w
                .maxWaveTime(2400)
                .setupTime(160)
                .entity(InvaderWaveEntity.createRandom(2))
                .entity(StandardWaveEntity
                    .builder(EntityType.ZOMBIE)
                    .count(3)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("frontier/iron")))
                    .addModifier(AffixWaveModifier.create())
                    .desc("wave_entity.apotheosis.affixed_armored_zombie")
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.SKELETON)
                    .count(2)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("frontier/ranged/chain")))
                    .addModifier(AffixWaveModifier.create())
                    .desc("wave_entity.apotheosis.affixed_armored_skeleton")
                    .build())
                .reward(AffixItemReward.create(Rarities.UNCOMMON, Rarities.RARE))
                .reward(GemReward.create(Purity.CHIPPED, Purity.FLAWED))
                .reward(new ExperienceReward(650, 25))
                .modifier(AttributeModifier.create(Attributes.MAX_HEALTH, Operation.ADD_MULTIPLIED_TOTAL, 0.15F))
                .modifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 3.5F))
                .modifier(AttributeModifier.create(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.25F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.PROJECTILE_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.25F))
                .modifier(AttributeModifier.create(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, 0.20F))
                .modifier(AttributeModifier.create(Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_TOTAL, 0.10F)))
            .keyReward(new StackReward(new ItemStack(Apoth.Items.GEM_DUST, 16)))
            .keyReward(new StackReward(new ItemStack(Apoth.Items.UNCOMMON_MATERIAL, 8)))
            .keyReward(new StackReward(new ItemStack(Apoth.Items.SIGIL_OF_SOCKETING, 2))));
    }

    private void tieredGateway(String path, UnaryOperator<TieredGateway.Builder> config) {
        this.add(Apotheosis.loc(path), config.apply(TieredGateway.builder()).build());
    }

}
