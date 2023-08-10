package dev.shadowsoffire.apotheosis.ench.table;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.apotheosis.Apoth.RecipeTypes;
import dev.shadowsoffire.apotheosis.ench.table.EnchantingStatRegistry.Stats;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;

public class EnchantingRecipe implements Recipe<Container> {

    public static final Serializer SERIALIZER = new Serializer();
    public static final Stats NO_MAX = new Stats(-1, -1, -1, -1, -1, -1);

    protected final ResourceLocation id;
    protected final ItemStack output;
    protected final Ingredient input;
    protected final Stats requirements, maxRequirements;

    /**
     * Defines an Enchanting Recipe.
     *
     * @param id           The Recipe ID
     * @param output       The output ItemStack
     * @param input        The input Ingredient
     * @param requirements The Level, Quanta, and Arcana requirements respectively.
     * @param displayLevel The level to show on the fake "Infusion" Enchantment that will show up.
     */
    public EnchantingRecipe(ResourceLocation id, ItemStack output, Ingredient input, Stats requirements, Stats maxRequirements) {
        this.id = id;
        this.output = output;
        this.input = input;
        this.requirements = requirements;
        this.maxRequirements = maxRequirements;
    }

    public boolean matches(ItemStack input, float eterna, float quanta, float arcana) {
        if (this.maxRequirements.eterna() > -1 && eterna > this.maxRequirements.eterna() || this.maxRequirements.quanta() > -1 && quanta > this.maxRequirements.quanta()
            || this.maxRequirements.arcana() > -1 && arcana > this.maxRequirements.arcana()) return false;
        return this.input.test(input) && eterna >= this.requirements.eterna() && quanta >= this.requirements.quanta() && arcana >= this.requirements.arcana();
    }

    public Stats getRequirements() {
        return this.requirements;
    }

    public Stats getMaxRequirements() {
        return this.maxRequirements;
    }

    public Ingredient getInput() {
        return this.input;
    }

    public ItemStack getOutput() {
        return this.output;
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

    @Override
    @Deprecated
    public ItemStack getResultItem(RegistryAccess regs) {
        return this.output;
    }

    public ItemStack assemble(ItemStack input, float eterna, float quanta, float arcana) {
        return this.output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return EnchantingRecipe.SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeTypes.INFUSION;
    }

    protected static Pair<Stats, Stats> readStats(ResourceLocation id, JsonObject obj) {
        Stats stats = Stats.CODEC.decode(JsonOps.INSTANCE, obj.get("requirements")).get().left().get().getFirst();
        Stats maxStats = obj.has("max_requirements") ? Stats.CODEC.decode(JsonOps.INSTANCE, obj.get("max_requirements")).get().left().get().getFirst() : NO_MAX;
        if (maxStats.eterna() != -1 && stats.eterna() > maxStats.eterna()) throw new JsonParseException("An enchanting recipe (" + id + ") has invalid min/max eterna bounds (min > max).");
        if (maxStats.quanta() != -1 && stats.quanta() > maxStats.quanta()) throw new JsonParseException("An enchanting recipe (" + id + ") has invalid min/max quanta bounds (min > max).");
        if (maxStats.arcana() != -1 && stats.arcana() > maxStats.arcana()) throw new JsonParseException("An enchanting recipe (" + id + ") has invalid min/max arcana bounds (min > max).");
        return Pair.of(stats, maxStats);
    }

    public static class Serializer implements RecipeSerializer<EnchantingRecipe> {

        protected static final Gson GSON = new GsonBuilder().create();

        @Override
        public EnchantingRecipe fromJson(ResourceLocation id, JsonObject obj) {
            ItemStack output = CraftingHelper.getItemStack(obj.get("result").getAsJsonObject(), true, true);
            Ingredient input = Ingredient.fromJson(obj.get("input"));
            Pair<Stats, Stats> requirements = readStats(id, obj);
            return new EnchantingRecipe(id, output, input, requirements.getLeft(), requirements.getRight());
        }

        @Override
        public EnchantingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            ItemStack output = buf.readItem();
            Ingredient input = Ingredient.fromNetwork(buf);
            Stats stats = Stats.read(buf);
            Stats maxStats = buf.readBoolean() ? Stats.read(buf) : NO_MAX;
            return new EnchantingRecipe(id, output, input, stats, maxStats);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, EnchantingRecipe recipe) {
            buf.writeItem(recipe.output);
            recipe.input.toNetwork(buf);
            recipe.requirements.write(buf);
            buf.writeBoolean(recipe.maxRequirements != NO_MAX);
            if (recipe.maxRequirements != NO_MAX) {
                recipe.maxRequirements.write(buf);
            }
        }

    }

    @Nullable
    public static EnchantingRecipe findMatch(Level level, ItemStack input, float eterna, float quanta, float arcana) {
        List<EnchantingRecipe> recipes = new ArrayList<>(level.getRecipeManager().getAllRecipesFor(RecipeTypes.INFUSION));
        recipes.sort((r1, r2) -> -Float.compare(r1.requirements.eterna(), r2.requirements.eterna()));
        for (EnchantingRecipe r : recipes)
            if (r.matches(input, eterna, quanta, arcana)) return r;
        return null;
    }

    public static EnchantingRecipe findItemMatch(Level level, ItemStack toEnchant) {
        return level.getRecipeManager().getAllRecipesFor(RecipeTypes.INFUSION).stream().filter(r -> r.getInput().test(toEnchant)).findFirst().orElse(null);
    }

}
