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
import dev.shadowsoffire.gateways.gate.Reward.CountedReward;
import dev.shadowsoffire.gateways.gate.Reward.ExperienceReward;
import dev.shadowsoffire.gateways.gate.Reward.StackReward;
import dev.shadowsoffire.gateways.gate.StandardWaveEntity;
import dev.shadowsoffire.gateways.gate.WaveModifier.AttributeModifier;
import dev.shadowsoffire.gateways.gate.WaveModifier.GearSetModifier;
import dev.shadowsoffire.gateways.gate.WaveModifier.LootTableModifier;
import dev.shadowsoffire.gateways.gate.normal.NormalGateway;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
                .modifier(LootTableModifier.createEmpty())
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
                .reward(new ExperienceReward(350, 25))
                .modifier(LootTableModifier.createEmpty())
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
                .reward(new ExperienceReward(650, 25))
                .modifier(LootTableModifier.createEmpty())
                .modifier(AttributeModifier.create(Attributes.MAX_HEALTH, Operation.ADD_MULTIPLIED_TOTAL, 0.15F))
                .modifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 3.5F))
                .modifier(AttributeModifier.create(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.25F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.PROJECTILE_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.25F))
                .modifier(AttributeModifier.create(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, 0.20F))
                .modifier(AttributeModifier.create(Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_TOTAL, 0.10F)))
            .keyReward(new CountedReward(AffixItemReward.create(Rarities.UNCOMMON, Rarities.RARE), 3))
            .keyReward(new CountedReward(GemReward.create(Purity.CHIPPED, Purity.FLAWED), 5))
            .keyReward(new StackReward(new ItemStack(Apoth.Items.GEM_DUST, 16)))
            .keyReward(new StackReward(new ItemStack(Apoth.Items.UNCOMMON_MATERIAL, 8)))
            .keyReward(new StackReward(new ItemStack(Apoth.Items.SIGIL_OF_SOCKETING, 2))));

        tieredGateway("tiered/ascent", b -> b
            .settings(c -> c
                .tier(WorldTier.ASCENT)
                .size(NormalGateway.Size.MEDIUM)
                .color(0x5555FF))
            .rules(c -> c
                .lives(3)
                .requiresNearbyPlayer(true))
            .wave(w -> w
                .maxWaveTime(2400)
                .setupTime(100)
                .entity(StandardWaveEntity
                    .builder(EntityType.WITCH)
                    .count(1)
                    .addModifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 12F))
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.HUSK)
                    .count(3)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("ascent/diamond")))
                    .addModifier(AffixWaveModifier.create())
                    .desc("wave_entity.apotheosis.affixed_armored_husk")
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.STRAY)
                    .count(2)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("ascent/ranged/iron")))
                    .addModifier(AffixWaveModifier.create())
                    .desc("wave_entity.apotheosis.affixed_armored_stray")
                    .build())
                .reward(new ExperienceReward(450, 25))
                .modifier(LootTableModifier.createEmpty()))

            .wave(w -> w
                .maxWaveTime(3200)
                .setupTime(200)
                .entity(StandardWaveEntity
                    .builder(EntityType.POLAR_BEAR)
                    .count(1)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("ascent/diamond")))
                    .addModifier(AffixWaveModifier.create())
                    .desc("wave_entity.apotheosis.affixed_polar_bear")
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.WOLF)
                    .count(3)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("ascent/iron")))
                    .addModifier(AffixWaveModifier.create())
                    .desc("wave_entity.apotheosis.affixed_wolf")
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.BOGGED)
                    .count(2)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("ascent/ranged/iron")))
                    .addModifier(AffixWaveModifier.create())
                    .desc("wave_entity.apotheosis.affixed_armored_bogged")
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.PHANTOM)
                    .count(2)
                    .addModifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 6F))
                    .addModifier(AttributeModifier.create(ALObjects.Attributes.LIFE_STEAL, Operation.ADD_VALUE, 0.25F))
                    .desc("wave_entity.apotheosis.vampiric_phantom")
                    .build())
                .reward(new ExperienceReward(650, 25))
                .modifier(LootTableModifier.createEmpty())
                .modifier(AttributeModifier.create(Attributes.MAX_HEALTH, Operation.ADD_MULTIPLIED_TOTAL, 0.20F))
                .modifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 4F))
                .modifier(AttributeModifier.create(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.25F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.PROJECTILE_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.25F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.ARMOR_SHRED, Operation.ADD_VALUE, 0.35F))
                .modifier(AttributeModifier.create(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, 0.30F))
                .modifier(AttributeModifier.create(Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_TOTAL, 0.10F)))
            .wave(w -> w
                .maxWaveTime(4800)
                .setupTime(240)
                .entity(InvaderWaveEntity.createRandom(3))
                .entity(StandardWaveEntity
                    .builder(EntityType.WITHER_SKELETON)
                    .count(5)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("gateway_only/nether_herald")))
                    .addModifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 20F))
                    .addModifier(AffixWaveModifier.create())
                    .desc("wave_entity.apotheosis.nether_herald")
                    .build())
                .reward(new ExperienceReward(850, 25))
                .modifier(LootTableModifier.createEmpty())
                .modifier(AttributeModifier.create(Attributes.MAX_HEALTH, Operation.ADD_MULTIPLIED_TOTAL, 0.25F))
                .modifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 5F))
                .modifier(AttributeModifier.create(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.25F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.PROJECTILE_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.25F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.ARMOR_SHRED, Operation.ADD_VALUE, 0.40F))
                .modifier(AttributeModifier.create(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, 0.30F))
                .modifier(AttributeModifier.create(Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_TOTAL, 0.10F)))
            .keyReward(new CountedReward(AffixItemReward.create(Rarities.RARE, Rarities.EPIC), 3))
            .keyReward(new CountedReward(GemReward.create(Purity.FLAWED, Purity.NORMAL), 5))
            .keyReward(new StackReward(new ItemStack(Apoth.Items.GEM_DUST, 24)))
            .keyReward(new StackReward(new ItemStack(Apoth.Items.RARE_MATERIAL, 12)))
            .keyReward(new StackReward(new ItemStack(Apoth.Items.SIGIL_OF_SOCKETING, 2)))
            .keyReward(new StackReward(new ItemStack(Items.WITHER_SKELETON_SKULL, 3))));
    }

    private void tieredGateway(String path, UnaryOperator<TieredGateway.Builder> config) {
        this.add(Apotheosis.loc(path), config.apply(TieredGateway.builder()).build());
    }

}
