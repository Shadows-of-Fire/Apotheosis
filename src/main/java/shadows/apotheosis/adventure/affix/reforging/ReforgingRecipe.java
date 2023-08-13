package shadows.apotheosis.adventure.affix.reforging;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.adventure.loot.LootRarity;

public record ReforgingRecipe(ResourceLocation id, LootRarity rarity, int matCost, int dustCost, int levelCost) implements Recipe<Container> {

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Apoth.RecipeTypes.REFORGING;
    }

    public static class Serializer implements RecipeSerializer<ReforgingRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public ReforgingRecipe fromJson(ResourceLocation id, JsonObject obj) {
            LootRarity rarity = LootRarity.byId(GsonHelper.getAsString(obj, "rarity"));
            Preconditions.checkNotNull(rarity);
            int matCost = GsonHelper.getAsInt(obj, "material_cost");
            int dustCost = GsonHelper.getAsInt(obj, "dust_cost");
            int levelCost = GsonHelper.getAsInt(obj, "level_cost");
            return new ReforgingRecipe(id, rarity, matCost, dustCost, levelCost);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ReforgingRecipe recipe) {
            buf.writeUtf(recipe.rarity.id());
            buf.writeByte(recipe.matCost);
            buf.writeByte(recipe.dustCost);
            buf.writeByte(recipe.levelCost);
        }

        @Override
        public ReforgingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            LootRarity rarity = LootRarity.byId(buf.readUtf());
            return new ReforgingRecipe(id, rarity, buf.readByte(), buf.readByte(), buf.readByte());
        }

    }

    @Override
    @Deprecated
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    @Deprecated
    public boolean matches(Container pContainer, Level pLevel) {
        return false;
    }

    @Override
    @Deprecated
    public ItemStack assemble(Container pContainer) {
        return ItemStack.EMPTY;
    }

    @Override
    @Deprecated
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }
}
