package dev.shadowsoffire.apotheosis.advancements;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EnchantedItemTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MinMaxBounds.Doubles;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class EnchantedTrigger extends EnchantedItemTrigger {

    @Override
    public Instance createInstance(JsonObject json, ContextAwarePredicate entityPredicate, DeserializationContext conditionsParser) {
        ItemPredicate item = ItemPredicate.fromJson(json.get("item"));
        Ints levels = Ints.fromJson(json.get("levels"));
        Doubles eterna = Doubles.fromJson(json.get("eterna"));
        Doubles quanta = Doubles.fromJson(json.get("quanta"));
        Doubles arcana = Doubles.fromJson(json.get("arcana"));
        Doubles rectification = Doubles.fromJson(json.get("rectification"));
        return new Instance(item, levels, eterna, quanta, arcana, rectification);
    }

    public void trigger(ServerPlayer player, ItemStack stack, int level, float eterna, float quanta, float arcana, float rectification) {
        this.trigger(player, inst -> {
            if (inst instanceof Instance) return ((Instance) inst).test(stack, level, eterna, quanta, arcana, rectification);
            return inst.matches(stack, level);
        });
    }

    public static class Instance extends EnchantedItemTrigger.TriggerInstance {

        protected final Doubles eterna, quanta, arcana, rectification;

        public Instance(ItemPredicate item, Ints levels, Doubles eterna, Doubles quanta, Doubles arcana, Doubles rectification) {
            super(ContextAwarePredicate.ANY, item, levels);
            this.eterna = eterna;
            this.quanta = quanta;
            this.arcana = arcana;
            this.rectification = rectification;
        }

        public static EnchantedItemTrigger.TriggerInstance any() {
            return new EnchantedItemTrigger.TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.ANY, MinMaxBounds.Ints.ANY);
        }

        public boolean test(ItemStack stack, int level, float eterna, float quanta, float arcana, float rectification) {
            return super.matches(stack, level) && this.eterna.matches(eterna) && this.quanta.matches(quanta) && this.arcana.matches(arcana) && this.rectification.matches(rectification);
        }

        @Override
        public boolean matches(ItemStack stack, int level) {
            return this.test(stack, level, 0, 0, 0, 0);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("eterna", this.eterna.serializeToJson());
            jsonobject.add("quanta", this.quanta.serializeToJson());
            jsonobject.add("arcana", this.arcana.serializeToJson());
            jsonobject.add("rectification", this.rectification.serializeToJson());
            return jsonobject;
        }
    }

}
