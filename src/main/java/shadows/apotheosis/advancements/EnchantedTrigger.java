package shadows.apotheosis.advancements;

import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.EnchantedItemTrigger;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.MinMaxBounds.FloatBound;
import net.minecraft.advancements.criterion.MinMaxBounds.IntBound;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;

public class EnchantedTrigger extends EnchantedItemTrigger {

	@Override
	public Instance deserializeTrigger(JsonObject json, EntityPredicate.AndPredicate entityPredicate, ConditionArrayParser conditionsParser) {
		ItemPredicate item = ItemPredicate.deserialize(json.get("item"));
		IntBound levels = IntBound.fromJson(json.get("levels"));
		FloatBound eterna = FloatBound.fromJson(json.get("eterna"));
		FloatBound quanta = FloatBound.fromJson(json.get("quanta"));
		FloatBound arcana = FloatBound.fromJson(json.get("arcana"));
		Instance inst = new Instance(item, levels, eterna, quanta, arcana);
		return inst;
	}

	public void trigger(ServerPlayerEntity player, ItemStack stack, int level, float eterna, float quanta, float arcana) {
		this.triggerListeners(player, inst -> {
			if (inst instanceof Instance) return ((Instance) inst).test(stack, level, eterna, quanta, arcana);
			return inst.test(stack, level);
		});
	}

	public static class Instance extends EnchantedItemTrigger.Instance {

		protected final FloatBound eterna, quanta, arcana;

		public Instance(ItemPredicate item, IntBound levels, FloatBound eterna, FloatBound quanta, FloatBound arcana) {
			super(EntityPredicate.AndPredicate.ANY_AND, item, levels);
			this.eterna = eterna;
			this.quanta = quanta;
			this.arcana = arcana;
		}

		public static EnchantedItemTrigger.Instance any() {
			return new EnchantedItemTrigger.Instance(EntityPredicate.AndPredicate.ANY_AND, ItemPredicate.ANY, MinMaxBounds.IntBound.UNBOUNDED);
		}

		public boolean test(ItemStack stack, int level, float eterna, float quanta, float arcana) {
			return super.test(stack, level) && this.eterna.test(eterna) && this.quanta.test(quanta) && this.arcana.test(arcana);
		}

		@Override
		public boolean test(ItemStack stack, int level) {
			return this.test(stack, level, 0, 0, 0);
		}

		@Override
		public JsonObject serialize(ConditionArraySerializer serializer) {
			JsonObject jsonobject = super.serialize(serializer);
			jsonobject.add("eterna", this.eterna.serialize());
			jsonobject.add("quanta", this.quanta.serialize());
			jsonobject.add("arcana", this.arcana.serialize());
			return jsonobject;
		}
	}

}