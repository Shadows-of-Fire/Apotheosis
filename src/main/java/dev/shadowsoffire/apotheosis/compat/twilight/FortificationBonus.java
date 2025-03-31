package dev.shadowsoffire.apotheosis.compat.twilight;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
import twilightforest.init.TFDataAttachments;
import twilightforest.init.TFSounds;

public class FortificationBonus extends GemBonus {

    public static final Codec<FortificationBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            Purity.mapCodec(Data.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, FortificationBonus::new));

    protected final Map<Purity, Data> values;

    public FortificationBonus(GemClass gemClass, Map<Purity, Data> values) {
        super(gemClass);
        this.values = values;
    }

    @Override
    public void doPostHurt(GemInstance inst, LivingEntity user, DamageSource source) {
        Data d = this.values.get(inst.purity());
        if (Affix.isOnCooldown(makeUniqueId(inst), d.cooldown, user)) {
            return;
        }
        if (user.hasData(TFDataAttachments.FORTIFICATION_SHIELDS) && user.getRandom().nextFloat() <= d.chance) {
            user.getData(TFDataAttachments.FORTIFICATION_SHIELDS).setShields(user, 5, true);
            user.playSound(TFSounds.SHIELD_ADD.get(), 1.0F, (user.getRandom().nextFloat() - user.getRandom().nextFloat()) * 0.2F + 1.0F);
            Affix.startCooldown(makeUniqueId(inst), user);
        }
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    @Override
    public boolean supports(Purity purity) {
        return this.values.containsKey(purity);
    }

    @Override
    public Component getSocketBonusTooltip(GemView inst, AttributeTooltipContext ctx) {
        Data d = this.values.get(inst.purity());
        Component cooldown = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(d.cooldown, ctx.tickRate()));
        return Component.translatable("bonus." + this.getTypeKey() + ".desc", Affix.fmt(d.chance * 100), cooldown).withStyle(ChatFormatting.YELLOW);
    }

    protected static record Data(float chance, int cooldown) {

        public static final Codec<Data> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.FLOAT.fieldOf("chance").forGetter(Data::chance),
                Codec.INT.fieldOf("cooldown").forGetter(Data::cooldown))
            .apply(inst, Data::new));

    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends GemBonus.Builder {

        protected final Map<Purity, Data> values = new HashMap<>();

        public Builder value(Purity purity, float chance, int cooldown) {
            this.values.put(purity, new Data(chance, cooldown));
            return this;
        }

        @Override
        public FortificationBonus build(GemClass gClass) {
            return new FortificationBonus(gClass, values);
        }

    }

}
