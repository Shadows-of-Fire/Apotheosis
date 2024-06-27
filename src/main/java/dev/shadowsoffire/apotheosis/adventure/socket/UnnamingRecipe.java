package dev.shadowsoffire.apotheosis.adventure.socket;

import com.google.gson.JsonObject;

import dev.shadowsoffire.apotheosis.adventure.Adventure.Items;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule.ApothSmithingRecipe;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class UnnamingRecipe extends ApothSmithingRecipe {

    private static final ResourceLocation ID = new ResourceLocation("apotheosis:unnaming");

    public UnnamingRecipe() {
        super(ID, Ingredient.EMPTY, Ingredient.of(Items.SIGIL_OF_UNNAMING.get()), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Container pInv, Level pLevel) {
        ItemStack base = pInv.getItem(BASE);
        if (base.isEmpty()) return false;
        CompoundTag afxData = base.getTagElement(AffixHelper.AFFIX_DATA);
        boolean hasName = afxData != null && afxData.contains(AffixHelper.NAME, 8);
        return hasName && pInv.getItem(ADDITION).getItem() == Items.SIGIL_OF_UNNAMING.get();
    }

    @Override
    public ItemStack assemble(Container pInv, RegistryAccess regs) {
        ItemStack out = pInv.getItem(BASE).copy();
        CompoundTag afxData = out.getTagElement(AffixHelper.AFFIX_DATA);
        DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(afxData);
        if (afxData == null || !rarity.isBound()) return ItemStack.EMPTY;
        // args[1] will be set to the item's underlying name. args[0] will be ignored.
        Component comp = Component.translatable("%2$s", "", "").withStyle(Style.EMPTY.withColor(rarity.get().getColor()));
        AffixHelper.setName(out, comp);
        return out;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<UnnamingRecipe> {

        public static Serializer INSTANCE = new Serializer();

        @Override
        public UnnamingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
            return new UnnamingRecipe();
        }

        @Override
        public UnnamingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            return new UnnamingRecipe();
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, UnnamingRecipe pRecipe) {

        }
    }

}
