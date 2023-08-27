package shadows.apotheosis.util;

import java.util.Collection;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.adventure.affix.socket.gem.Gem;
import shadows.apotheosis.adventure.affix.socket.gem.GemItem;
import shadows.apotheosis.adventure.affix.socket.gem.GemManager;
import shadows.apotheosis.adventure.loot.LootRarity;

public class GemIngredient extends AbstractIngredient {

    protected final LootRarity rarity;
    protected ItemStack[] items;

    public GemIngredient(LootRarity rarity) {
        super(Stream.empty());
        this.rarity = rarity;
    }

    @Override
    public boolean test(ItemStack stack) {
        var rarity = GemItem.getLootRarity(stack);
        return stack.getItem() == Apoth.Items.GEM.get() && rarity != null && rarity == this.rarity;
    }

    @Override
    public ItemStack[] getItems() {
        if (this.items == null) {
            Collection<Gem> gems = GemManager.INSTANCE.getValues();
            if (gems.size() == 0) return new ItemStack[0]; // Hasn't been initialized yet, don't cache.
            this.items = new ItemStack[gems.size()];
            int i = 0;
            for (Gem g : GemManager.INSTANCE.getValues()) {
                this.items[i++] = GemManager.createGemStack(g, this.rarity);
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
        return this.rarity;
    }

    public static class Serializer implements IIngredientSerializer<GemIngredient> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public GemIngredient parse(FriendlyByteBuf buffer) {
            var rarity = LootRarity.byId(buffer.readUtf());
            return new GemIngredient(rarity);
        }

        @Override
        public GemIngredient parse(JsonObject json) {
            var rarity = LootRarity.byId(GsonHelper.getAsString(json, "rarity"));
            Preconditions.checkNotNull(rarity);
            return new GemIngredient(rarity);
        }

        @Override
        public void write(FriendlyByteBuf buffer, GemIngredient ingredient) {
            buffer.writeUtf(ingredient.rarity.id());
        }
    }

}
