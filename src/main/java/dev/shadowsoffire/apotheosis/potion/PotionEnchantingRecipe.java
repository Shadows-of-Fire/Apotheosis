package dev.shadowsoffire.apotheosis.potion;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.ench.table.EnchantingRecipe;
import dev.shadowsoffire.apotheosis.ench.table.EnchantingStatRegistry.Stats;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;

public class PotionEnchantingRecipe extends EnchantingRecipe {

    public static final Serializer SERIALIZER = new Serializer();

    public PotionEnchantingRecipe(Stats requirements, Stats maxRequirements) {
        super(new ResourceLocation(Apotheosis.MODID, "potion_charm_enchanting"), charm(), potion(), requirements, maxRequirements);
    }

    private static ItemStack charm() {
        ItemStack out = new ItemStack(Apoth.Items.POTION_CHARM.get());
        out.getOrCreateTag().putBoolean("Unbreakable", true);
        return out;
    }

    private static Ingredient potion() {
        List<ItemStack> potionStacks = new ArrayList<>();
        for (Potion p : ForgeRegistries.POTIONS) {
            if (p.getEffects().size() != 1 || p.getEffects().get(0).getEffect().isInstantenous()) continue;
            ItemStack potion = new ItemStack(Apoth.Items.POTION_CHARM.get());
            PotionUtils.setPotion(potion, p);
            potionStacks.add(potion);
        }
        return Ingredient.of(potionStacks.toArray(new ItemStack[0]));
    }

    @Override
    public boolean matches(ItemStack input, float eterna, float quanta, float arcana) {
        if (input.hasTag() && input.getTag().getBoolean("Unbreakable")) return false;
        return super.matches(input, eterna, quanta, arcana);
    }

    @Override
    public ItemStack assemble(ItemStack input, float eterna, float quanta, float arcana) {
        ItemStack out = input.copy();
        out.setDamageValue(0);
        out.getOrCreateTag().putBoolean("Unbreakable", true);
        return out;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PotionEnchantingRecipe.SERIALIZER;
    }

    public static class Serializer implements RecipeSerializer<PotionEnchantingRecipe> {

        protected static final Gson GSON = new GsonBuilder().create();

        @Override
        public PotionEnchantingRecipe fromJson(ResourceLocation id, JsonObject obj) {
            Pair<Stats, Stats> requirements = readStats(id, obj);
            return new PotionEnchantingRecipe(requirements.getLeft(), requirements.getRight());
        }

        @Override
        public PotionEnchantingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Stats stats = Stats.read(buf);
            Stats maxStats = buf.readBoolean() ? Stats.read(buf) : NO_MAX;
            return new PotionEnchantingRecipe(stats, maxStats);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, PotionEnchantingRecipe recipe) {
            recipe.requirements.write(buf);
            buf.writeBoolean(recipe.maxRequirements != NO_MAX);
            if (recipe.maxRequirements != NO_MAX) {
                recipe.maxRequirements.write(buf);
            }
        }

    }

}
