package dev.shadowsoffire.apotheosis.advancements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;

import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;

public class SplittingTrigger implements CriterionTrigger<AbstractCriterionTriggerInstance> {

    private static final ResourceLocation ID = new ResourceLocation(Apotheosis.MODID, "splitting");
    Map<PlayerAdvancements, Set<Listener<AbstractCriterionTriggerInstance>>> listeners = new HashMap<>();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements adv, Listener<AbstractCriterionTriggerInstance> listener) {
        this.listeners.computeIfAbsent(adv, a -> new HashSet<>()).add(listener);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements adv, Listener<AbstractCriterionTriggerInstance> listener) {
        this.listeners.computeIfAbsent(adv, a -> new HashSet<>()).remove(listener);
    }

    @Override
    public void removePlayerListeners(PlayerAdvancements adv) {
        this.listeners.remove(adv);
    }

    @Override
    public AbstractCriterionTriggerInstance createInstance(JsonObject json, DeserializationContext parser) {
        return new AbstractCriterionTriggerInstance(ID, ContextAwarePredicate.ANY){};
    }

    public void trigger(PlayerAdvancements adv) {
        if (this.listeners.containsKey(adv)) {
            new HashSet<>(this.listeners.get(adv)).forEach(t -> t.run(adv));
        }
    }

}
