package dev.shadowsoffire.apotheosis.data.gateways;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.compat.gateways.AffixItemReward;
import dev.shadowsoffire.apotheosis.compat.gateways.AffixWaveModifier;
import dev.shadowsoffire.apotheosis.compat.gateways.EliteWaveEntity;
import dev.shadowsoffire.apotheosis.compat.gateways.GemReward;
import dev.shadowsoffire.apotheosis.compat.gateways.InvaderWaveEntity;
import dev.shadowsoffire.apotheosis.compat.gateways.PassengerWaveModifier;
import dev.shadowsoffire.apotheosis.compat.gateways.TrueRandomGemReward;
import dev.shadowsoffire.apotheosis.compat.gateways.TrueRandomInvaderWaveEntity;
import dev.shadowsoffire.apotheosis.compat.gateways.tiered_gate.TieredGateway;
import dev.shadowsoffire.apotheosis.data.Rarities;
import dev.shadowsoffire.apotheosis.mobs.registries.EliteRegistry;
import dev.shadowsoffire.apotheosis.mobs.types.Elite;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.gateways.GatewayObjects;
import dev.shadowsoffire.gateways.gate.BossEventSettings;
import dev.shadowsoffire.gateways.gate.Gateway;
import dev.shadowsoffire.gateways.gate.GatewayRegistry;
import dev.shadowsoffire.gateways.gate.Reward.CountedReward;
import dev.shadowsoffire.gateways.gate.Reward.ExperienceReward;
import dev.shadowsoffire.gateways.gate.Reward.StackReward;
import dev.shadowsoffire.gateways.gate.SpawnAlgorithms;
import dev.shadowsoffire.gateways.gate.StandardWaveEntity;
import dev.shadowsoffire.gateways.gate.WaveModifier.AttributeModifier;
import dev.shadowsoffire.gateways.gate.WaveModifier.EffectModifier;
import dev.shadowsoffire.gateways.gate.WaveModifier.GearSetModifier;
import dev.shadowsoffire.gateways.gate.WaveModifier.LootTableModifier;
import dev.shadowsoffire.gateways.gate.endless.ApplicationMode.AfterEveryNWaves;
import dev.shadowsoffire.gateways.gate.endless.ApplicationMode.OnlyOnEveryNWaves;
import dev.shadowsoffire.gateways.gate.endless.EndlessGateway;
import dev.shadowsoffire.gateways.gate.normal.NormalGateway;
import dev.shadowsoffire.gateways.item.GatePearlItem;
import dev.shadowsoffire.placebo.color.GradientColor;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
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
                .color(0x33FF33)
                .soundtrack(Apoth.Sounds.MUSIC_DISC_SHIMMER))
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
                    .addModifier(LootTableModifier.createEmpty())
                    .desc(Apotheosis.langKey("wave_entity", "affixed_armored_zombie"))
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.SKELETON)
                    .count(2)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("frontier/ranged/chain")))
                    .addModifier(AffixWaveModifier.create())
                    .addModifier(LootTableModifier.createEmpty())
                    .desc(Apotheosis.langKey("wave_entity", "affixed_armored_skeleton"))
                    .build()))
            .wave(w -> w
                .maxWaveTime(2400)
                .setupTime(160)
                .entity(StandardWaveEntity
                    .builder(EntityType.ZOMBIE)
                    .count(3)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("frontier/iron")))
                    .addModifier(AffixWaveModifier.create())
                    .desc(Apotheosis.langKey("wave_entity", "affixed_armored_zombie"))
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.SKELETON)
                    .count(2)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("frontier/ranged/chain")))
                    .addModifier(AffixWaveModifier.create())
                    .desc(Apotheosis.langKey("wave_entity", "affixed_armored_skeleton"))
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
                .maxWaveTime(3200)
                .setupTime(160)
                .entity(InvaderWaveEntity.createRandom(2))
                .entity(StandardWaveEntity
                    .builder(EntityType.ZOMBIE)
                    .count(3)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("frontier/iron")))
                    .addModifier(AffixWaveModifier.create())
                    .desc(Apotheosis.langKey("wave_entity", "affixed_armored_zombie"))
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.SKELETON)
                    .count(2)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("frontier/ranged/chain")))
                    .addModifier(AffixWaveModifier.create())
                    .desc(Apotheosis.langKey("wave_entity", "affixed_armored_skeleton"))
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
                .color(0x5555FF)
                .soundtrack(Apoth.Sounds.MUSIC_DISC_FLASH))
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
                    .desc(Apotheosis.langKey("wave_entity", "affixed_armored_husk"))
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.STRAY)
                    .count(2)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("ascent/ranged/iron")))
                    .addModifier(AffixWaveModifier.create())
                    .desc(Apotheosis.langKey("wave_entity", "affixed_armored_stray"))
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
                    .desc(Apotheosis.langKey("wave_entity", "affixed_polar_bear"))
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.WOLF)
                    .count(3)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("ascent/iron")))
                    .addModifier(AffixWaveModifier.create())
                    .desc(Apotheosis.langKey("wave_entity", "affixed_wolf"))
                    .nbt(t -> {
                        t.putInt("AngerTime", 9999999);
                        return t;
                    })
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.BOGGED)
                    .count(2)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("ascent/ranged/iron")))
                    .addModifier(AffixWaveModifier.create())
                    .desc(Apotheosis.langKey("wave_entity", "affixed_armored_bogged"))
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.PHANTOM)
                    .count(2)
                    .addModifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 6F))
                    .addModifier(AttributeModifier.create(ALObjects.Attributes.LIFE_STEAL, Operation.ADD_VALUE, 0.25F))
                    .desc(Apotheosis.langKey("wave_entity", "vampiric_phantom"))
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
                    .desc(Apotheosis.langKey("wave_entity", "nether_herald"))
                    .build())
                .reward(new ExperienceReward(850, 25))
                .modifier(LootTableModifier.createEmpty())
                .modifier(AttributeModifier.create(Attributes.MAX_HEALTH, Operation.ADD_MULTIPLIED_TOTAL, 0.25F))
                .modifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 5F))
                .modifier(AttributeModifier.create(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.25F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.PROJECTILE_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.25F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.ARMOR_SHRED, Operation.ADD_VALUE, 0.40F))
                .modifier(AttributeModifier.create(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, 0.08F)))
            .keyReward(new CountedReward(AffixItemReward.create(Rarities.RARE, Rarities.EPIC), 3))
            .keyReward(new CountedReward(GemReward.create(Purity.FLAWED, Purity.NORMAL), 5))
            .keyReward(new StackReward(new ItemStack(Apoth.Items.GEM_DUST, 24)))
            .keyReward(new StackReward(new ItemStack(Apoth.Items.RARE_MATERIAL, 12)))
            .keyReward(new StackReward(new ItemStack(Apoth.Items.SIGIL_OF_SOCKETING, 2)))
            .keyReward(new StackReward(new ItemStack(Items.WITHER_SKELETON_SKULL, 3))));

        tieredGateway("tiered/summit", b -> b
            .settings(c -> c
                .tier(WorldTier.SUMMIT)
                .size(NormalGateway.Size.MEDIUM)
                .color(0xBB00BB)
                .soundtrack(Ench.Sounds.MUSIC_DISC_ARCANA))
            .rules(c -> c
                .lives(3)
                .requiresNearbyPlayer(true)
                .spawnRange(24)
                .leashRange(48))
            .wave(w -> w
                .maxWaveTime(3000)
                .setupTime(100)
                .entity(StandardWaveEntity
                    .builder(EntityType.GHAST)
                    .count(3)
                    .addModifier(AttributeModifier.create(Attributes.MAX_HEALTH, Operation.ADD_VALUE, 35F))
                    .addModifier(AttributeModifier.create(Attributes.ARMOR_TOUGHNESS, Operation.ADD_VALUE, 10F))
                    .addModifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 12F))
                    .addModifier(AttributeModifier.create(ALObjects.Attributes.PROJECTILE_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.55F))
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.ZOMBIFIED_PIGLIN)
                    .count(3)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("summit/enchanted_diamond")))
                    .addModifier(AffixWaveModifier.create())
                    .desc(Apotheosis.langKey("wave_entity", "affixed_armored_zombified_piglin"))
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.WITHER_SKELETON)
                    .count(2)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("summit/ranged/enchanted_iron")))
                    .addModifier(AffixWaveModifier.create())
                    .desc(Apotheosis.langKey("wave_entity", "affixed_armored_wither_skeleton"))
                    .build())
                .reward(new ExperienceReward(1050, 50))
                .modifier(LootTableModifier.createEmpty()))
            .wave(w -> w
                .maxWaveTime(3200)
                .setupTime(200)
                .entity(StandardWaveEntity
                    .builder(EntityType.GHAST)
                    .count(2)
                    .addModifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 12F))
                    .addModifier(AttributeModifier.create(ALObjects.Attributes.PROJECTILE_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.55F))
                    .addModifier(AttributeModifier.create(Attributes.SCALE, Operation.ADD_MULTIPLIED_TOTAL, -0.75F))
                    .addModifier(PassengerWaveModifier.create(EntityType.STRAY, c -> c
                        .finalizeSpawn(false)
                        .addModifier(GearSetModifier.create(Apotheosis.loc("summit/ranged/enchanted_iron")))
                        .addModifier(AffixWaveModifier.create())))
                    .desc(Apotheosis.langKey("wave_entity", "ghast_rider"))
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.PIGLIN_BRUTE) // Brutes will try to kill wither skeletons, so don't use them here.
                    .count(3)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("summit/netherite")))
                    .addModifier(AffixWaveModifier.create())
                    .desc(Apotheosis.langKey("wave_entity", "affixed_armored_piglin_brute"))
                    .nbt(c -> {
                        c.putBoolean("IsImmuneToZombification", true);
                        return c;
                    })
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.STRAY)
                    .count(2)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("summit/ranged/enchanted_iron")))
                    .addModifier(AffixWaveModifier.create())
                    .desc(Apotheosis.langKey("wave_entity", "affixed_armored_stray"))
                    .build())
                .reward(new ExperienceReward(1250, 50))
                .modifier(LootTableModifier.createEmpty())
                .modifier(AttributeModifier.create(Attributes.MAX_HEALTH, Operation.ADD_MULTIPLIED_TOTAL, 0.20F))
                .modifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 4F))
                .modifier(AttributeModifier.create(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.25F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.PROJECTILE_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.25F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.ARMOR_SHRED, Operation.ADD_VALUE, 0.35F))
                .modifier(AttributeModifier.create(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, 0.30F))
                .modifier(AttributeModifier.create(Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_TOTAL, 0.10F)))
            .wave(w -> w
                .maxWaveTime(6400)
                .setupTime(240)
                .entity(InvaderWaveEntity.createRandom(3))
                .entity(StandardWaveEntity
                    .builder(EntityType.PIGLIN_BRUTE)
                    .count(5)
                    .finalizeSpawn(false)
                    .addModifier(GearSetModifier.create(Apotheosis.loc("gateway_only/bastion_guard")))
                    .addModifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 20F))
                    .addModifier(AffixWaveModifier.create())
                    .desc(Apotheosis.langKey("wave_entity", "bastion_guard"))
                    .nbt(c -> {
                        c.putBoolean("IsImmuneToZombification", true);
                        return c;
                    })
                    .build())
                .reward(new ExperienceReward(1550, 50))
                .modifier(LootTableModifier.createEmpty())
                .modifier(AttributeModifier.create(Attributes.MAX_HEALTH, Operation.ADD_MULTIPLIED_TOTAL, 0.25F))
                .modifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 5F))
                .modifier(AttributeModifier.create(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.15F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.PROJECTILE_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.25F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.ARMOR_SHRED, Operation.ADD_VALUE, 0.40F))
                .modifier(AttributeModifier.create(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, 0.08F)))
            .keyReward(new CountedReward(AffixItemReward.create(Rarities.EPIC, Rarities.MYTHIC), 3))
            .keyReward(new CountedReward(GemReward.create(Purity.NORMAL, Purity.FLAWLESS), 5))
            .keyReward(new StackReward(new ItemStack(Apoth.Items.GEM_DUST, 48)))
            .keyReward(new StackReward(new ItemStack(Apoth.Items.EPIC_MATERIAL, 24)))
            .keyReward(new StackReward(new ItemStack(Apoth.Items.SIGIL_OF_SOCKETING, 6)))
            .keyReward(new StackReward(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1), Apotheosis.langKey("reward", "netherite_smithing_template"))));

        endlessGateway("endless_invader", b -> b
            .color(GradientColor.RAINBOW)
            .size(Gateway.Size.LARGE)
            .bossSettings(new BossEventSettings(BossEventSettings.Mode.NAME_PLATE, false))
            .spawnAlgo(SpawnAlgorithms.INWARD_SPIRAL)
            .soundtrack(Apoth.Sounds.MUSIC_DISC_GLIMMER)
            .rules(c -> c
                .lives(3)
                .requiresNearbyPlayer(true)
                .allowDiscarding(true)
                .spacing(32))
            .baseWave(w -> w
                .maxWaveTime(3600)
                .setupTime(200)
                .entity(TrueRandomInvaderWaveEntity.createRandom(3))
                .modifier(AttributeModifier.create(ALObjects.Attributes.ARMOR_SHRED, Operation.ADD_VALUE, 0.40F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.PROT_SHRED, Operation.ADD_VALUE, 0.20F))
                .modifier(AttributeModifier.create(Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_TOTAL, 0.02F))
                .reward(new CountedReward(AffixItemReward.create(Rarities.MYTHIC), 1))
                .reward(new CountedReward(TrueRandomGemReward.create(Purity.PERFECT), 3))
                .reward(new ExperienceReward(25000, 1000)))
            .modifier(m -> m
                .applicationMode(new AfterEveryNWaves(3, 100))
                .entity(TrueRandomInvaderWaveEntity.createRandom(1))
                .modifier(LootTableModifier.createEmpty())
                .modifier(AttributeModifier.create(Attributes.MAX_HEALTH, Operation.ADD_MULTIPLIED_TOTAL, 0.25F))
                .modifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_MULTIPLIED_TOTAL, 0.10F))
                .modifier(AttributeModifier.create(Attributes.ARMOR_TOUGHNESS, Operation.ADD_MULTIPLIED_TOTAL, 0.03F))
                .modifier(AttributeModifier.create(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.25F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.PROJECTILE_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.25F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.ARMOR_SHRED, Operation.ADD_MULTIPLIED_TOTAL, 0.08F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.PROT_SHRED, Operation.ADD_MULTIPLIED_TOTAL, 0.08F))
                .modifier(AttributeModifier.create(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_MULTIPLIED_TOTAL, 0.05F))
                .reward(new StackReward(new ItemStack(Apoth.Items.MYTHIC_MATERIAL, 16)))
                .reward(new StackReward(new ItemStack(Apoth.Items.GEM_DUST, 16)))
                .reward(new ExperienceReward(25000, 1000))
                .setupTime(-5)
                .maxWaveTime(-25))
            .modifier(m -> m
                .applicationMode(new OnlyOnEveryNWaves(100))
                .reward(new StackReward(new ItemStack(Apoth.Items.SIGIL_OF_SUPREMACY)))
                .modifier(AttributeModifier.create(Attributes.MAX_HEALTH, Operation.ADD_MULTIPLIED_TOTAL, 1F))));

        tieredGateway("tiered/pinnacle", b -> b
            .settings(c -> c
                .tier(WorldTier.PINNACLE)
                .size(NormalGateway.Size.LARGE)
                .color(0xED7014)
                .soundtrack(Ench.Sounds.MUSIC_DISC_QUANTA))
            .rules(c -> c
                .lives(3)
                .requiresNearbyPlayer(true)
                .spawnRange(24)
                .leashRange(48))
            .wave(w -> w
                .maxWaveTime(3000)
                .setupTime(100)
                .entity(StandardWaveEntity
                    .builder(EntityType.ZOMBIE)
                    .count(3)
                    .addModifier(AttributeModifier.create(Attributes.MAX_HEALTH, Operation.ADD_VALUE, 80F))
                    .addModifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 12F))
                    .addModifier(AttributeModifier.create(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.55F))
                    .addModifier(AttributeModifier.create(Attributes.SCALE, Operation.ADD_MULTIPLIED_TOTAL, 5F))
                    .addModifier(EffectModifier.create(MobEffects.FIRE_RESISTANCE, 0, true, false))
                    .addModifier(GearSetModifier.create(Apotheosis.loc("pinnacle/enchanted_netherite")))
                    .addModifier(AffixWaveModifier.create())
                    .addModifier(LootTableModifier.createEmpty())
                    .desc("entity.minecraft.giant")
                    .nbt(t -> {
                        Component name = Component.translatable("entity.minecraft.giant");
                        t.put("CustomName", ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, name).getOrThrow());
                        return t;
                    })
                    .build())
                .reward(new ExperienceReward(2000, 100)))
            .wave(w -> w
                .maxWaveTime(3200)
                .setupTime(200)
                .entity(StandardWaveEntity
                    .builder(EntityType.ILLUSIONER)
                    .count(2)
                    .addModifier(AttributeModifier.create(Attributes.ARMOR_TOUGHNESS, Operation.ADD_VALUE, 25F))
                    .addModifier(AttributeModifier.create(ALObjects.Attributes.PROJECTILE_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.55F))
                    .addModifier(GearSetModifier.create(Apotheosis.loc("pinnacle/ranged/enchanted_netherite")))
                    .addModifier(AffixWaveModifier.create())
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.EVOKER)
                    .count(2)
                    .addModifier(AttributeModifier.create(Attributes.ARMOR_TOUGHNESS, Operation.ADD_VALUE, 25F))
                    .addModifier(AttributeModifier.create(ALObjects.Attributes.PROJECTILE_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.55F))
                    .addModifier(GearSetModifier.create(Apotheosis.loc("pinnacle/ranged/enchanted_netherite")))
                    .addModifier(AffixWaveModifier.create())
                    .build())
                .entity(StandardWaveEntity
                    .builder(EntityType.RABBIT)
                    .count(3)
                    .finalizeSpawn(false)
                    .addModifier(AttributeModifier.create(Attributes.MAX_HEALTH, Operation.ADD_VALUE, 15F))
                    .addModifier(GearSetModifier.create(Apotheosis.loc("pinnacle/enchanted_netherite")))
                    .addModifier(AffixWaveModifier.create())
                    .addModifier(AffixWaveModifier.create())
                    .desc(Apotheosis.langKey("wave_entity", "killer_bunny"))
                    .nbt(c -> {
                        c.putInt("RabbitType", 99);
                        return c;
                    })
                    .build())
                .reward(new ExperienceReward(2000, 100))
                .modifier(LootTableModifier.createEmpty())
                .modifier(AttributeModifier.create(Attributes.MAX_HEALTH, Operation.ADD_MULTIPLIED_TOTAL, 1.30F))
                .modifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 30F))
                .modifier(AttributeModifier.create(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.35F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.PROJECTILE_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.35F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.ARMOR_SHRED, Operation.ADD_VALUE, 0.45F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.PROT_SHRED, Operation.ADD_VALUE, 0.35F))
                .modifier(AttributeModifier.create(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, 0.30F))
                .modifier(AttributeModifier.create(Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_TOTAL, 0.15F)))
            .wave(w -> w
                .maxWaveTime(6400)
                .setupTime(240)
                .entity(InvaderWaveEntity.createRandom(3))
                .entity(StandardWaveEntity
                    .builder(EntityType.VEX)
                    .count(2)
                    .addModifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 30F))
                    .addModifier(AttributeModifier.create(Attributes.ARMOR_TOUGHNESS, Operation.ADD_VALUE, 20F))
                    .addModifier(AffixWaveModifier.create())
                    .build())
                .reward(new ExperienceReward(2500, 125))
                .modifier(LootTableModifier.createEmpty())
                .modifier(AttributeModifier.create(Attributes.MAX_HEALTH, Operation.ADD_MULTIPLIED_TOTAL, 0.35F))
                .modifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 15F))
                .modifier(AttributeModifier.create(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.40F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.PROJECTILE_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.40F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.ARMOR_SHRED, Operation.ADD_VALUE, 0.50F))
                .modifier(AttributeModifier.create(ALObjects.Attributes.PROT_SHRED, Operation.ADD_VALUE, 0.50F))
                .modifier(AttributeModifier.create(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, 0.08F)))
            .wave(w -> w
                .maxWaveTime(2000)
                .setupTime(300)
                .entity(elite(EntityType.GOAT, EliteRegistry.INSTANCE.holder(Apotheosis.loc("overworld/craig")), "elite.apotheosis.craig",
                    e -> e
                        .finalizeSpawn(false)
                        .addModifier(AttributeModifier.create(Attributes.MAX_HEALTH, Operation.ADD_MULTIPLIED_TOTAL, 5.0F))
                        .addModifier(AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 50F))
                        .addModifier(AttributeModifier.create(Attributes.ARMOR_TOUGHNESS, Operation.ADD_VALUE, 25F))
                        .addModifier(AttributeModifier.create(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.50F))
                        .addModifier(AttributeModifier.create(ALObjects.Attributes.PROJECTILE_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.50F))
                        .addModifier(AttributeModifier.create(ALObjects.Attributes.ARMOR_SHRED, Operation.ADD_VALUE, 0.75F))
                        .addModifier(AttributeModifier.create(ALObjects.Attributes.PROT_SHRED, Operation.ADD_VALUE, 0.50F))
                        .addModifier(AttributeModifier.create(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, 0.50F))
                        .addModifier(AttributeModifier.create(Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_TOTAL, 0.20F)))))
            .keyReward(new CountedReward(AffixItemReward.create(Rarities.MYTHIC), 5))
            .keyReward(new CountedReward(GemReward.create(Purity.PERFECT), 10))
            .keyReward(new StackReward(new ItemStack(Apoth.Items.GEM_DUST, 64)))
            .keyReward(new StackReward(new ItemStack(Apoth.Items.MYTHIC_MATERIAL, 64)))
            .keyReward(new StackReward(endlessInvaderGatePearl())));
    }

    private void tieredGateway(String path, UnaryOperator<TieredGateway.Builder> config) {
        this.add(Apotheosis.loc(path), config.apply(TieredGateway.builder()).build());
    }

    private void endlessGateway(String path, UnaryOperator<EndlessGateway.Builder> config) {
        this.add(Apotheosis.loc(path), config.apply(EndlessGateway.builder()).build());
    }

    private ItemStack endlessInvaderGatePearl() {
        ItemStack stack = new ItemStack(GatewayObjects.GATE_PEARL);
        GatePearlItem.setGate(stack, GatewayRegistry.INSTANCE.holder(Apotheosis.loc("endless_invader")));
        return stack;
    }

    public static EliteWaveEntity elite(EntityType<? extends Mob> entity, DynamicHolder<Elite> elite, String desc, UnaryOperator<StandardWaveEntity.Builder> baseEntity) {
        return new EliteWaveEntity(baseEntity.apply(new StandardWaveEntity.Builder(entity)).build(), elite, Optional.of(desc));
    }

}
