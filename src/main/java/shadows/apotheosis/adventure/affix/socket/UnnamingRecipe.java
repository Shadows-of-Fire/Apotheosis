package shadows.apotheosis.adventure.affix.socket;

import com.google.gson.JsonObject;

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
import shadows.apotheosis.Apoth;
import shadows.apotheosis.adventure.AdventureModule.ApothUpgradeRecipe;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.loot.LootRarity;

public class UnnamingRecipe extends ApothUpgradeRecipe {

    private static final ResourceLocation ID = new ResourceLocation("apotheosis:unnaming");

    public UnnamingRecipe() {
        super(ID, Ingredient.EMPTY, Ingredient.of(Apoth.Items.VIAL_OF_UNNAMING.get()), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Container pInv, Level pLevel) {
        if (pInv.getItem(0).isEmpty()) return false;
        CompoundTag afxData = pInv.getItem(0).getTagElement(AffixHelper.AFFIX_DATA);
        boolean hasName = afxData != null && afxData.contains(AffixHelper.NAME, 8);
        return hasName && pInv.getItem(1).getItem() == Apoth.Items.VIAL_OF_UNNAMING.get();
    }

    @Override
    public ItemStack assemble(Container pInv) {
        ItemStack out = pInv.getItem(0).copy();
        CompoundTag afxData = out.getTagElement(AffixHelper.AFFIX_DATA);
        LootRarity rarity = AffixHelper.getRarity(afxData);
        if (afxData == null || rarity == null) return ItemStack.EMPTY;
        // args[1] will be set to the item's underlying name. args[0] will be ignored.
        Component comp = Component.translatable("%2$s", "", "").withStyle(Style.EMPTY.withColor(rarity.color()));
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
