package dev.shadowsoffire.apotheosis.socket.gem.bonus.special;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.LootCategories;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemView;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apothic_enchanting.Ench;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class BloodyArrowBonus extends GemBonus {

    public static Codec<BloodyArrowBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Purity.mapCodec(Data.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, BloodyArrowBonus::new));

    protected final Map<Purity, Data> values;

    public BloodyArrowBonus(Map<Purity, Data> values) {
        super(new GemClass(LootCategories.BOW));
        this.values = values;
    }

    @Override
    public void onProjectileFired(GemInstance inst, LivingEntity user, Projectile proj) {
        if (proj instanceof AbstractArrow arrow) {
            Data d = this.values.get(inst.purity());
            if (Affix.isOnCooldown(makeUniqueId(inst), d.cooldown, user)) {
                return;
            }
            user.hurt(user.damageSources().source(Ench.DamageTypes.CORRUPTED), user.getMaxHealth() * d.healthCost);
            arrow.setBaseDamage(arrow.getBaseDamage() * d.dmgMultiplier);
            Affix.startCooldown(makeUniqueId(inst), user);
        }
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    @Override
    public Component getSocketBonusTooltip(GemView inst, AttributeTooltipContext ctx) {
        Data d = this.values.get(inst.purity());
        Component cooldown = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(d.cooldown, ctx.tickRate()));
        return Component.translatable("bonus." + this.getTypeKey() + ".desc", Affix.fmt(d.healthCost * 100), Affix.fmt(100 * d.dmgMultiplier), cooldown).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public boolean supports(Purity purity) {
        return this.values.containsKey(purity);
    }

    public static record Data(float healthCost, float dmgMultiplier, int cooldown) {

        public static final Codec<Data> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.FLOAT.fieldOf("health_cost").forGetter(Data::healthCost),
                Codec.FLOAT.fieldOf("damage_mult").forGetter(Data::dmgMultiplier),
                Codec.INT.fieldOf("cooldown").forGetter(Data::cooldown))
            .apply(inst, Data::new));

    }
}
