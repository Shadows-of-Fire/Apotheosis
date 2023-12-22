package dev.shadowsoffire.apotheosis.adventure.loot;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.reflect.TypeToken;

import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class AffixLootPoolEntry extends LootPoolSingletonContainer {
    public static final LootPoolEntryType TYPE = new LootPoolEntryType(new AffixLootPoolEntry.Serializer());

    @Nullable
    private final RarityClamp.Simple rarityLimit;
    private final List<DynamicHolder<AffixLootEntry>> entries;

    public AffixLootPoolEntry(@Nullable RarityClamp.Simple rarityLimit, List<ResourceLocation> entries, int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions) {
        super(weight, quality, conditions, functions);
        this.rarityLimit = rarityLimit;
        this.entries = entries.stream().map(AffixLootRegistry.INSTANCE::holder).toList();
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> list, LootContext ctx) {
        ItemStack stack;
        if (this.entries.isEmpty()) {
            var player = GemLootPoolEntry.findPlayer(ctx);
            if (player == null) return;
            LootRarity selectedRarity = LootRarity.random(ctx.getRandom(), ctx.getLuck(), this.rarityLimit);
            stack = LootController.createRandomLootItem(ctx.getRandom(), selectedRarity, player, ctx.getLevel());
        }
        else {
            List<Wrapper<AffixLootEntry>> resolved = this.entries.stream().map(this::unwrap).filter(Objects::nonNull).map(e -> e.<AffixLootEntry>wrap(ctx.getLuck())).toList();
            AffixLootEntry entry = WeightedRandom.getRandomItem(ctx.getRandom(), resolved).get().getData();
            LootRarity selectedRarity = LootRarity.random(ctx.getRandom(), ctx.getLuck(), this.rarityLimit == null ? entry : this.rarityLimit);
            stack = LootController.createLootItem(entry.getStack().copy(), selectedRarity, ctx.getRandom());
        }
        if (!stack.isEmpty()) list.accept(stack);
    }

    @Override
    public LootPoolEntryType getType() {
        return TYPE;
    }

    /**
     * Unwraps the holder to its object, if present, otherwise returns null and logs an error.
     */
    private AffixLootEntry unwrap(DynamicHolder<AffixLootEntry> holder) {
        if (!holder.isBound()) {
            AdventureModule.LOGGER.error("An AffixLootPoolEntry failed to resolve the Affix Loot Entry {}!", holder.getId());
            return null;
        }
        return holder.get();
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<AffixLootPoolEntry> {

        @Override
        protected AffixLootPoolEntry deserialize(JsonObject obj, JsonDeserializationContext context, int weight, int quality, LootItemCondition[] lootConditions, LootItemFunction[] lootFunctions) {
            RarityClamp.Simple rarity;
            if (obj.has("min_rarity") || obj.has("max_rarity")) {
                DynamicHolder<LootRarity> minRarity = RarityRegistry.byLegacyId(GsonHelper.getAsString(obj, "min_rarity"));
                DynamicHolder<LootRarity> maxRarity = RarityRegistry.byLegacyId(GsonHelper.getAsString(obj, "max_rarity"));
                rarity = new RarityClamp.Simple(minRarity, maxRarity);
            }
            else {
                rarity = null;
            }
            List<String> entries = context.deserialize(GsonHelper.getAsJsonArray(obj, "entries", new JsonArray()), new TypeToken<List<String>>(){}.getType());
            return new AffixLootPoolEntry(rarity, entries.stream().map(ResourceLocation::new).toList(), weight, quality, lootConditions, lootFunctions);
        }

        @Override
        public void serializeCustom(JsonObject object, AffixLootPoolEntry e, JsonSerializationContext ctx) {
            if (e.rarityLimit != null) {
                object.addProperty("min_rarity", e.rarityLimit.min().getId().toString());
                object.addProperty("max_rarity", e.rarityLimit.max().getId().toString());
            }
            object.add("entries", ctx.serialize(e.entries));
            super.serializeCustom(object, e, ctx);
        }

    }
}
