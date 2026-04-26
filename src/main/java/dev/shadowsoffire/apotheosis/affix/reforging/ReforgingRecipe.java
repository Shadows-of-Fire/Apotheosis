package dev.shadowsoffire.apotheosis.affix.reforging;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.RecipeTypes;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public record ReforgingRecipe(DynamicHolder<LootRarity> rarity, int matCost, int sigilCost, int levelCost, HolderSet<Block> tables) implements Recipe<RecipeInput> {

    public static final MapCodec<ReforgingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        RarityRegistry.INSTANCE.holderCodec().fieldOf("rarity").forGetter(ReforgingRecipe::rarity),
        Codec.intRange(1, 99).fieldOf("material_cost").forGetter(ReforgingRecipe::matCost),
        Codec.intRange(0, 99).fieldOf("sigil_cost").forGetter(ReforgingRecipe::sigilCost),
        Codec.intRange(0, 65536).fieldOf("level_cost").forGetter(ReforgingRecipe::levelCost),
        RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("tables").forGetter(ReforgingRecipe::tables))
        .apply(inst, ReforgingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReforgingRecipe> STREAM_CODEC = StreamCodec.composite(
        RarityRegistry.INSTANCE.holderStreamCodec(), ReforgingRecipe::rarity,
        ByteBufCodecs.VAR_INT, ReforgingRecipe::matCost,
        ByteBufCodecs.VAR_INT, ReforgingRecipe::sigilCost,
        ByteBufCodecs.VAR_INT, ReforgingRecipe::levelCost,
        ByteBufCodecs.holderSet(Registries.BLOCK), ReforgingRecipe::tables,
        ReforgingRecipe::new);

    public static final RecipeSerializer<ReforgingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return RecipeTypes.REFORGING;
    }

    @Override
    @Deprecated
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    @Deprecated
    public ItemStack assemble(RecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return net.minecraft.world.item.crafting.RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

}
