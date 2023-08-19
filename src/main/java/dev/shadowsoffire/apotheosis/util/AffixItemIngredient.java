package dev.shadowsoffire.apotheosis.util;

import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootController;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

public class AffixItemIngredient extends AbstractIngredient {

    protected final DynamicHolder<LootRarity> rarity;
    protected ItemStack[] items;

    public AffixItemIngredient(DynamicHolder<LootRarity> rarity) {
        this.rarity = rarity;
    }

    @Override
    public boolean test(ItemStack stack) {
        var rarity = AffixHelper.getRarity(stack);
        var affixes = AffixHelper.getAffixes(stack);
        return affixes.size() > 0 && rarity.isBound() && rarity == this.rarity;
    }

    @Override
    public ItemStack[] getItems() {
        if (this.items == null) {
            this.items = createFakeDisplayItems(this.rarity.get()).toArray(new ItemStack[0]);
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

    public static class Serializer implements IIngredientSerializer<AffixItemIngredient> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public AffixItemIngredient parse(FriendlyByteBuf buffer) {
            var rarity = RarityRegistry.INSTANCE.holder(buffer.readResourceLocation());
            return new AffixItemIngredient(rarity);
        }

        @Override
        public AffixItemIngredient parse(JsonObject json) {
            var rarity = RarityRegistry.INSTANCE.holder(new ResourceLocation(GsonHelper.getAsString(json, "rarity")));
            return new AffixItemIngredient(rarity);
        }

        @Override
        public void write(FriendlyByteBuf buffer, AffixItemIngredient ingredient) {
            buffer.writeResourceLocation(ingredient.rarity.getId());
        }
    }

    private static List<ItemStack> createFakeDisplayItems(LootRarity rarity) {
        RandomSource src = new LegacyRandomSource(0);
        List<ItemStack> out = Arrays.asList(Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS).stream().map(ItemStack::new).toList();
        out.forEach(stack -> {
            LootController.createLootItem(stack, rarity, src);
            AffixHelper.setName(stack, Component.translatable("text.apotheosis.any_x_item", rarity.toComponent(), "").withStyle(Style.EMPTY.withColor(rarity.getColor())));
        });
        return out;
    }

}
