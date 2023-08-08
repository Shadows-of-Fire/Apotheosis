package dev.shadowsoffire.apotheosis.adventure.affix.salvaging;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.RecipeTypes;
import dev.shadowsoffire.placebo.json.ItemAdapter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class SalvagingRecipe implements Recipe<Container> {

    protected final ResourceLocation id;
    protected final Ingredient input;
    protected final List<OutputData> outputs;

    public SalvagingRecipe(ResourceLocation id, List<OutputData> outputs, Ingredient input) {
        this.id = id;
        this.outputs = outputs;
        this.input = input;
    }

    public boolean matches(ItemStack stack) {
        return this.input.test(stack);
    }

    public Ingredient getInput() {
        return this.input;
    }

    public List<OutputData> getOutputs() {
        return this.outputs;
    }

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
        return RecipeTypes.SALVAGING;
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

    public static class Serializer implements RecipeSerializer<SalvagingRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public SalvagingRecipe fromJson(ResourceLocation id, JsonObject obj) {
            var outputs = OutputData.LIST_CODEC.decode(JsonOps.INSTANCE, GsonHelper.getAsJsonArray(obj, "outputs")).result().get().getFirst();
            Ingredient input = Ingredient.fromJson(obj.get("input"));
            return new SalvagingRecipe(id, outputs, input);
        }

        @Override
        public SalvagingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            var outputs = OutputData.LIST_CODEC.decode(NbtOps.INSTANCE, buf.readNbt().get("outputs")).result().get().getFirst();
            Ingredient input = Ingredient.fromNetwork(buf);
            return new SalvagingRecipe(id, outputs, input);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, SalvagingRecipe recipe) {
            Tag outputs = OutputData.LIST_CODEC.encodeStart(NbtOps.INSTANCE, recipe.outputs).get().left().get();
            CompoundTag netWrapper = new CompoundTag();
            netWrapper.put("outputs", outputs);
            buf.writeNbt(netWrapper);
            recipe.input.toNetwork(buf);
        }

    }

    public static class OutputData {

        public static Codec<OutputData> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                ItemAdapter.CODEC.fieldOf("stack").forGetter(d -> d.stack),
                Codec.intRange(0, 64).fieldOf("min_count").forGetter(d -> d.min),
                Codec.intRange(0, 64).fieldOf("max_count").forGetter(d -> d.max))
            .apply(inst, OutputData::new));

        public static final Codec<List<OutputData>> LIST_CODEC = Codec.list(CODEC);

        ItemStack stack;
        int min, max;

        OutputData(ItemStack stack, int min, int max) {
            this.stack = stack;
            this.min = min;
            this.max = max;
            Preconditions.checkArgument(max >= min);
            this.stack.setCount(1);
        }

        public ItemStack getStack() {
            return this.stack;
        }

        public int getMin() {
            return this.min;
        }

        public int getMax() {
            return this.max;
        }

    }
}
