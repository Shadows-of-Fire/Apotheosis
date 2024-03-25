package dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.bonus;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.PotionAffix.Target;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.registries.ForgeRegistries;

public class PotionBonus extends GemBonus {

    public static final Codec<PotionBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("mob_effect").forGetter(a -> a.effect),
            Target.CODEC.fieldOf("target").forGetter(a -> a.target),
            LootRarity.mapCodec(EffectData.CODEC).fieldOf("values").forGetter(a -> a.values),
            PlaceboCodecs.nullableField(Codec.BOOL, "stack_on_reapply", false).forGetter(a -> a.stackOnReapply))
        .apply(inst, PotionBonus::new));

    protected final MobEffect effect;
    protected final Target target;
    protected final Map<LootRarity, EffectData> values;
    protected final boolean stackOnReapply;

    public PotionBonus(GemClass gemClass, MobEffect effect, Target target, Map<LootRarity, EffectData> values, boolean stackOnReapply) {
        super(Apotheosis.loc("mob_effect"), gemClass);
        this.effect = effect;
        this.target = target;
        this.values = values;
        this.stackOnReapply = stackOnReapply;
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        MobEffectInstance inst = this.values.get(rarity).build(this.effect);
        MutableComponent comp = this.target.toComponent(toComponent(inst)).withStyle(ChatFormatting.YELLOW);
        int cooldown = this.getCooldown(rarity);
        if (cooldown != 0) {
            Component cd = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(cooldown));
            comp = comp.append(" ").append(cd);
        }
        if (this.stackOnReapply) {
            comp = comp.append(" ").append(Component.translatable("affix.apotheosis.stacking"));
        }
        return comp;
    }

    @Override
    public int getNumberOfUUIDs() {
        return 0;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return this.values.containsKey(rarity);
    }

    @Override
    public void doPostHurt(ItemStack gem, LootRarity rarity, LivingEntity user, Entity attacker) {
        if (this.target == Target.HURT_SELF) this.applyEffect(gem, user, rarity);
        else if (this.target == Target.HURT_ATTACKER) {
            if (attacker instanceof LivingEntity tLiving) {
                this.applyEffect(gem, tLiving, rarity);
            }
        }
    }

    @Override
    public void doPostAttack(ItemStack gem, LootRarity rarity, LivingEntity user, Entity target) {
        if (this.target == Target.ATTACK_SELF) this.applyEffect(gem, user, rarity);
        else if (this.target == Target.ATTACK_TARGET) {
            if (target instanceof LivingEntity tLiving) {
                this.applyEffect(gem, tLiving, rarity);
            }
        }
    }

    @Override
    public void onBlockBreak(ItemStack gem, LootRarity rarity, Player player, LevelAccessor world, BlockPos pos, BlockState state) {
        if (this.target == Target.BREAK_SELF) {
            this.applyEffect(gem, player, rarity);
        }
    }

    @Override
    public void onArrowImpact(ItemStack gemStack, LootRarity rarity, AbstractArrow arrow, HitResult res, HitResult.Type type) {
        if (this.target == Target.ARROW_SELF) {
            if (arrow.getOwner() instanceof LivingEntity owner) {
                this.applyEffect(gemStack, owner, rarity);
            }
        }
        else if (this.target == Target.ARROW_TARGET) {
            if (type == Type.ENTITY && ((EntityHitResult) res).getEntity() instanceof LivingEntity target) {
                this.applyEffect(gemStack, target, rarity);
            }
        }
    }

    @Override
    public float onShieldBlock(ItemStack gem, LootRarity rarity, LivingEntity entity, DamageSource source, float amount) {
        if (this.target == Target.BLOCK_SELF) {
            this.applyEffect(gem, entity, rarity);
        }
        else if (this.target == Target.BLOCK_ATTACKER && source.getDirectEntity() instanceof LivingEntity target) {
            this.applyEffect(gem, target, rarity);
        }
        return amount;
    }

    protected int getCooldown(LootRarity rarity) {
        EffectData data = this.values.get(rarity);
        return data.cooldown;
    }

    private void applyEffect(ItemStack gemStack, LivingEntity target, LootRarity rarity) {
        int cooldown = this.getCooldown(rarity);
        if (cooldown != 0 && Affix.isOnCooldown(this.getCooldownId(gemStack), cooldown, target)) return;
        EffectData data = this.values.get(rarity);
        var inst = target.getEffect(this.effect);
        if (this.stackOnReapply && inst != null) {
            if (inst != null) {
                var newInst = new MobEffectInstance(this.effect, Math.max(inst.getDuration(), data.duration), inst.getAmplifier() + 1 + data.amplifier);
                target.addEffect(newInst);
            }
        }
        else {
            target.addEffect(data.build(this.effect));
        }
        Affix.startCooldown(this.getCooldownId(gemStack), target);
    }

    public static Component toComponent(MobEffectInstance inst) {
        MutableComponent mutablecomponent = Component.translatable(inst.getDescriptionId());
        MobEffect mobeffect = inst.getEffect();

        if (inst.getAmplifier() > 0) {
            mutablecomponent = Component.translatable("potion.withAmplifier", mutablecomponent, Component.translatable("potion.potency." + inst.getAmplifier()));
        }

        if (inst.getDuration() > 20) {
            mutablecomponent = Component.translatable("potion.withDuration", mutablecomponent, MobEffectUtil.formatDuration(inst, 1));
        }

        return mutablecomponent.withStyle(mobeffect.getCategory().getTooltipFormatting());
    }

    public static record EffectData(int duration, int amplifier, int cooldown) {

        private static Codec<EffectData> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.INT.fieldOf("duration").forGetter(EffectData::duration),
                Codec.INT.fieldOf("amplifier").forGetter(EffectData::amplifier),
                PlaceboCodecs.nullableField(Codec.INT, "cooldown", 0).forGetter(EffectData::cooldown))
            .apply(inst, EffectData::new));

        public MobEffectInstance build(MobEffect effect) {
            return new MobEffectInstance(effect, this.duration, this.amplifier);
        }
    }

    @Override
    public PotionBonus validate() {
        Preconditions.checkNotNull(this.effect, "Null mob effect");
        Preconditions.checkNotNull(this.target, "Null target");
        Preconditions.checkNotNull(this.values, "Null values map");
        return this;
    }

}
