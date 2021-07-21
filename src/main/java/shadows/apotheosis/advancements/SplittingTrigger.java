package shadows.apotheosis.advancements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;

import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;
import shadows.apotheosis.Apotheosis;

public class SplittingTrigger implements ICriterionTrigger<CriterionInstance> {

	private static final ResourceLocation ID = new ResourceLocation(Apotheosis.MODID, "splitting");
	Map<PlayerAdvancements, Set<Listener<CriterionInstance>>> listeners = new HashMap<>();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements adv, Listener<CriterionInstance> listener) {
		this.listeners.computeIfAbsent(adv, a -> new HashSet<>()).add(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements adv, Listener<CriterionInstance> listener) {
		this.listeners.computeIfAbsent(adv, a -> new HashSet<>()).remove(listener);
	}

	@Override
	public void removePlayerListeners(PlayerAdvancements adv) {
		this.listeners.remove(adv);
	}

	@Override
	public CriterionInstance createInstance(JsonObject json, ConditionArrayParser parser) {
		return new CriterionInstance(ID, AndPredicate.ANY) {
		};
	}

	public void trigger(PlayerAdvancements adv) {
		if (this.listeners.containsKey(adv)) {
			new HashSet<>(this.listeners.get(adv)).forEach(t -> t.run(adv));
		}
	}

}