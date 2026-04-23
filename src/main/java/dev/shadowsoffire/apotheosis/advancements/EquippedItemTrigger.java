package dev.shadowsoffire.apotheosis.advancements;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class EquippedItemTrigger extends SimpleCriterionTrigger<EquippedItemTrigger.TriggerInstance> {

    @Override
    public Codec<EquippedItemTrigger.TriggerInstance> codec() {
        return EquippedItemTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, EquipmentSlot slot, ItemStack stack) {
        this.trigger(player, inst -> inst.matches(slot, stack));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, EquipmentSlotGroup slots, ItemPredicate items)
        implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                EquipmentSlotGroup.CODEC.optionalFieldOf("slots", EquipmentSlotGroup.ANY).forGetter(TriggerInstance::slots),
                ItemPredicate.CODEC.optionalFieldOf("items", ItemPredicate.Builder.item().build()).forGetter(TriggerInstance::items))
            .apply(inst, TriggerInstance::new));

        public static Criterion<TriggerInstance> hasItems(EquipmentSlotGroup slots, ItemPredicate.Builder items) {
            return hasItems(slots, items.build());
        }

        public static Criterion<TriggerInstance> hasItems(EquipmentSlotGroup slots, ItemPredicate items) {
            return Apoth.Triggers.EQUIPPED_ITEM
                .createCriterion(
                    new EquippedItemTrigger.TriggerInstance(Optional.empty(), slots, items));
        }

        @SuppressWarnings("deprecation")
        public static Criterion<EquippedItemTrigger.TriggerInstance> hasItems(EquipmentSlotGroup slots, ItemLike item) {
            ItemPredicate predicate = new ItemPredicate(
                Optional.of(HolderSet.direct(item.asItem().builtInRegistryHolder())), MinMaxBounds.Ints.ANY, DataComponentMatchers.ANY);

            return hasItems(slots, predicate);
        }

        public boolean matches(EquipmentSlot slot, ItemStack stack) {
            if (!this.slots.test(slot)) {
                return false;
            }

            return !stack.isEmpty() && this.items.test(stack);
        }

    }
}
