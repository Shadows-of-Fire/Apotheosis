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
	public Instance createInstance(JsonObject json, EntityPredicate.AndPredicate entityPredicate, ConditionArrayParser conditionsParser) {
		ItemPredicate item = ItemPredicate.fromJson(json.get("item"));
		IntBound levels = IntBound.fromJson(json.get("levels"));
		FloatBound eterna = FloatBound.fromJson(json.get("eterna"));
		FloatBound quanta = FloatBound.fromJson(json.get("quanta"));
		FloatBound arcana = FloatBound.fromJson(json.get("arcana"));
		Instance inst = new Instance(item, levels, eterna, quanta, arcana);
		return inst;
	}

	public void trigger(ServerPlayerEntity player, ItemStack stack, int level, float eterna, float quanta, float arcana) {
		this.trigger(player, inst -> {
			if (inst instanceof Instance) return ((Instance) inst).test(stack, level, eterna, quanta, arcana);
			return inst.matches(stack, level);
		});
	}

	public static class Instance extends EnchantedItemTrigger.Instance {

		protected final FloatBound eterna, quanta, arcana;

		public Instance(ItemPredicate item, IntBound levels, FloatBound eterna, FloatBound quanta, FloatBound arcana) {
			super(EntityPredicate.AndPredicate.ANY, item, levels);
			this.eterna = eterna;
			this.quanta = quanta;
			this.arcana = arcana;
		}

		public static EnchantedItemTrigger.Instance any() {
			return new EnchantedItemTrigger.Instance(EntityPredicate.AndPredicate.ANY, ItemPredicate.ANY, MinMaxBounds.IntBound.ANY);
		}

		public boolean test(ItemStack stack, int level, float eterna, float quanta, float arcana) {
			return super.matches(stack, level) && this.eterna.matches(eterna) && this.quanta.matches(quanta) && this.arcana.matches(arcana);
		}

		@Override
		public boolean matches(ItemStack stack, int level) {
			return this.test(stack, level, 0, 0, 0);
		}

		@Override
		public JsonObject serializeToJson(ConditionArraySerializer serializer) {
			JsonObject jsonobject = super.serializeToJson(serializer);
			jsonobject.add("eterna", this.eterna.serializeToJson());
			jsonobject.add("quanta", this.quanta.serializeToJson());
			jsonobject.add("arcana", this.arcana.serializeToJson());
			return jsonobject;
		}
	}

}