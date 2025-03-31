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
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class LeechBlockBonus extends GemBonus {

    public static Codec<LeechBlockBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Purity.mapCodec(Data.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, LeechBlockBonus::new));

    protected final Map<Purity, Data> values;

    public LeechBlockBonus(Map<Purity, Data> values) {
        super(new GemClass(LootCategories.SHIELD));
        this.values = values;
    }

    @Override
    public float onShieldBlock(GemInstance inst, LivingEntity entity, DamageSource source, float amount) {
        Data d = this.values.get(inst.purity());
        if (amount <= 2 || Affix.isOnCooldown(makeUniqueId(inst), d.cooldown, entity)) {
            return amount;
        }
        entity.heal(amount * d.healFactor);
        Affix.startCooldown(makeUniqueId(inst), entity);
        return amount;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    @Override
    public Component getSocketBonusTooltip(GemView inst, AttributeTooltipContext ctx) {
        Data d = this.values.get(inst.purity());
        Component cooldown = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(d.cooldown, ctx.tickRate()));
        return Component.translatable("bonus." + this.getTypeKey() + ".desc", Affix.fmt(d.healFactor * 100), cooldown).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public boolean supports(Purity purity) {
        return this.values.containsKey(purity);
    }

    public static record Data(float healFactor, int cooldown) {

        public static final Codec<Data> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.FLOAT.fieldOf("heal_factor").forGetter(Data::healFactor),
                Codec.INT.fieldOf("cooldown").forGetter(Data::cooldown))
            .apply(inst, Data::new));

    }
}
