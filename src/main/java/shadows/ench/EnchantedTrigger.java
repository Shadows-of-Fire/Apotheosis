package shadows.ench;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EnchantedItemTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.util.ResourceLocation;
import shadows.Apotheosis;
import shadows.placebo.util.ReflectionHelper;

public class EnchantedTrigger extends EnchantedItemTrigger {
	private static final ResourceLocation ID = new ResourceLocation(Apotheosis.MODID, "enchanted_item");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
		ItemPredicate itempredicate = ItemPredicate.deserialize(json.get("item"));
		MinMaxBounds.IntBound minmaxbounds$intbound = MinMaxBounds.IntBound.fromJson(json.get("levels"));
		Instance inst = new Instance(itempredicate, minmaxbounds$intbound);
		ReflectionHelper.setPrivateValue(CriterionInstance.class, inst, ID, "field_192245_a", "criterion");
		return inst;
	}

}
