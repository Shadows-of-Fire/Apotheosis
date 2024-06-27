package dev.shadowsoffire.apotheosis.adventure.affix.reforging;

import com.google.gson.JsonObject;

import dev.shadowsoffire.apotheosis.Apoth.RecipeTypes;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record ReforgingRecipe(ResourceLocation id, DynamicHolder<LootRarity> rarity, int matCost, int sigilCost, int levelCost) implements Recipe<Container> {

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
        return RecipeTypes.REFORGING;
    }

    public static class Serializer implements RecipeSerializer<ReforgingRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public ReforgingRecipe fromJson(ResourceLocation id, JsonObject obj) {
            DynamicHolder<LootRarity> rarity = RarityRegistry.byLegacyId(GsonHelper.getAsString(obj, "rarity"));
            int matCost = GsonHelper.getAsInt(obj, "material_cost");
            int sigilCost = GsonHelper.getAsInt(obj, "sigil_cost");
            int levelCost = GsonHelper.getAsInt(obj, "level_cost");
            return new ReforgingRecipe(id, rarity, matCost, sigilCost, levelCost);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ReforgingRecipe recipe) {
            buf.writeResourceLocation(recipe.rarity.getId());
            buf.writeByte(recipe.matCost);
            buf.writeByte(recipe.sigilCost);
            buf.writeByte(recipe.levelCost);
        }

        @Override
        public ReforgingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            DynamicHolder<LootRarity> rarity = RarityRegistry.INSTANCE.holder(buf.readResourceLocation());
            return new ReforgingRecipe(id, rarity, buf.readByte(), buf.readByte(), buf.readByte());
        }

    }

    @Override
    @Deprecated
    public ItemStack getResultItem(RegistryAccess regs) {
        return ItemStack.EMPTY;
    }

    @Override
    @Deprecated
    public boolean matches(Container pContainer, Level pLevel) {
        return false;
    }

    @Override
    @Deprecated
    public ItemStack assemble(Container pContainer, RegistryAccess regs) {
        return ItemStack.EMPTY;
    }

    @Override
    @Deprecated
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }
}
