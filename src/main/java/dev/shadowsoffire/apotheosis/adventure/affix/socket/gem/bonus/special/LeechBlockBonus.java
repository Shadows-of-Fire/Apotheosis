package dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.bonus.special;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class LeechBlockBonus extends GemBonus {

    public static Codec<LeechBlockBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            LootRarity.mapCodec(Data.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, LeechBlockBonus::new));

    protected final Map<LootRarity, Data> values;

    public LeechBlockBonus(Map<LootRarity, Data> values) {
        super(Apotheosis.loc("leech_block"), new GemClass("shield", ImmutableSet.of(LootCategory.SHIELD)));
        this.values = values;
    }

    @Override
    public float onShieldBlock(ItemStack gem, LootRarity rarity, LivingEntity entity, DamageSource source, float amount) {
        Data d = this.values.get(rarity);
        if (amount <= 2 || Affix.isOnCooldown(this.getCooldownId(gem), d.cooldown, entity)) return amount;
        entity.heal(amount * d.healFactor);
        Affix.startCooldown(this.getCooldownId(gem), entity);
        return amount;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        Data d = this.values.get(rarity);
        Component cooldown = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(d.cooldown));
        return Component.translatable("bonus." + this.getId() + ".desc", Affix.fmt(d.healFactor * 100), cooldown).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public LeechBlockBonus validate() {
        Preconditions.checkNotNull(this.values);
        this.values.forEach((k, v) -> {
            Preconditions.checkNotNull(k);
            Preconditions.checkNotNull(v);
        });
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

    static record Data(float healFactor, int cooldown) {

        public static final Codec<Data> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.FLOAT.fieldOf("heal_factor").forGetter(Data::healFactor),
                Codec.INT.fieldOf("cooldown").forGetter(Data::cooldown))
            .apply(inst, Data::new));

    }
}
