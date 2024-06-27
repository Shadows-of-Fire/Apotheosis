package dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.special;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("deprecation")
public class AllStatsBonus extends GemBonus {

    public static Codec<AllStatsBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            PlaceboCodecs.enumCodec(Operation.class).fieldOf("operation").forGetter(a -> a.operation),
            VALUES_CODEC.fieldOf("values").forGetter(a -> a.values),
            BuiltInRegistries.ATTRIBUTE.byNameCodec().listOf().fieldOf("attributes").forGetter(a -> a.attributes))
        .apply(inst, AllStatsBonus::new));

    protected final Operation operation;
    protected final Map<LootRarity, StepFunction> values;
    protected final List<Attribute> attributes;

    public AllStatsBonus(GemClass gemClass, Operation op, Map<LootRarity, StepFunction> values, List<Attribute> attributes) {
        super(Apotheosis.loc("all_stats"), gemClass);
        this.operation = op;
        this.values = values;
        this.attributes = attributes;
    }

    @Override
    public void addModifiers(ItemStack gem, LootRarity rarity, BiConsumer<Attribute, AttributeModifier> map) {
        UUID id = GemItem.getUUIDs(gem).get(0);
        for (Attribute attr : this.attributes) {
            var modif = new AttributeModifier(id, "apoth.gem_modifier.all_stats_buff", this.values.get(rarity).min(), this.operation);
            map.accept(attr, modif);
        }
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        StepFunction value = this.values.get(rarity);
        return Component.translatable("bonus." + this.getId() + ".desc", Affix.fmt(value.get(0) * 100)).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public AllStatsBonus validate() {
        Preconditions.checkNotNull(this.operation, "Invalid AllStatsBonus with null operation");
        Preconditions.checkNotNull(this.values, "Invalid AllStatsBonus with null values");
        return this;
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return this.values.containsKey(rarity);
    }

    @Override
    public int getNumberOfUUIDs() {
        return 1;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

}
