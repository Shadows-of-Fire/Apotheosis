package dev.shadowsoffire.apotheosis.util;

import java.util.Arrays;
import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

public record AffixItemSlotDisplay(DynamicHolder<LootRarity> rarity) implements SlotDisplay {

    public static final MapCodec<AffixItemSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(RarityRegistry.INSTANCE.holderCodec().fieldOf("rarity").forGetter(AffixItemSlotDisplay::rarity))
            .apply(i, AffixItemSlotDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, AffixItemSlotDisplay> STREAM_CODEC = RarityRegistry.INSTANCE.holderStreamCodec()
        .map(AffixItemSlotDisplay::new, AffixItemSlotDisplay::rarity)
        .cast();
    public static final SlotDisplay.Type<AffixItemSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        if (!(factory instanceof DisplayContentsFactory.ForStacks<T> stacks) || !this.rarity.isBound()) {
            return Stream.empty();
        }
        LootRarity r = this.rarity.get();
        RandomSource src = new LegacyRandomSource(0);
        return Arrays.asList(Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS)
            .stream()
            .map(ItemStack::new)
            .map(stack -> {
                LootController.createLootItem(stack, r, GenContext.dummy(src));
                AffixHelper.setName(stack, Component.translatable("text.apotheosis.any_x_item", r.toComponent(), "").withStyle(Style.EMPTY.withColor(r.color()).withItalic(false)));
                return stack;
            })
            .map(stacks::forStack);
    }

    @Override
    public SlotDisplay.Type<AffixItemSlotDisplay> type() {
        return TYPE;
    }

}
