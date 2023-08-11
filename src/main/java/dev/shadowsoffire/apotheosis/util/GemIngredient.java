package dev.shadowsoffire.apotheosis.util;

import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.shadowsoffire.apotheosis.adventure.Adventure;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
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

    public GemIngredient(DynamicHolder<LootRarity> rarity) {
        super(Stream.of(Adventure.Items.GEM.get()).map(ItemStack::new).map(ItemValue::new));
        this.rarity = rarity;
    }

    @Override
    public boolean test(ItemStack stack) {
        var rarity = AffixHelper.getRarity(stack);
        return super.test(stack) && rarity.isBound() && rarity == this.rarity;
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
