package dev.shadowsoffire.apotheosis.compat.enchanting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apothic_enchanting.table.EnchantingStatRegistry.Stats;
import dev.shadowsoffire.apothic_enchanting.table.infusion.InfusionRecipe;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CharmInfusionRecipe extends InfusionRecipe {

    public CharmInfusionRecipe(Stats requirements, Stats maxRequirements) {
        super(charm(), potion(), requirements, maxRequirements);
    }

    private static ItemStackTemplate charm() {
        DataComponentPatch patch = DataComponentPatch.builder().set(DataComponents.UNBREAKABLE, Unit.INSTANCE).build();
        return new ItemStackTemplate(Apoth.Items.POTION_CHARM, 1, patch);
    }

    private static Ingredient potion() {
        return Ingredient.of(Apoth.Items.POTION_CHARM.value());
    }

    @Override
    public boolean matches(ItemStack input, float eterna, float quanta, float arcana) {
        return !input.has(DataComponents.UNBREAKABLE) && super.matches(input, eterna, quanta, arcana);
    }

    @Override
    public ItemStack assemble(ItemStack input, float eterna, float quanta, float arcana) {
        ItemStack out = input.copy();
        out.setDamageValue(0);
        out.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
        return out;
    }

    @Override
    public RecipeSerializer<? extends net.minecraft.world.item.crafting.Recipe<net.minecraft.world.item.crafting.RecipeInput>> getSerializer() {
        return SERIALIZER;
    }

    public static final MapCodec<CharmInfusionRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        Stats.CODEC.fieldOf("requirements").forGetter(InfusionRecipe::getRequirements),
        Stats.CODEC.optionalFieldOf("max_requirements", NO_MAX).forGetter(InfusionRecipe::getMaxRequirements))
        .apply(inst, CharmInfusionRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CharmInfusionRecipe> STREAM_CODEC = StreamCodec.composite(
        Stats.STREAM_CODEC, InfusionRecipe::getRequirements,
        Stats.STREAM_CODEC, InfusionRecipe::getMaxRequirements,
        CharmInfusionRecipe::new);

    public static final RecipeSerializer<CharmInfusionRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

}
