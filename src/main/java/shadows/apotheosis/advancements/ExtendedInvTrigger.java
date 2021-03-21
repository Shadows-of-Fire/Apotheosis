package shadows.apotheosis.advancements;

import java.util.Map;
import java.util.function.Predicate;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.MinMaxBounds.IntBound;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.affix.LootRarity;

public class ExtendedInvTrigger extends InventoryChangeTrigger {

	@Override
	public InventoryChangeTrigger.Instance deserializeTrigger(JsonObject json, EntityPredicate.AndPredicate andPred, ConditionArrayParser conditionsParser) {
		JsonObject jsonobject = JSONUtils.getJsonObject(json, "slots", new JsonObject());
		MinMaxBounds.IntBound minmaxbounds$intbound = MinMaxBounds.IntBound.fromJson(jsonobject.get("occupied"));
		MinMaxBounds.IntBound minmaxbounds$intbound1 = MinMaxBounds.IntBound.fromJson(jsonobject.get("full"));
		MinMaxBounds.IntBound minmaxbounds$intbound2 = MinMaxBounds.IntBound.fromJson(jsonobject.get("empty"));
		ItemPredicate[] aitempredicate = ItemPredicate.deserializeArray(json.get("items"));
		if (json.has("apoth")) aitempredicate = this.deserializeApoth(json.getAsJsonObject("apoth"));
		return new InventoryChangeTrigger.Instance(andPred, minmaxbounds$intbound, minmaxbounds$intbound1, minmaxbounds$intbound2, aitempredicate);
	}

	ItemPredicate[] deserializeApoth(JsonObject json) {
		String type = json.get("type").getAsString();
		if (type.equals("spawn_egg")) return new ItemPredicate[] { new TrueItemPredicate(s -> s.getItem() instanceof SpawnEggItem) };
		if (type.equals("enchanted")) {
			Enchantment ench = json.has("enchantment") ? ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(json.get("enchantment").getAsString())) : null;
			IntBound bound = IntBound.fromJson(json.get("level"));
			return new ItemPredicate[] { new TrueItemPredicate(s -> {
				Map<Enchantment, Integer> enchMap = EnchantmentHelper.getEnchantments(s);
				if (ench != null) return bound.test(enchMap.getOrDefault(ench, 0));
				return enchMap.values().stream().anyMatch(bound::test);
			}) };
		}
		if (type.equals("affix")) {
			return new ItemPredicate[] { new TrueItemPredicate(s -> !AffixHelper.getAffixes(s).isEmpty()) };
		}
		if (type.equals("rarity")) {
			LootRarity rarity = LootRarity.valueOf(json.get("rarity").getAsString());
			return new ItemPredicate[] { new TrueItemPredicate(s -> AffixHelper.getRarity(s) == rarity) };
		}
		if (type.equals("nbt")) {
			CompoundNBT tag;
			try {
				tag = JsonToNBT.getTagFromJson(JSONUtils.getString(json.get("nbt"), "nbt"));
			} catch (CommandSyntaxException e) {
				throw new RuntimeException(e);
			}
			return new ItemPredicate[] { new TrueItemPredicate(s -> {
				if (!s.hasTag()) return false;
				for (String key : tag.keySet()) {
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
		public boolean test(ItemStack item) {
			return this.predicate.test(item);
		}
	}

}