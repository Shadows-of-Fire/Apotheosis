package dev.shadowsoffire.apotheosis.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

public class RarityIngredient extends AbstractIngredient {

    protected final DynamicHolder<LootRarity> rarity;

    public RarityIngredient(DynamicHolder<LootRarity> rarity) {
        this.rarity = rarity;
    }

    @Override
    public boolean test(ItemStack stack) {
        var rarity = AffixHelper.getRarity(stack);
        return rarity.isBound() && rarity == this.rarity;
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

    public static class Serializer implements IIngredientSerializer<RarityIngredient> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public RarityIngredient parse(FriendlyByteBuf buffer) {
            var rarity = RarityRegistry.byLegacyId(buffer.readUtf());
            return new RarityIngredient(rarity);
        }

        @Override
        public RarityIngredient parse(JsonObject json) {
            var rarity = RarityRegistry.byLegacyId(GsonHelper.getAsString(json, "rarity"));
            return new RarityIngredient(rarity);
        }

        @Override
        public void write(FriendlyByteBuf buffer, RarityIngredient ingredient) {
            buffer.writeResourceLocation(ingredient.rarity.getId());
        }
    }

}
