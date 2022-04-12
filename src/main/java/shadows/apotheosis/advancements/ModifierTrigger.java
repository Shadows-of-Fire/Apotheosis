package shadows.apotheosis.advancements;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import shadows.apotheosis.spawn.modifiers.SpawnerModifier;
import shadows.apotheosis.spawn.modifiers.SpawnerStats;
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
	public void addPlayerListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<ModifierTrigger.Instance> listener) {
		ModifierTrigger.Listeners ModifierTrigger$listeners = this.listeners.get(playerAdvancementsIn);
		if (ModifierTrigger$listeners == null) {
			ModifierTrigger$listeners = new ModifierTrigger.Listeners(playerAdvancementsIn);
			this.listeners.put(playerAdvancementsIn, ModifierTrigger$listeners);
		}

		ModifierTrigger$listeners.add(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<ModifierTrigger.Instance> listener) {
		ModifierTrigger.Listeners ModifierTrigger$listeners = this.listeners.get(playerAdvancementsIn);
		if (ModifierTrigger$listeners != null) {
			ModifierTrigger$listeners.remove(listener);
			if (ModifierTrigger$listeners.isEmpty()) {
				this.listeners.remove(playerAdvancementsIn);
			}
		}

	}

	@Override
	public void removePlayerListeners(PlayerAdvancements playerAdvancementsIn) {
		this.listeners.remove(playerAdvancementsIn);
	}

	@Override
	public ModifierTrigger.Instance createInstance(JsonObject json, ConditionArrayParser conditionsParser) {
		MinMaxBounds.IntBound minDelay = MinMaxBounds.IntBound.fromJson(json.get(SpawnerStats.MIN_DELAY.getId()));
		MinMaxBounds.IntBound maxDelay = MinMaxBounds.IntBound.fromJson(json.get(SpawnerStats.MAX_DELAY.getId()));
		MinMaxBounds.IntBound spawnCount = MinMaxBounds.IntBound.fromJson(json.get(SpawnerStats.SPAWN_COUNT.getId()));
		MinMaxBounds.IntBound nearbyEnts = MinMaxBounds.IntBound.fromJson(json.get(SpawnerStats.MAX_NEARBY_ENTITIES.getId()));
		MinMaxBounds.IntBound playerRange = MinMaxBounds.IntBound.fromJson(json.get(SpawnerStats.REQ_PLAYER_RANGE.getId()));
		MinMaxBounds.IntBound spawnRange = MinMaxBounds.IntBound.fromJson(json.get(SpawnerStats.SPAWN_RANGE.getId()));
		Boolean ignorePlayers = json.has(SpawnerStats.IGNORE_PLAYERS.getId()) ? json.get(SpawnerStats.IGNORE_PLAYERS.getId()).getAsBoolean() : null;
		Boolean ignoreConditions = json.has(SpawnerStats.IGNORE_CONDITIONS.getId()) ? json.get(SpawnerStats.IGNORE_CONDITIONS.getId()).getAsBoolean() : null;
		Boolean redstone = json.has(SpawnerStats.REDSTONE_CONTROL.getId()) ? json.get(SpawnerStats.REDSTONE_CONTROL.getId()).getAsBoolean() : null;
		Boolean ignoreLight = json.has(SpawnerStats.IGNORE_LIGHT.getId()) ? json.get(SpawnerStats.IGNORE_LIGHT.getId()).getAsBoolean() : null;
		Boolean noAI = json.has(SpawnerStats.NO_AI.getId()) ? json.get(SpawnerStats.NO_AI.getId()).getAsBoolean() : null;
		return new ModifierTrigger.Instance(minDelay, maxDelay, spawnCount, nearbyEnts, playerRange, spawnRange, ignorePlayers, ignoreConditions, redstone, ignoreLight, noAI);
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
		private final Boolean redstone;
		private final Boolean ignoreLight;
		private final Boolean noAI;

		public Instance(MinMaxBounds.IntBound minDelay, MinMaxBounds.IntBound maxDelay, MinMaxBounds.IntBound spawnCount, MinMaxBounds.IntBound nearbyEnts, MinMaxBounds.IntBound playerRange, MinMaxBounds.IntBound spawnRange, Boolean ignorePlayers, Boolean ignoreConditions, Boolean redstone, Boolean ignoreLight, Boolean noAI) {
			super(ModifierTrigger.ID, EntityPredicate.AndPredicate.ANY);
			this.minDelay = minDelay;
			this.maxDelay = maxDelay;
			this.spawnCount = spawnCount;
			this.nearbyEnts = nearbyEnts;
			this.playerRange = playerRange;
			this.spawnRange = spawnRange;
			this.ignorePlayers = ignorePlayers;
			this.ignoreConditions = ignoreConditions;
			this.redstone = redstone;
			this.ignoreLight = ignoreLight;
			this.noAI = noAI;
		}

		@Override
		public JsonObject serializeToJson(ConditionArraySerializer serializer) {
			return new JsonObject();
		}

		public boolean test(ApothSpawnerTile tile, SpawnerModifier modif) {
			SpawnerLogicExt logic = (SpawnerLogicExt) tile.spawner;
			if (!this.minDelay.matches(logic.minSpawnDelay)) return false;
			if (!this.maxDelay.matches(logic.maxSpawnDelay)) return false;
			if (!this.spawnCount.matches(logic.spawnCount)) return false;
			if (!this.nearbyEnts.matches(logic.maxNearbyEntities)) return false;
			if (!this.playerRange.matches(logic.requiredPlayerRange)) return false;
			if (!this.spawnRange.matches(logic.spawnRange)) return false;
			if (this.ignorePlayers != null && tile.ignoresPlayers != this.ignorePlayers) return false;
			if (this.ignoreConditions != null && tile.ignoresConditions != this.ignoreConditions) return false;
			if (this.redstone != null && tile.redstoneControl != this.redstone) return false;
			if (this.ignoreLight != null && tile.ignoresLight != this.ignoreLight) return false;
			if (this.noAI != null && tile.hasNoAI != this.noAI) return false;
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
				if (listener.getTriggerInstance().test(tile, modif)) {
					if (list == null) {
						list = Lists.newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (ICriterionTrigger.Listener<ModifierTrigger.Instance> listener1 : list) {
					listener1.run(this.playerAdvancements);
				}
			}

		}
	}
}