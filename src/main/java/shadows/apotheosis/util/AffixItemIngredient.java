package shadows.apotheosis.util;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.loot.LootController;
import shadows.apotheosis.adventure.loot.LootRarity;

public class AffixItemIngredient extends AbstractIngredient {

    protected final LootRarity rarity;
    protected ItemStack[] items;

    public AffixItemIngredient(LootRarity rarity) {
        this.rarity = rarity;
    }

    @Override
    public boolean test(ItemStack stack) {
        var rarity = AffixHelper.getRarity(stack);
        var affixes = AffixHelper.getAffixes(stack);
        return affixes.size() > 0 && rarity != null && rarity == this.rarity;
    }

    @Override
    public ItemStack[] getItems() {
        if (this.items == null) {
            this.items = createFakeDisplayItems(this.rarity).toArray(new ItemStack[0]);
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
        return this.rarity;
    }

    public static class Serializer implements IIngredientSerializer<AffixItemIngredient> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public AffixItemIngredient parse(FriendlyByteBuf buffer) {
            var rarity = LootRarity.byId(buffer.readUtf());
            return new AffixItemIngredient(rarity);
        }

        @Override
        public AffixItemIngredient parse(JsonObject json) {
            var rarity = LootRarity.byId(GsonHelper.getAsString(json, "rarity"));
            Preconditions.checkNotNull(rarity);
            return new AffixItemIngredient(rarity);
        }

        @Override
        public void write(FriendlyByteBuf buffer, AffixItemIngredient ingredient) {
            buffer.writeUtf(ingredient.rarity.id());
        }
    }

    private static List<ItemStack> createFakeDisplayItems(LootRarity rarity) {
        RandomSource src = new LegacyRandomSource(0);
        List<ItemStack> out = Arrays.asList(Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS).stream().map(ItemStack::new).toList();
        out.forEach(stack -> {
            LootController.createLootItem(stack, rarity, src);
            AffixHelper.setName(stack, Component.translatable("text.apotheosis.any_x_item", rarity.toComponent(), "").withStyle(Style.EMPTY.withColor(rarity.color())));
        });
        return out;
    }

}
