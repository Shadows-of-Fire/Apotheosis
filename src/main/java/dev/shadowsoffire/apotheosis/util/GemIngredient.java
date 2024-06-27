package dev.shadowsoffire.apotheosis.util;

import java.util.Collection;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.shadowsoffire.apotheosis.adventure.Adventure;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

public class GemIngredient extends AbstractIngredient {

    protected final DynamicHolder<LootRarity> rarity;
    protected ItemStack[] items;

    public GemIngredient(DynamicHolder<LootRarity> rarity) {
        super(Stream.empty());
        this.rarity = rarity;
    }

    @Override
    public boolean test(ItemStack stack) {
        var rarity = AffixHelper.getRarity(stack);
        return stack.getItem() == Adventure.Items.GEM.get() && rarity.isBound() && rarity == this.rarity;
    }

    @Override
    public ItemStack[] getItems() {
        if (this.items == null) {
            Collection<Gem> gems = GemRegistry.INSTANCE.getValues();
            if (gems.size() == 0) return new ItemStack[0]; // Hasn't been initialized yet, don't cache.
            this.items = new ItemStack[gems.size()];
            int i = 0;
            for (Gem g : GemRegistry.INSTANCE.getValues()) {
                this.items[i++] = GemRegistry.createGemStack(g, this.rarity.get());
            }
        }
        return this.items;
    }

    @Override
    protected void invalidate() {
        super.invalidate();
        this.items = null;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public JsonElement toJson() {
        return new JsonObject();
    }

    public LootRarity getRarity() {
        return this.rarity.get();
    }

    public static class Serializer implements IIngredientSerializer<GemIngredient> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public GemIngredient parse(FriendlyByteBuf buffer) {
            var rarity = RarityRegistry.INSTANCE.holder(buffer.readResourceLocation());
            return new GemIngredient(rarity);
        }

        @Override
        public GemIngredient parse(JsonObject json) {
            var rarity = RarityRegistry.INSTANCE.holder(new ResourceLocation(GsonHelper.getAsString(json, "rarity")));
            return new GemIngredient(rarity);
        }

        @Override
        public void write(FriendlyByteBuf buffer, GemIngredient ingredient) {
            buffer.writeResourceLocation(ingredient.rarity.getId());
        }
    }

}
