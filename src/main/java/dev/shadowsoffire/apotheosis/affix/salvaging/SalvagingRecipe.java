package dev.shadowsoffire.apotheosis.affix.salvaging;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.RecipeTypes;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public class SalvagingRecipe implements Recipe<SingleRecipeInput> {

    public static final MapCodec<SalvagingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(SalvagingRecipe::getInput),
        OutputData.CODEC.listOf().fieldOf("outputs").forGetter(SalvagingRecipe::getOutputs))
        .apply(inst, SalvagingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SalvagingRecipe> STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, SalvagingRecipe::getInput,
        OutputData.STREAM_CODEC.apply(ByteBufCodecs.list()), SalvagingRecipe::getOutputs,
        SalvagingRecipe::new);

    protected final Ingredient input;
    protected final List<OutputData> outputs;

    public SalvagingRecipe(Ingredient input, List<OutputData> outputs) {
        this.input = input;
        this.outputs = outputs;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.getItem(0));
    }

    public Ingredient getInput() {
        return this.input;
    }

    public List<OutputData> getOutputs() {
        return this.outputs;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeTypes.SALVAGING;
    }

    public static class Serializer implements RecipeSerializer<SalvagingRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public MapCodec<SalvagingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SalvagingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

    }

    public static record OutputData(ItemStack stack, int min, int max) {

        public static Codec<OutputData> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                ItemStack.CODEC.fieldOf("stack").forGetter(d -> d.stack),
                Codec.intRange(0, 99).fieldOf("min_count").forGetter(d -> d.min),
                Codec.intRange(1, 99).fieldOf("max_count").forGetter(d -> d.max))
            .apply(inst, OutputData::new));

        public static final Codec<List<OutputData>> LIST_CODEC = Codec.list(CODEC);

        public static final StreamCodec<RegistryFriendlyByteBuf, OutputData> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, OutputData::stack,
            ByteBufCodecs.VAR_INT, OutputData::min,
            ByteBufCodecs.VAR_INT, OutputData::max,
            OutputData::new);

        public OutputData(Item item, int min, int max) {
            this(item.getDefaultInstance(), min, max);
        }
    }

    @Override
    @Deprecated
    public ItemStack assemble(SingleRecipeInput input, Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    @Deprecated
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    @Deprecated
    public ItemStack getResultItem(Provider registries) {
        return ItemStack.EMPTY;
    }
}
