package dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemClass;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class DurabilityBonus extends GemBonus {

    public static Codec<DurabilityBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            VALUES_CODEC.fieldOf("values").forGetter(a -> a.values))
        .apply(inst, DurabilityBonus::new));

    protected final Map<LootRarity, StepFunction> values;

    public DurabilityBonus(GemClass gemClass, Map<LootRarity, StepFunction> values) {
        super(Apotheosis.loc("durability"), gemClass);
        this.values = values;
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        float level = this.values.get(rarity).get(0);
        return Component.translatable("bonus." + this.getId() + ".desc", Affix.fmt(100 * level)).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public float getDurabilityBonusPercentage(ItemStack gem, LootRarity rarity, ServerPlayer user) {
        return this.values.get(rarity).min();
    }

    @Override
    public GemBonus validate() {
        Preconditions.checkNotNull(this.values, "Invalid AttributeBonus with null values");
        return this;
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return this.values.containsKey(rarity);
    }

    @Override
    public int getNumberOfUUIDs() {
        return 0;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

}
