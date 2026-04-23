package dev.shadowsoffire.apotheosis.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.item.PotionCharmItem;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

public class PotionCharmRecipe extends ShapedRecipe {

    private final Recipe.CommonInfo common;
    private final CraftingRecipe.CraftingBookInfo book;

    public PotionCharmRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern) {
        super(commonInfo, bookInfo, pattern, new ItemStackTemplate(Apoth.Items.POTION_CHARM));
        this.common = commonInfo;
        this.book = bookInfo;
    }

    public Recipe.CommonInfo commonInfo() {
        return this.common;
    }

    public CraftingRecipe.CraftingBookInfo bookInfo() {
        return this.book;
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        ItemStack out = super.assemble(inv);
        PotionContents contents = findPotion(inv);
        if (contents == PotionContents.EMPTY) {
            return ItemStack.EMPTY;
        }
        Holder<Potion> potion = contents.potion().get();
        out.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return out;
    }

    @Override
    public boolean matches(CraftingInput inv, Level world) {
        if (super.matches(inv, world)) {
            return findPotion(inv) != PotionContents.EMPTY;
        }
        return false;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer) SERIALIZER;
    }

    /**
     * Returns the deduced {@link PotionContents} from a given {@link CraftingInput}.
     * <p>
     * A contents is found by being the only {@link PotionCharmItem#isValidPotion valid} contents in the entire grid,
     * and by being the same as all other potion contents found in the grid.
     *
     * @return The deduced contents, or {@link PotionContents#EMPTY} if none was found.
     */
    public static PotionContents findPotion(CraftingInput input) {
        PotionContents found = PotionContents.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);

            if (contents == null) {
                continue;
            }

            // If we run into an invalid potion, bail.
            if (!PotionCharmItem.isValidPotion(contents.potion().orElse(Potions.WATER))) {
                return PotionContents.EMPTY;
            }

            // If we haven't found one yet, and we find one, retain it.
            if (found == PotionContents.EMPTY) {
                found = contents;
            }
            else if (!contents.equals(found)) {
                // Otherwise, if we have found one, and we find a mismatch, abort early.
                return PotionContents.EMPTY;
            }
        }

        return found;
    }

    public static final MapCodec<PotionCharmRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst
        .group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(PotionCharmRecipe::commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(PotionCharmRecipe::bookInfo),
            ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern))
        .apply(inst, PotionCharmRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PotionCharmRecipe> STREAM_CODEC = StreamCodec.composite(
        Recipe.CommonInfo.STREAM_CODEC, PotionCharmRecipe::commonInfo,
        CraftingRecipe.CraftingBookInfo.STREAM_CODEC, PotionCharmRecipe::bookInfo,
        ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.pattern,
        PotionCharmRecipe::new);

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final RecipeSerializer<PotionCharmRecipe> SERIALIZER = new RecipeSerializer(CODEC, STREAM_CODEC);

}
