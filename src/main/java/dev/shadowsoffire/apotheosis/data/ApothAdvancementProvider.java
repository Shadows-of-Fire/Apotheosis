package dev.shadowsoffire.apotheosis.data;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ApothAdvancementProvider extends AdvancementProvider {

    private ApothAdvancementProvider(PackOutput output, CompletableFuture<Provider> registries, ExistingFileHelper existingFileHelper, List<AdvancementGenerator> subProviders) {
        super(output, registries, existingFileHelper, subProviders);
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

        }

        @SafeVarargs
        private static Criterion<?> rarityInSlot(EquipmentSlotGroup slot, DynamicHolder<LootRarity>... rarities) {
            return EquippedItemTrigger.TriggerInstance.hasItems(slot, ip(new RarityItemPredicate(ApothMiscUtil.linkedSet(rarities))));
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
}
