package dev.shadowsoffire.apotheosis.advancements;

import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.shadowsoffire.apotheosis.adventure.Adventure;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
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

// TODO: Replace with uses of forge's Custom ItemPredicate API. That's all this does anyway.
public class ExtendedInvTrigger extends InventoryChangeTrigger {

    @Override
    public InventoryChangeTrigger.TriggerInstance createInstance(JsonObject json, ContextAwarePredicate andPred, DeserializationContext conditionsParser) {
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
        if ("spawn_egg".equals(type)) return new ItemPredicate[] { new TrueItemPredicate(s -> s.getItem() instanceof SpawnEggItem) };
        if ("enchanted".equals(type)) {
            Enchantment ench = json.has("enchantment") ? ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(json.get("enchantment").getAsString())) : null;
            Ints bound = Ints.fromJson(json.get("level"));
            return new ItemPredicate[] { new TrueItemPredicate(s -> {
                Map<Enchantment, Integer> enchMap = EnchantmentHelper.getEnchantments(s);
                if (ench != null) return bound.matches(enchMap.getOrDefault(ench, 0));
                return enchMap.values().stream().anyMatch(bound::matches);
            }) };
        }
        if ("affix".equals(type)) {
            return new ItemPredicate[] { new TrueItemPredicate(s -> !AffixHelper.getAffixes(s).isEmpty()) };
        }
        if ("rarity".equals(type)) {
            var rarity = RarityRegistry.byLegacyId(json.get("rarity").getAsString().toLowerCase(Locale.ROOT));
            return new ItemPredicate[] { new TrueItemPredicate(s -> !AffixHelper.getAffixes(s).isEmpty() && rarity.isBound() && AffixHelper.getRarity(s) == rarity) };
        }
        if ("gem_rarity".equals(type)) {
            var rarity = RarityRegistry.byLegacyId(json.get("rarity").getAsString().toLowerCase(Locale.ROOT));
            return new ItemPredicate[] { new TrueItemPredicate(s -> s.getItem() == Adventure.Items.GEM.get() && rarity.isBound() && AffixHelper.getRarity(s) == rarity) };
        }
        if ("socket".equals(type)) {
            return new ItemPredicate[] { new TrueItemPredicate(s -> SocketHelper.getGems(s).stream().anyMatch(GemInstance::isValid)) };
        }
        if ("nbt".equals(type)) {
            CompoundTag tag;
            try {
                tag = TagParser.parseTag(GsonHelper.convertToString(json.get("nbt"), "nbt"));
            }
            catch (CommandSyntaxException e) {
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
