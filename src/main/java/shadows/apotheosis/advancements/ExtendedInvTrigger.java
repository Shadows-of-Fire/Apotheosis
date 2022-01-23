package shadows.apotheosis.advancements;

import java.util.Map;
import java.util.function.Predicate;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class ExtendedInvTrigger extends InventoryChangeTrigger {

	@Override
	public InventoryChangeTrigger.TriggerInstance createInstance(JsonObject json, EntityPredicate.Composite andPred, DeserializationContext conditionsParser) {
		JsonObject slots = GsonHelper.getAsJsonObject(json, "slots", new JsonObject());
		MinMaxBounds.Ints occupied = MinMaxBounds.Ints.fromJson(slots.get("occupied"));
		MinMaxBounds.Ints full = MinMaxBounds.Ints.fromJson(slots.get("full"));
		MinMaxBounds.Ints empty = MinMaxBounds.Ints.fromJson(slots.get("empty"));
		ItemPredicate[] predicate = ItemPredicate.fromJsonArray(json.get("items"));
		if (json.has("apoth")) predicate = this.deserializeApoth(json.getAsJsonObject("apoth"));
		return new InventoryChangeTrigger.TriggerInstance(andPred, occupied, full, empty, predicate);
	}

	ItemPredicate[] deserializeApoth(JsonObject json) {
		String type = json.get("type").getAsString();
		if (type.equals("spawn_egg")) return new ItemPredicate[] { new TrueItemPredicate(s -> s.getItem() instanceof SpawnEggItem) };
		if (type.equals("enchanted")) {
			Enchantment ench = json.has("enchantment") ? ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(json.get("enchantment").getAsString())) : null;
			Ints bound = Ints.fromJson(json.get("level"));
			return new ItemPredicate[] { new TrueItemPredicate(s -> {
				Map<Enchantment, Integer> enchMap = EnchantmentHelper.getEnchantments(s);
				if (ench != null) return bound.matches(enchMap.getOrDefault(ench, 0));
				return enchMap.values().stream().anyMatch(bound::matches);
			}) };
		}
		//if (type.equals("affix")) {
		//	return new ItemPredicate[] { new TrueItemPredicate(s -> !AffixHelper.getAffixes(s).isEmpty()) };
		//}
		//if (type.equals("rarity")) {
		//	LootRarity rarity = LootRarity.valueOf(json.get("rarity").getAsString());
		//	return new ItemPredicate[] { new TrueItemPredicate(s -> AffixHelper.getRarity(s) == rarity) };
		//}
		if (type.equals("nbt")) {
			CompoundTag tag;
			try {
				tag = TagParser.parseTag(GsonHelper.convertToString(json.get("nbt"), "nbt"));
			} catch (CommandSyntaxException e) {
				throw new RuntimeException(e);
			}
			return new ItemPredicate[] { new TrueItemPredicate(s -> {
				if (!s.hasTag()) return false;
				for (String key : tag.getAllKeys()) {
					if (!tag.get(key).equals(s.getTag().get(key))) return false;
				}
				return true;
			}) };

		}
		return new ItemPredicate[0];
	}

	private static class TrueItemPredicate extends ItemPredicate {

		Predicate<ItemStack> predicate;

		TrueItemPredicate(Predicate<ItemStack> predicate) {
			this.predicate = predicate;
		}

		@Override
		public boolean matches(ItemStack item) {
			return this.predicate.test(item);
		}
	}

}