package dev.shadowsoffire.apotheosis.data;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.mobs.registries.EliteRegistry;
import dev.shadowsoffire.apotheosis.mobs.types.Elite;
import dev.shadowsoffire.apotheosis.mobs.util.SpawnCondition;
import dev.shadowsoffire.apotheosis.mobs.util.SpawnCondition.NotCondition;
import dev.shadowsoffire.apotheosis.mobs.util.SpawnCondition.SpawnTypeCondition;
import dev.shadowsoffire.apotheosis.mobs.util.SpawnCondition.SurfaceTypeCondition;
import dev.shadowsoffire.apotheosis.mobs.util.SurfaceType;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.placebo.color.GradientColor;
import dev.shadowsoffire.placebo.util.StepFunction;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class EliteProvider extends DynamicRegistryProvider<Elite> {

    public static final int DEFAULT_WEIGHT = 100;
    public static final float DEFAULT_QUALITY = 0.1F;

    public EliteProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries, EliteRegistry.INSTANCE);
    }

    @Override
    public String getName() {
        return "Apothic Elites";
    }

    @Override
    public void generate() {
        this.addBoss("overworld/craig", b -> b
            .chance(0.005F)
            .entities(EntityType.GOAT)
            .basicData(c -> c
                .name(Apotheosis.lang("elite", "craig").withStyle(s -> s.withColor(GradientColor.RAINBOW)))
                .weights(TieredWeights.onlyFor(WorldTier.PINNACLE, DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .constraints(Constraints.forDimension(Level.OVERWORLD))
                .exclusion(excludedSpawnTypes(MobSpawnType.SPAWN_EGG))
                .gearSets(WorldTier.PINNACLE, "#pinnacle_melee")
                .nbt(t -> t.putBoolean("IsScreamingGoat", true))
                .nbt(t -> t.putBoolean("HasLeftHorn", true))
                .nbt(t -> t.putBoolean("HasRightHorn", false))
                .support(s -> s.entity(EntityType.SHEEP).nbt(t -> t.putString("CustomName", "jeb_"))))
            .stats(c -> c
                .enchantChance(1)
                .enchLevels(100, 100)
                .effect(1, MobEffects.FIRE_RESISTANCE)
                .effect(1, MobEffects.GLOWING)
                .modifier(Attributes.MAX_HEALTH, Operation.ADD_VALUE, StepFunction.constant(4096))
                .modifier(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 4, 10)
                .modifier(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, StepFunction.constant(1))
                .modifier(Attributes.SCALE, Operation.ADD_VALUE, StepFunction.constant(3))));

        this.addBoss("overworld/honeyed_archer", b -> b
            .chance(0.008F)
            .entities(EntityType.SKELETON, EntityType.STRAY, EntityType.BOGGED)
            .affixes(0.25F, Set.of())
            .basicData(c -> InvaderProvider.rangedGear(c)
                .name(Component.literal("Honeyed Archer"))
                .weights(TieredWeights.forTiersAbove(WorldTier.FRONTIER, DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .constraints(Constraints.forDimension(Level.OVERWORLD))
                .exclusion(excludedSpawnTypes(MobSpawnType.SPAWN_EGG, MobSpawnType.SPAWNER, MobSpawnType.MOB_SUMMONED))
                .exclusion(new SurfaceTypeCondition(SurfaceType.NEEDS_SURFACE))
                .mount(m -> m
                    .entity(EntityType.BEE)
                    .nbt(t -> t.putInt("CannotEnterHiveTicks", 999999))
                    .nbt(t -> t.putInt("AngerTime", 99999999))))
            .stats(c -> c
                .enchantChance(0.25F)
                .enchLevels(20, 12)
                .modifier(Attributes.MAX_HEALTH, Operation.ADD_VALUE, 10, 20)
                .modifier(ALObjects.Attributes.ARROW_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.25F, 0.35F)
                .modifier(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, StepFunction.constant(0.5F))));

        this.addBoss("overworld/undead_knight", b -> b
            .chance(0.01F)
            .entities(EntityType.SKELETON, EntityType.STRAY, EntityType.BOGGED)
            .affixes(0.35F, Set.of())
            .basicData(c -> InvaderProvider.rangedGear(c)
                .name(Component.literal("Undead Knight"))
                .weights(TieredWeights.forTiersAbove(WorldTier.FRONTIER, DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .constraints(Constraints.forDimension(Level.OVERWORLD))
                .exclusion(excludedSpawnTypes(MobSpawnType.SPAWN_EGG, MobSpawnType.SPAWNER, MobSpawnType.MOB_SUMMONED))
                .exclusion(new SurfaceTypeCondition(SurfaceType.NEEDS_SURFACE))
                .mount(m -> m
                    .entity(EntityType.SKELETON_HORSE)
                    .nbt(skeletonHorseNbt())))
            .stats(c -> c
                .enchantChance(0.25F)
                .enchLevels(20, 12)
                .modifier(Attributes.MAX_HEALTH, Operation.ADD_VALUE, 10, 30)
                .modifier(ALObjects.Attributes.ARROW_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.30F, 0.5F)
                .modifier(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, StepFunction.constant(0.5F))));

        this.addBoss("the_nether/withering_archer", b -> b
            .chance(0.02F)
            .entities(EntityType.WITHER_SKELETON, EntityType.SKELETON)
            .affixes(0.45F, Set.of())
            .basicData(c -> InvaderProvider.rangedGear(c)
                .name(Component.literal("Withering Archer"))
                .weights(TieredWeights.forTiersAbove(WorldTier.FRONTIER, DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .constraints(Constraints.forDimension(Level.NETHER))
                .exclusion(excludedSpawnTypes(MobSpawnType.SPAWN_EGG, MobSpawnType.SPAWNER, MobSpawnType.MOB_SUMMONED))
                .nbt(witherCloud()))
            .stats(c -> c
                .enchantChance(0.25F)
                .enchLevels(20, 12)
                .modifier(Attributes.MAX_HEALTH, Operation.ADD_VALUE, 20, 20)
                .modifier(Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_BASE, 0.1F, 0.1F)
                .modifier(ALObjects.Attributes.ARROW_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.25F, 0.25F)
                .modifier(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, 0.5F, 0.5F)));
    }

    private static CompoundTag skeletonHorseNbt() {
        String rawNbt = """
            {
                "NeoForgeData": {
                    "apoth.burns_in_sun": 1
                },
                "Tame": 1,
                "attributes": [
                    {
                        "base": 0.3,
                        "id": "generic.movement_speed"
                    },
                    {
                        "base": 0.5,
                        "id": "generic.knockback_resistance"
                    }
                ]
            }
            """;

        try {
            return TagParser.parseTag(rawNbt);
        }
        catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static CompoundTag witherCloud() {
        String rawNbt = """
            {
                "Passengers": [{
                    "id": "minecraft:area_effect_cloud",
                    "potion_contents": {
                        "potion": "apothic_attributes:wither"
                    },
                    "Duration": 200000,
                    "Radius": 2.5,
                    "ReapplicationDelay": 20,
                    "Particle": {
                        "type":"entity_effect",
                        "color": 0
                    },
                    "WaitTime": 0
                }]
            }
            """;

        try {
            return TagParser.parseTag(rawNbt);
        }
        catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void addBoss(String name, UnaryOperator<Elite.Builder> builder) {
        this.add(Apotheosis.loc(name), builder.apply(Elite.builder()).build());
    }

    private static SpawnCondition excludedSpawnTypes(MobSpawnType... types) {
        return new NotCondition(new SpawnTypeCondition(ApothMiscUtil.linkedSet(types)));
    }

}
