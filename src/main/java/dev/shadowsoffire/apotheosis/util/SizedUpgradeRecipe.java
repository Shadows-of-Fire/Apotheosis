package dev.shadowsoffire.apotheosis.util;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.socket.ReactiveSmithingRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class SizedUpgradeRecipe implements SmithingRecipe, ReactiveSmithingRecipe {

    protected final Ingredient template;
    protected final Ingredient base;
    protected final SizedIngredient addition;
    protected final ItemStackTemplate result;

    public SizedUpgradeRecipe(Ingredient template, Ingredient base, SizedIngredient addition, ItemStackTemplate result) {
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        return this.template.test(input.template()) && this.base.test(input.base()) && this.addition.test(input.addition());
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input) {
        return this.result.create();
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
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @Override
    public void onCraft(Container inv, ServerPlayer player, ItemStack output) {
        int size = this.addition.count() - 1;
        ItemStack stack = inv.getItem(ApothSmithingRecipe.ADDITION);
        stack.shrink(size);
        inv.setItem(ApothSmithingRecipe.ADDITION, stack);
    }

    @Override
    public RecipeSerializer<? extends SmithingRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Optional<Ingredient> templateIngredient() {
        return Optional.of(this.template);
    }

    @Override
    public Ingredient baseIngredient() {
        return this.base;
    }

    @Override
    public Optional<Ingredient> additionIngredient() {
        return Optional.of(this.addition.ingredient());
    }

    public Ingredient template() {
        return this.template;
    }

    public Ingredient base() {
        return this.base;
    }

    public SizedIngredient addition() {
        return this.addition;
    }

    public ItemStack result() {
        return this.result.create();
    }

    public static final MapCodec<SizedUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst
        .group(
            Ingredient.CODEC.fieldOf("template").forGetter(r -> r.template),
            Ingredient.CODEC.fieldOf("base").forGetter(r -> r.base),
            SizedIngredient.NESTED_CODEC.fieldOf("addition").forGetter(r -> r.addition),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result))
        .apply(inst, SizedUpgradeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SizedUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, r -> r.template,
        Ingredient.CONTENTS_STREAM_CODEC, r -> r.base,
        SizedIngredient.STREAM_CODEC, r -> r.addition,
        ItemStackTemplate.STREAM_CODEC, r -> r.result,
        SizedUpgradeRecipe::new);

    public static final RecipeSerializer<SizedUpgradeRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

}
