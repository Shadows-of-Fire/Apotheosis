package dev.shadowsoffire.apotheosis.util;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class AffixItemIngredient implements ICustomIngredient {

    public static final MapCodec<AffixItemIngredient> CODEC = RarityRegistry.INSTANCE.holderCodec().fieldOf("rarity").xmap(AffixItemIngredient::new, a -> a.rarity);
    public static final StreamCodec<ByteBuf, AffixItemIngredient> STREAM_CODEC = RarityRegistry.INSTANCE.holderStreamCodec().map(AffixItemIngredient::new, a -> a.rarity);
    public static final IngredientType<AffixItemIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

    protected final DynamicHolder<LootRarity> rarity;

    public AffixItemIngredient(DynamicHolder<LootRarity> rarity) {
        this.rarity = rarity;
    }

    @Override
    public boolean test(ItemStack stack) {
        var rarity = AffixHelper.getRarity(stack);
        var affixes = AffixHelper.getAffixes(stack);
        return affixes.size() > 0 && rarity.isBound() && rarity == this.rarity;
    }

    /**
     * Affixes could be on anything, so we have to return the universe here.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Stream<Holder<Item>> items() {
        // We need to filter out enabled features, otherwise the client will ignore our entire ingredient and then things like smithing tables won't work.
        var server = ServerLifecycleHooks.getCurrentServer();
        FeatureFlagSet flags = server == null ? FeatureFlags.VANILLA_SET : server.getWorldData().enabledFeatures();
        return (Stream<Holder<Item>>) (Object) BuiltInRegistries.ITEM.listElements().filter(i -> i.value().isEnabled(flags) && i.value() != Items.AIR);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    public LootRarity getRarity() {
        return this.rarity.get();
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }

    @Override
    public SlotDisplay display() {
        return new AffixItemSlotDisplay(this.rarity);
    }

}
