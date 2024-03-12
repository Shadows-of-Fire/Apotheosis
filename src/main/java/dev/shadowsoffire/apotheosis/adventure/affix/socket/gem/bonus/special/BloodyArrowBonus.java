package dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.bonus.special;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;

public class BloodyArrowBonus extends GemBonus {

    public static Codec<BloodyArrowBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            LootRarity.mapCodec(Data.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, BloodyArrowBonus::new));

    protected final Map<LootRarity, Data> values;

    public BloodyArrowBonus(Map<LootRarity, Data> values) {
        super(Apotheosis.loc("bloody_arrow"), new GemClass("ranged_weapon", ImmutableSet.of(LootCategory.BOW, LootCategory.CROSSBOW)));
        this.values = values;
    }

    @Override
    public void onArrowFired(ItemStack gem, LootRarity rarity, LivingEntity user, AbstractArrow arrow) {
        Data d = this.values.get(rarity);
        if (Affix.isOnCooldown(this.getCooldownId(gem), d.cooldown, user)) return;
        user.hurt(user.damageSources().source(Apoth.DamageTypes.CORRUPTED), user.getMaxHealth() * d.healthCost);
        arrow.setBaseDamage(arrow.getBaseDamage() * d.dmgMultiplier);
        Affix.startCooldown(this.getCooldownId(gem), user);
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        Data d = this.values.get(rarity);
        Component cooldown = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(d.cooldown));
        return Component.translatable("bonus." + this.getId() + ".desc", Affix.fmt(d.healthCost * 100), Affix.fmt(100 * d.dmgMultiplier), cooldown).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public BloodyArrowBonus validate() {
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

    static record Data(float healthCost, float dmgMultiplier, int cooldown) {

        public static final Codec<Data> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.FLOAT.fieldOf("health_cost").forGetter(Data::healthCost),
                Codec.FLOAT.fieldOf("damage_mult").forGetter(Data::dmgMultiplier),
                Codec.INT.fieldOf("cooldown").forGetter(Data::cooldown))
            .apply(inst, Data::new));

    }
}
