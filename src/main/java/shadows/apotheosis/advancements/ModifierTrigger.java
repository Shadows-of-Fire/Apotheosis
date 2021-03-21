package shadows.apotheosis.advancements;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.spawn.SpawnerModifiers;
import shadows.apotheosis.spawn.modifiers.SpawnerModifier;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile.SpawnerLogicExt;

public class ModifierTrigger implements ICriterionTrigger<ModifierTrigger.Instance> {
	private static final ResourceLocation ID = new ResourceLocation(Apotheosis.MODID, "spawner_modifier");
	private final Map<PlayerAdvancements, ModifierTrigger.Listeners> listeners = Maps.newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<ModifierTrigger.Instance> listener) {
		ModifierTrigger.Listeners ModifierTrigger$listeners = this.listeners.get(playerAdvancementsIn);
		if (ModifierTrigger$listeners == null) {
			ModifierTrigger$listeners = new ModifierTrigger.Listeners(playerAdvancementsIn);
			this.listeners.put(playerAdvancementsIn, ModifierTrigger$listeners);
		}

		ModifierTrigger$listeners.add(listener);
	}

	@Override
	public void removeListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<ModifierTrigger.Instance> listener) {
		ModifierTrigger.Listeners ModifierTrigger$listeners = this.listeners.get(playerAdvancementsIn);
		if (ModifierTrigger$listeners != null) {
			ModifierTrigger$listeners.remove(listener);
			if (ModifierTrigger$listeners.isEmpty()) {
				this.listeners.remove(playerAdvancementsIn);
			}
		}

	}

	@Override
	public void removeAllListeners(PlayerAdvancements playerAdvancementsIn) {
		this.listeners.remove(playerAdvancementsIn);
	}

	@Override
	public ModifierTrigger.Instance deserialize(JsonObject json, ConditionArrayParser conditionsParser) {
		MinMaxBounds.IntBound minDelay = MinMaxBounds.IntBound.fromJson(json.get("min_delay"));
		MinMaxBounds.IntBound maxDelay = MinMaxBounds.IntBound.fromJson(json.get("max_delay"));
		MinMaxBounds.IntBound spawnCount = MinMaxBounds.IntBound.fromJson(json.get("spawn_count"));
		MinMaxBounds.IntBound nearbyEnts = MinMaxBounds.IntBound.fromJson(json.get("max_nearby_entities"));
		MinMaxBounds.IntBound playerRange = MinMaxBounds.IntBound.fromJson(json.get("player_activation_range"));
		MinMaxBounds.IntBound spawnRange = MinMaxBounds.IntBound.fromJson(json.get("spawn_range"));
		Boolean ignorePlayers = json.has("ignore_players") ? json.get("ignore_players").getAsBoolean() : null;
		Boolean ignoreConditions = json.has("ignore_conditions") ? json.get("ignore_conditions").getAsBoolean() : null;
		Boolean ignoreCap = json.has("ignore_cap") ? json.get("ignore_cap").getAsBoolean() : null;
		Boolean redstone = json.has("redstone") ? json.get("redstone").getAsBoolean() : null;
		JsonElement modif = json.get("modifier");
		SpawnerModifier modifier = null;
		if (modif != null) modifier = SpawnerModifiers.MODIFIERS.values().stream().filter(m -> m.getId().equals(modif.getAsString())).findAny().orElse(null);
		return new ModifierTrigger.Instance(minDelay, maxDelay, spawnCount, nearbyEnts, playerRange, spawnRange, ignorePlayers, ignoreConditions, ignoreCap, redstone, modifier);
	}

	public void trigger(ServerPlayerEntity player, ApothSpawnerTile tile, SpawnerModifier modif) {
		ModifierTrigger.Listeners ModifierTrigger$listeners = this.listeners.get(player.getAdvancements());
		if (ModifierTrigger$listeners != null) {
			ModifierTrigger$listeners.trigger(tile, modif);
		}

	}

	public static class Instance extends CriterionInstance {
		private final MinMaxBounds.IntBound minDelay;
		private final MinMaxBounds.IntBound maxDelay;
		private final MinMaxBounds.IntBound spawnCount;
		private final MinMaxBounds.IntBound nearbyEnts;
		private final MinMaxBounds.IntBound playerRange;
		private final MinMaxBounds.IntBound spawnRange;
		private final Boolean ignorePlayers;
		private final Boolean ignoreConditions;
		private final Boolean ignoreCap;
		private final Boolean redstone;
		private final SpawnerModifier modifier;

		public Instance(MinMaxBounds.IntBound minDelay, MinMaxBounds.IntBound maxDelay, MinMaxBounds.IntBound spawnCount, MinMaxBounds.IntBound nearbyEnts, MinMaxBounds.IntBound playerRange, MinMaxBounds.IntBound spawnRange, Boolean ignorePlayers, Boolean ignoreConditions, Boolean ignoreCap, Boolean redstone, SpawnerModifier modifier) {
			super(ModifierTrigger.ID, EntityPredicate.AndPredicate.ANY_AND);
			this.minDelay = minDelay;
			this.maxDelay = maxDelay;
			this.spawnCount = spawnCount;
			this.nearbyEnts = nearbyEnts;
			this.playerRange = playerRange;
			this.spawnRange = spawnRange;
			this.ignorePlayers = ignorePlayers;
			this.ignoreConditions = ignoreConditions;
			this.ignoreCap = ignoreCap;
			this.redstone = redstone;
			this.modifier = modifier;
		}

		@Override
		public JsonObject serialize(ConditionArraySerializer serializer) {
			return new JsonObject();
		}

		public boolean test(ApothSpawnerTile tile, SpawnerModifier modif) {
			SpawnerLogicExt logic = (SpawnerLogicExt) tile.spawnerLogic;
			if (this.modifier != null && modif != this.modifier) return false;
			if (!this.minDelay.test(logic.minSpawnDelay)) return false;
			if (!this.maxDelay.test(logic.maxSpawnDelay)) return false;
			if (!this.spawnCount.test(logic.spawnCount)) return false;
			if (!this.nearbyEnts.test(logic.maxNearbyEntities)) return false;
			if (!this.playerRange.test(logic.activatingRangeFromPlayer)) return false;
			if (!this.spawnRange.test(logic.spawnRange)) return false;
			if (this.ignorePlayers != null && tile.ignoresPlayers != this.ignorePlayers) return false;
			if (this.ignoreConditions != null && tile.ignoresConditions != this.ignoreConditions) return false;
			if (this.ignoreCap != null && tile.ignoresCap != this.ignoreCap) return false;
			if (this.redstone != null && tile.redstoneEnabled != this.redstone) return false;
			return true;
		}
	}

	static class Listeners {
		private final PlayerAdvancements playerAdvancements;
		private final Set<ICriterionTrigger.Listener<ModifierTrigger.Instance>> listeners = Sets.newHashSet();

		public Listeners(PlayerAdvancements playerAdvancementsIn) {
			this.playerAdvancements = playerAdvancementsIn;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void add(ICriterionTrigger.Listener<ModifierTrigger.Instance> listener) {
			this.listeners.add(listener);
		}

		public void remove(ICriterionTrigger.Listener<ModifierTrigger.Instance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(ApothSpawnerTile tile, SpawnerModifier modif) {
			List<ICriterionTrigger.Listener<ModifierTrigger.Instance>> list = null;

			for (ICriterionTrigger.Listener<ModifierTrigger.Instance> listener : this.listeners) {
				if (listener.getCriterionInstance().test(tile, modif)) {
					if (list == null) {
						list = Lists.newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (ICriterionTrigger.Listener<ModifierTrigger.Instance> listener1 : list) {
					listener1.grantCriterion(this.playerAdvancements);
				}
			}

		}
	}
}