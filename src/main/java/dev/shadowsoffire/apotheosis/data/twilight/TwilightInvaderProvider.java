//package dev.shadowsoffire.apotheosis.data.twilight;
//
//import java.util.concurrent.CompletableFuture;
//import java.util.function.UnaryOperator;
//
//import dev.shadowsoffire.apotheosis.Apoth;
//import dev.shadowsoffire.apotheosis.Apotheosis;
//import dev.shadowsoffire.apotheosis.data.InvaderProvider;
//import dev.shadowsoffire.apotheosis.loot.LootRarity;
//import dev.shadowsoffire.apotheosis.mobs.types.Invader;
//import dev.shadowsoffire.apotheosis.mobs.util.BasicBossData;
//import dev.shadowsoffire.apotheosis.tiers.Constraints;
//import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
//import dev.shadowsoffire.apotheosis.tiers.WorldTier;
//import dev.shadowsoffire.placebo.util.StepFunction;
//import net.minecraft.core.HolderLookup.Provider;
//import net.minecraft.core.registries.Registries;
//import net.minecraft.data.PackOutput;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceKey;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.effect.MobEffects;
//import net.minecraft.world.entity.EntityType;
//import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
//import net.minecraft.world.entity.ai.attributes.Attributes;
//import net.minecraft.world.level.Level;
//import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
//import twilightforest.init.TFEntities;
//
//public class TwilightInvaderProvider extends InvaderProvider {
//
//    private static ResourceKey<Level> TWILIGHT_FOREST = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("twilightforest:twilight_forest"));
//
//    public TwilightInvaderProvider(PackOutput output, CompletableFuture<Provider> registries) {
//        super(output, registries);
//    }
//
//    @Override
//    public String getName() {
//        return "Twilight Invaders";
//    }
//
//    @Override
//    public void generate() {
//        // HolderLookup.Provider registries = this.lookupProvider.join();
//        // RegistryLookup<Biome> biomes = registries.lookup(Registries.BIOME).get();
//
//        LootRarity mythic = rarity("mythic");
//
//        addBoss("twilight/carminite_golem", b -> basicMeleeStats(b)
//            .entity(TFEntities.CARMINITE_GOLEM.value())
//            .size(2.25, 3.3)
//            .basicData(c -> meleeGear(c)
//                .name(Component.literal(BasicBossData.NAME_GEN))
//                .weights(TieredWeights.forTiersAbove(WorldTier.SUMMIT, DEFAULT_WEIGHT, DEFAULT_QUALITY))
//                .constraints(Constraints.forDimension(TWILIGHT_FOREST))
//                .bonusLoot(Apoth.LootTables.BONUS_BOSS_DROPS, Apoth.LootTables.BONUS_RARE_BOSS_DROPS)));
//
//        addBoss("twilight/goblin_knight", b -> basicMeleeStats(b)
//            .entity(TFEntities.LOWER_GOBLIN_KNIGHT.value())
//            .size(1.75, 2.75)
//            .basicData(c -> meleeGear(c)
//                .name(Component.literal(BasicBossData.NAME_GEN))
//                .weights(TieredWeights.forTiersAbove(WorldTier.ASCENT, DEFAULT_WEIGHT, DEFAULT_QUALITY))
//                .constraints(Constraints.forDimension(TWILIGHT_FOREST))
//                .bonusLoot(Apoth.LootTables.BONUS_BOSS_DROPS, Apoth.LootTables.BONUS_RARE_BOSS_DROPS)));
//
//        addBoss("twilight/helmet_crab", b -> b
//            .entity(TFEntities.HELMET_CRAB.value())
//            .size(2, 2)
//            .basicData(c -> meleeGear(c)
//                .name(Component.literal(BasicBossData.NAME_GEN))
//                .weights(TieredWeights.onlyFor(WorldTier.PINNACLE, 2, 0.55F))
//                .constraints(Constraints.forDimension(TWILIGHT_FOREST))
//                .bonusLoot(Apoth.LootTables.BONUS_BOSS_DROPS, Apoth.LootTables.BONUS_RARE_BOSS_DROPS))
//            .stats(mythic, c -> c
//                .enchantChance(0.85F)
//                .enchLevels(60, 35)
//                .effect(1, MobEffects.FIRE_RESISTANCE)
//                .modifier(Attributes.MAX_HEALTH, Operation.ADD_VALUE, 100, 140)
//                .modifier(Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_BASE, 0.35F, 0.70F)
//                .modifier(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_BASE, 0.32F, 0.85F)
//                .modifier(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, StepFunction.constant(0.65F))
//                .modifier(Attributes.ARMOR, Operation.ADD_VALUE, StepFunction.constant(12F))
//                .modifier(Attributes.ARMOR_TOUGHNESS, Operation.ADD_VALUE, StepFunction.constant(20F))
//                .modifier(Attributes.SCALE, Operation.ADD_MULTIPLIED_TOTAL, StepFunction.constant(1.85F))));
//
//        addBoss("twilight/kobold", b -> basicMeleeStats(b)
//            .entity(TFEntities.KOBOLD.value())
//            .size(1.5, 1.5)
//            .basicData(c -> meleeGear(c)
//                .name(Component.literal(BasicBossData.NAME_GEN))
//                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
//                .constraints(Constraints.forDimension(TWILIGHT_FOREST))
//                .bonusLoot(Apoth.LootTables.BONUS_BOSS_DROPS)));
//
//        addBoss("twilight/redcap", b -> basicMeleeStats(b)
//            .entity(TFEntities.REDCAP.value())
//            .size(1.5, 1.5)
//            .basicData(c -> meleeGear(c)
//                .name(Component.literal(BasicBossData.NAME_GEN))
//                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
//                .constraints(Constraints.forDimension(TWILIGHT_FOREST))
//                .bonusLoot(Apoth.LootTables.BONUS_BOSS_DROPS)));
//
//        addBoss("twilight/redcap_sapper", b -> basicMeleeStats(b)
//            .entity(TFEntities.REDCAP_SAPPER.value())
//            .size(1.5, 1.5)
//            .basicData(c -> meleeGear(c)
//                .name(Component.literal(BasicBossData.NAME_GEN))
//                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
//                .constraints(Constraints.forDimension(TWILIGHT_FOREST))
//                .bonusLoot(Apoth.LootTables.BONUS_BOSS_DROPS)));
//
//        addBoss("twilight/skeleton_druid", b -> basicRangedStats(b)
//            .entity(TFEntities.SKELETON_DRUID.value())
//            .size(1.5, 2.5)
//            .basicData(c -> rangedGear(c)
//                .name(Component.literal(BasicBossData.NAME_GEN))
//                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
//                .constraints(Constraints.forDimension(TWILIGHT_FOREST))
//                .bonusLoot(Apoth.LootTables.BONUS_BOSS_DROPS)));
//
//        addBoss("twilight/skeleton", b -> basicRangedStats(b)
//            .entity(EntityType.SKELETON)
//            .size(1.5, 2.5)
//            .basicData(c -> rangedGear(c)
//                .name(Component.literal(BasicBossData.NAME_GEN))
//                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
//                .constraints(Constraints.forDimension(TWILIGHT_FOREST))
//                .bonusLoot(Apoth.LootTables.BONUS_BOSS_DROPS)));
//
//        // TODO: Mist Wolf and Ice Wolf?
//    }
//
//    @Override
//    protected void addBoss(String name, UnaryOperator<Invader.Builder> builder) {
//        this.addConditionally(Apotheosis.loc(name), builder.apply(Invader.builder()).build(), new ModLoadedCondition("twilightforest"));
//    }
//
//}
