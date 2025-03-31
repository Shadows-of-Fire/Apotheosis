package dev.shadowsoffire.apotheosis.compat.twilight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Attachments;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.attachments.BonusLootTables;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemView;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.color.GradientColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import twilightforest.entity.monster.Redcap;

public class TreasureGoblinBonus extends GemBonus {

    public static final Codec<TreasureGoblinBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            Purity.mapCodec(Data.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, TreasureGoblinBonus::new));

    protected final Map<Purity, Data> values;

    public TreasureGoblinBonus(GemClass gemClass, Map<Purity, Data> values) {
        super(gemClass);
        this.values = values;
    }

    @Override
    public void doPostAttack(GemInstance inst, LivingEntity user, Entity target) {
        Data d = this.values.get(inst.purity());
        if (Affix.isOnCooldown(makeUniqueId(inst), d.cooldown, user)) {
            return;
        }
        if (user.getRandom().nextFloat() <= d.chance) {
            Redcap goblin = AdventureTwilightCompat.REDCAP.get().create(user.level());
            goblin.setData(Attachments.BONUS_LOOT_TABLES, new BonusLootTables(List.of(Apoth.LootTables.TREASURE_GOBLIN)));
            goblin.getPersistentData().putBoolean("apoth.treasure_goblin", true);
            goblin.setCustomName(Component.translatable("name.apotheosis.treasure_goblin").withStyle(s -> s.withColor(GradientColor.RAINBOW)));
            goblin.setCustomNameVisible(true);
            goblin.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier(Apotheosis.loc("very_fast"), 0.2, Operation.ADD_VALUE));
            goblin.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(Apotheosis.loc("healmth"), 60, Operation.ADD_VALUE));
            goblin.setHealth(goblin.getMaxHealth());
            for (int i = 0; i < 8; i++) {
                int x = Mth.nextInt(goblin.getRandom(), -5, 5);
                int y = Mth.nextInt(goblin.getRandom(), -1, 1);
                int z = Mth.nextInt(goblin.getRandom(), -5, 5);
                goblin.setPos(target.position().add(x, y, z));
                if (user.level().noCollision(goblin)) {
                    break;
                }
                if (i == 7) {
                    goblin.setPos(target.position());
                }
            }
            goblin.addEffect(new MobEffectInstance(MobEffects.GLOWING, 96000, 0, true, false));
            user.level().addFreshEntity(goblin);
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
        public TreasureGoblinBonus build(GemClass gClass) {
            return new TreasureGoblinBonus(gClass, values);
        }

    }

}
