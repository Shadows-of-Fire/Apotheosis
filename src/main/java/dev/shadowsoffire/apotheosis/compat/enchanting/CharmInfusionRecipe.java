package dev.shadowsoffire.apotheosis.compat.enchanting;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.item.PotionCharmItem;
import dev.shadowsoffire.apothic_enchanting.table.EnchantingStatRegistry.Stats;
import dev.shadowsoffire.apothic_enchanting.table.infusion.InfusionRecipe;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CharmInfusionRecipe extends InfusionRecipe {

    public CharmInfusionRecipe(Stats requirements, Stats maxRequirements) {
        super(charm(), potion(), requirements, maxRequirements);
    }

    private static ItemStack charm() {
        ItemStack out = new ItemStack(Apoth.Items.POTION_CHARM);
        out.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        return out;
    }

    private static Ingredient potion() {
        List<ItemStack> potionStacks = new ArrayList<>();
        BuiltInRegistries.POTION.holders()
            .filter(PotionCharmItem::isValidPotion)
            .forEach(p -> {
                potionStacks.add(PotionContents.createItemStack(Apoth.Items.POTION_CHARM.value(), p));
            });
        return Ingredient.of(potionStacks.toArray(new ItemStack[0]));
    }

    @Override
    public boolean matches(ItemStack input, float eterna, float quanta, float arcana) {
        return !input.has(DataComponents.UNBREAKABLE) && super.matches(input, eterna, quanta, arcana);
    }

    @Override
    public ItemStack assemble(ItemStack input, float eterna, float quanta, float arcana) {
        ItemStack out = input.copy();
        out.setDamageValue(0);
        out.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        return out;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements RecipeSerializer<CharmInfusionRecipe> {

        public static final MapCodec<CharmInfusionRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Stats.CODEC.fieldOf("requirements").forGetter(InfusionRecipe::getRequirements),
            Stats.CODEC.optionalFieldOf("max_requirements", NO_MAX).forGetter(InfusionRecipe::getMaxRequirements))
            .apply(inst, CharmInfusionRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CharmInfusionRecipe> STREAM_CODEC = StreamCodec.composite(
            Stats.STREAM_CODEC, InfusionRecipe::getRequirements,
            Stats.STREAM_CODEC, InfusionRecipe::getMaxRequirements,
            CharmInfusionRecipe::new);

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public MapCodec<CharmInfusionRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CharmInfusionRecipe> streamCodec() {
            return STREAM_CODEC;
        }

    }

}
