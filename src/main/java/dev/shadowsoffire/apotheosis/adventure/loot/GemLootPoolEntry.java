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

import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.IDimensional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class GemLootPoolEntry extends LootPoolSingletonContainer {
    public static final Serializer SERIALIZER = new Serializer();
    public static final LootPoolEntryType TYPE = new LootPoolEntryType(SERIALIZER);

    private final List<DynamicHolder<Gem>> gems;

    public GemLootPoolEntry(List<ResourceLocation> gems, int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions) {
        super(weight, quality, conditions, functions);
        this.gems = gems.stream().map(GemRegistry.INSTANCE::holder).toList();
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> list, LootContext ctx) {
        Gem gem;

        if (!this.gems.isEmpty()) {
            List<Wrapper<Gem>> resolved = this.gems.stream().map(this::unwrap).filter(Objects::nonNull).map(e -> e.<Gem>wrap(ctx.getLuck())).toList();
            gem = WeightedRandom.getRandomItem(ctx.getRandom(), resolved).get().getData();
        }
        else {
            var player = GemLootPoolEntry.findPlayer(ctx);
            if (player == null) return;
            gem = GemRegistry.INSTANCE.getRandomItem(ctx.getRandom(), ctx.getLuck(), IDimensional.matches(ctx.getLevel()), IStaged.matches(player));
        }

        RarityClamp clamp = AdventureConfig.GEM_DIM_RARITIES.get(ctx.getLevel().dimension().location());
        ItemStack stack = GemRegistry.createGemStack(gem, gem.clamp(LootRarity.random(ctx.getRandom(), ctx.getLuck(), clamp)));
        list.accept(stack);
    }

    @Override
    public LootPoolEntryType getType() {
        return TYPE;
    }

    /**
     * Unwraps the holder to its object, if present, otherwise returns null and logs an error.
     */
    private Gem unwrap(DynamicHolder<Gem> holder) {
        if (!holder.isBound()) {
            AdventureModule.LOGGER.error("A GemLootPoolEntry failed to resolve the Gem {}!", holder.getId());
            return null;
        }
        return holder.get();
    }

    @Nullable
    public static Player findPlayer(LootContext ctx) {
        if (ctx.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof Player p) return p;
        if (ctx.getParamOrNull(LootContextParams.DIRECT_KILLER_ENTITY) instanceof Player p) return p;
        if (ctx.getParamOrNull(LootContextParams.KILLER_ENTITY) instanceof Player p) return p;
        if (ctx.getParamOrNull(LootContextParams.LAST_DAMAGE_PLAYER) != null) return ctx.getParamOrNull(LootContextParams.LAST_DAMAGE_PLAYER);
        return null;
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<GemLootPoolEntry> {

        @Override
        protected GemLootPoolEntry deserialize(JsonObject jsonObject, JsonDeserializationContext context, int weight, int quality, LootItemCondition[] lootConditions, LootItemFunction[] lootFunctions) {
            List<String> gems = context.deserialize(GsonHelper.getAsJsonArray(jsonObject, "gems", new JsonArray()), new TypeToken<List<String>>(){}.getType());
            return new GemLootPoolEntry(gems.stream().map(ResourceLocation::new).toList(), weight, quality, lootConditions, lootFunctions);
        }

        @Override
        public void serializeCustom(JsonObject object, GemLootPoolEntry e, JsonSerializationContext ctx) {
            object.add("gems", ctx.serialize(e.gems));
            super.serializeCustom(object, e, ctx);
        }

    }
}
