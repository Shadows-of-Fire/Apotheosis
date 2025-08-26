package dev.shadowsoffire.apotheosis.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.advancements.EquippedItemTrigger;
import dev.shadowsoffire.apotheosis.advancements.predicates.AffixItemPredicate;
import dev.shadowsoffire.apotheosis.advancements.predicates.InvaderPredicate;
import dev.shadowsoffire.apotheosis.advancements.predicates.RarityItemPredicate;
import dev.shadowsoffire.apotheosis.advancements.predicates.TypeAwareISP;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.gateways.GatewayObjects;
import dev.shadowsoffire.gateways.Gateways;
import dev.shadowsoffire.gateways.advancements.FinishGatewayTrigger;
import dev.shadowsoffire.gateways.gate.Gateway;
import dev.shadowsoffire.gateways.gate.GatewayRegistry;
import dev.shadowsoffire.gateways.item.GatePearlItem;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ApothAdvancementProvider extends AdvancementProvider {

    private final Map<ResourceLocation, List<ICondition>> conditions = new HashMap<>();

    private ApothAdvancementProvider(PackOutput output, CompletableFuture<Provider> registries, ExistingFileHelper existingFileHelper, List<AdvancementGenerator> subProviders) {
        super(output, registries, existingFileHelper, subProviders);

    }

    @Override
    @SuppressWarnings("deprecation")
    public final CompletableFuture<?> run(CachedOutput output) {
        return this.registries.thenCompose(regs -> {
            var conditionalCodec = ConditionalOps.createConditionalCodecWithConditions(Advancement.CODEC);

            Set<ResourceLocation> set = new HashSet<>();
            List<CompletableFuture<?>> list = new ArrayList<>();
            ConditionalConsumer<AdvancementHolder> consumer = wrap(holder -> {
                if (!set.add(holder.id())) {
                    throw new IllegalStateException("Duplicate advancement " + holder.id());
                }
                else {
                    Path path = this.pathProvider.json(holder.id());
                    List<ICondition> conds = this.conditions.getOrDefault(holder.id(), List.of());
                    if (conds.isEmpty()) {
                        list.add(DataProvider.saveStable(output, regs, Advancement.CODEC, holder.value(), path));
                    }
                    else {
                        WithConditions<Advancement> withConds = new WithConditions<>(conds, holder.value());
                        list.add(DataProvider.saveStable(output, regs, conditionalCodec, Optional.of(withConds), path));
                    }
                }
            });

            for (AdvancementSubProvider advancementsubprovider : this.subProviders) {
                advancementsubprovider.generate(regs, consumer);
            }

            return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
        });
    }

    public static ApothAdvancementProvider create(PackOutput output, CompletableFuture<Provider> registries, ExistingFileHelper existingFileHelper) {
        return new ApothAdvancementProvider(
            output,
            registries,
            existingFileHelper,
            List.of(
                new ProgressionGenerator()

            ));
    }

    private static class ProgressionGenerator implements AdvancementGenerator {

        @Override
        @SuppressWarnings("unused")
        public void generate(Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {

            ConditionalConsumer<AdvancementHolder> consumer = (ConditionalConsumer<AdvancementHolder>) saver;

            DynamicHolder<LootRarity> common = rarity("common");
            DynamicHolder<LootRarity> uncommon = rarity("uncommon");
            DynamicHolder<LootRarity> rare = rarity("rare");
            DynamicHolder<LootRarity> epic = rarity("epic");
            DynamicHolder<LootRarity> mythic = rarity("mythic");

            AdvancementHolder root = Advancement.Builder.advancement()
                .display(
                    Apoth.Items.BOSS_SUMMONER.value(),
                    title("progression.root"),
                    desc("progression.root"),
                    Apotheosis.loc("textures/advancements/bg/apoth.png"),
                    AdvancementType.TASK,
                    false,
                    false,
                    false)
                .requirements(AdvancementRequirements.Strategy.OR)
                .addCriterion("tick", PlayerTrigger.TriggerInstance.tick())
                .save(saver, loc("progression/root"));

            AdvancementHolder haven = Advancement.Builder.advancement()
                .display(
                    Items.OAK_SAPLING,
                    title("progression.haven"),
                    desc("progression.haven"),
                    Apotheosis.loc("textures/advancements/bg/apoth.png"),
                    AdvancementType.TASK,
                    true,
                    true,
                    false)
                .requirements(AdvancementRequirements.Strategy.OR)
                .addCriterion("affixed", InventoryChangeTrigger.TriggerInstance.hasItems(ip(new AffixItemPredicate())))
                .parent(root)
                .save(saver, loc("progression/haven"));

            AdvancementHolder frontier = Advancement.Builder.advancement()
                .display(
                    Items.IRON_SWORD,
                    title("progression.frontier"),
                    desc("progression.frontier"),
                    Apotheosis.loc("textures/advancements/bg/apoth.png"),
                    AdvancementType.TASK,
                    true,
                    true,
                    false)
                .requirements(AdvancementRequirements.Strategy.AND)
                .addCriterion("common_helm", rarityInSlot(EquipmentSlotGroup.HEAD, common, uncommon, rare, epic, mythic))
                .addCriterion("common_chest", rarityInSlot(EquipmentSlotGroup.CHEST, common, uncommon, rare, epic, mythic))
                .addCriterion("common_legs", rarityInSlot(EquipmentSlotGroup.LEGS, common, uncommon, rare, epic, mythic))
                .addCriterion("common_feet", rarityInSlot(EquipmentSlotGroup.FEET, common, uncommon, rare, epic, mythic))
                .addCriterion("common_hand", rarityInSlot(EquipmentSlotGroup.HAND, common, uncommon, rare, epic, mythic))
                .parent(haven)
                .save(saver, loc("progression/frontier"));

            AdvancementHolder ascent = Advancement.Builder.advancement()
                .display(
                    Items.BLAZE_POWDER,
                    title("progression.ascent"),
                    desc("progression.ascent"),
                    Apotheosis.loc("textures/advancements/bg/apoth.png"),
                    AdvancementType.GOAL,
                    true,
                    true,
                    false)
                .requirements(AdvancementRequirements.Strategy.AND)
                .addCriterion("uncommon_helm", rarityInSlot(EquipmentSlotGroup.HEAD, uncommon, rare, epic, mythic))
                .addCriterion("uncommon_chest", rarityInSlot(EquipmentSlotGroup.CHEST, uncommon, rare, epic, mythic))
                .addCriterion("uncommon_legs", rarityInSlot(EquipmentSlotGroup.LEGS, uncommon, rare, epic, mythic))
                .addCriterion("uncommon_feet", rarityInSlot(EquipmentSlotGroup.FEET, uncommon, rare, epic, mythic))
                .addCriterion("uncommon_hand", rarityInSlot(EquipmentSlotGroup.HAND, uncommon, rare, epic, mythic))
                .addCriterion("kill_apothic_invader", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().subPredicate(InvaderPredicate.INSTANCE)))
                .parent(frontier)
                .save(saver, loc("progression/ascent"));

            AdvancementHolder summit = Advancement.Builder.advancement()
                .display(
                    Items.NETHERITE_SWORD,
                    title("progression.summit"),
                    desc("progression.summit"),
                    Apotheosis.loc("textures/advancements/bg/apoth.png"),
                    AdvancementType.GOAL,
                    true,
                    true,
                    false)
                .requirements(AdvancementRequirements.Strategy.AND)
                .addCriterion("rare_helm", rarityInSlot(EquipmentSlotGroup.HEAD, rare, epic, mythic))
                .addCriterion("rare_chest", rarityInSlot(EquipmentSlotGroup.CHEST, rare, epic, mythic))
                .addCriterion("rare_legs", rarityInSlot(EquipmentSlotGroup.LEGS, rare, epic, mythic))
                .addCriterion("rare_feet", rarityInSlot(EquipmentSlotGroup.FEET, rare, epic, mythic))
                .addCriterion("rare_hand", rarityInSlot(EquipmentSlotGroup.HAND, rare, epic, mythic))
                .addCriterion("kill_wither", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(EntityType.WITHER)))
                .parent(ascent)
                .save(saver, loc("progression/summit"));

            AdvancementHolder pinnacle = Advancement.Builder.advancement()
                .display(
                    Items.END_CRYSTAL,
                    title("progression.pinnacle"),
                    desc("progression.pinnacle"),
                    Apotheosis.loc("textures/advancements/bg/apoth.png"),
                    AdvancementType.CHALLENGE,
                    true,
                    true,
                    false)
                .requirements(AdvancementRequirements.Strategy.AND)
                .addCriterion("epic_helm", rarityInSlot(EquipmentSlotGroup.HEAD, epic, mythic))
                .addCriterion("epic_chest", rarityInSlot(EquipmentSlotGroup.CHEST, epic, mythic))
                .addCriterion("epic_legs", rarityInSlot(EquipmentSlotGroup.LEGS, epic, mythic))
                .addCriterion("epic_feet", rarityInSlot(EquipmentSlotGroup.FEET, epic, mythic))
                .addCriterion("epic_hand", rarityInSlot(EquipmentSlotGroup.HAND, epic, mythic))
                .addCriterion("kill_ender_dragon", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(EntityType.ENDER_DRAGON)))
                .parent(summit)
                .save(saver, loc("progression/pinnacle"));

            DynamicHolder<Gateway> frontierGate = gateway("tiered/frontier");
            DynamicHolder<Gateway> ascentGate = gateway("tiered/ascent");
            DynamicHolder<Gateway> summitGate = gateway("tiered/summit");
            DynamicHolder<Gateway> pinnacleGate = gateway("tiered/pinnacle");

            AdvancementHolder completeFrontierGate = Advancement.Builder.advancement()
                .display(
                    gatePearl(frontierGate),
                    title("challenge_gates.frontier"),
                    desc("challenge_gates.frontier"),
                    Apotheosis.loc("textures/advancements/bg/apoth.png"),
                    AdvancementType.TASK,
                    true,
                    true,
                    false)
                .requirements(AdvancementRequirements.Strategy.AND)
                .addCriterion("complete_frontier_gate", completeGateway(frontierGate))
                .parent(frontier)
                .build(Apotheosis.loc("gateways/frontier"));

            consumer.saveConditionally(completeFrontierGate, new ModLoadedCondition(Gateways.MODID));

            AdvancementHolder completeAscentGate = Advancement.Builder.advancement()
                .display(
                    gatePearl(ascentGate),
                    title("challenge_gates.ascent"),
                    desc("challenge_gates.ascent"),
                    Apotheosis.loc("textures/advancements/bg/apoth.png"),
                    AdvancementType.GOAL,
                    true,
                    true,
                    false)
                .requirements(AdvancementRequirements.Strategy.AND)
                .addCriterion("complete_ascent_gate", completeGateway(ascentGate))
                .parent(ascent)
                .build(Apotheosis.loc("gateways/ascent"));

            consumer.saveConditionally(completeAscentGate, new ModLoadedCondition(Gateways.MODID));

            AdvancementHolder completeSummitGate = Advancement.Builder.advancement()
                .display(
                    gatePearl(summitGate),
                    title("challenge_gates.summit"),
                    desc("challenge_gates.summit"),
                    Apotheosis.loc("textures/advancements/bg/apoth.png"),
                    AdvancementType.GOAL,
                    true,
                    true,
                    false)
                .requirements(AdvancementRequirements.Strategy.AND)
                .addCriterion("complete_summit_gate", completeGateway(summitGate))
                .parent(summit)
                .build(Apotheosis.loc("gateways/summit"));

            consumer.saveConditionally(completeSummitGate, new ModLoadedCondition(Gateways.MODID));

            AdvancementHolder completePinnacleGate = Advancement.Builder.advancement()
                .display(
                    gatePearl(pinnacleGate),
                    title("challenge_gates.pinnacle"),
                    desc("challenge_gates.pinnacle"),
                    Apotheosis.loc("textures/advancements/bg/apoth.png"),
                    AdvancementType.GOAL,
                    true,
                    true,
                    false)
                .requirements(AdvancementRequirements.Strategy.AND)
                .addCriterion("complete_pinnacle_gate", completeGateway(pinnacleGate))
                .parent(pinnacle)
                .build(Apotheosis.loc("gateways/pinnacle"));

            consumer.saveConditionally(completePinnacleGate, new ModLoadedCondition(Gateways.MODID));

            AdvancementHolder obtainSigilOfSupremacy = Advancement.Builder.advancement()
                .display(
                    Apoth.Items.SIGIL_OF_SUPREMACY.value(),
                    title("challenge_gates.sigil_of_supremacy"),
                    desc("challenge_gates.sigil_of_supremacy"),
                    Apotheosis.loc("textures/advancements/bg/apoth.png"),
                    AdvancementType.CHALLENGE,
                    true,
                    true,
                    false)
                .requirements(AdvancementRequirements.Strategy.AND)
                .addCriterion("obtain_sigil_of_supremacy", InventoryChangeTrigger.TriggerInstance.hasItems(Apoth.Items.SIGIL_OF_SUPREMACY.value()))
                .parent(completePinnacleGate)
                .build(Apotheosis.loc("gateways/sigil_of_supremacy"));

            consumer.saveConditionally(obtainSigilOfSupremacy, new ModLoadedCondition(Gateways.MODID));
        }

        @SafeVarargs
        private static Criterion<?> rarityInSlot(EquipmentSlotGroup slot, DynamicHolder<LootRarity>... rarities) {
            return EquippedItemTrigger.TriggerInstance.hasItems(slot, ip(new RarityItemPredicate(ApothMiscUtil.linkedSet(rarities))));
        }

        private static Criterion<?> completeGateway(DynamicHolder<Gateway> gate) {
            return GatewayObjects.FINISH_GATEWAY.createCriterion(new FinishGatewayTrigger.Instance(Optional.empty(), gate));
        }
    }

    private static Component title(String key) {
        return Apotheosis.lang("advancements", key + ".title");
    }

    private static Component desc(String key) {
        return Apotheosis.lang("advancements", key + ".desc");
    }

    private static String loc(String path) {
        return Apotheosis.loc(path).toString();
    }

    private static ItemPredicate ip(TypeAwareISP<?> sub) {
        return new ItemPredicate(Optional.empty(), MinMaxBounds.Ints.ANY, DataComponentPredicate.EMPTY, Map.of(sub.type(), sub));
    }

    private static DynamicHolder<LootRarity> rarity(String path) {
        return RarityRegistry.INSTANCE.holder(Apotheosis.loc(path));
    }

    private static DynamicHolder<Gateway> gateway(String path) {
        return GatewayRegistry.INSTANCE.holder(Apotheosis.loc(path));
    }

    private static ItemStack gatePearl(DynamicHolder<Gateway> gate) {
        ItemStack stack = new ItemStack(GatewayObjects.GATE_PEARL);
        GatePearlItem.setGate(stack, gate);
        return stack;
    }

    private static interface ConditionalConsumer<T> extends Consumer<T> {
        void saveConditionally(T adv, ICondition... conditions);
    }

    private void addCondition(AdvancementHolder adv, ICondition cond) {
        this.conditions.computeIfAbsent(adv.id(), a -> new ArrayList<>()).add(cond);
    }

    private <T> ConditionalConsumer<T> wrap(Consumer<T> consumer) {
        return new ConditionalConsumer<>(){
            @Override
            public void accept(T t) {
                consumer.accept(t);
            }

            @Override
            public void saveConditionally(T adv, ICondition... conditions) {
                for (ICondition cond : conditions) {
                    ApothAdvancementProvider.this.addCondition((AdvancementHolder) adv, cond);
                }
                this.accept(adv);
            }
        };
    }
}
