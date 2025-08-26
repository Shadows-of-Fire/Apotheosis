package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.spongepowered.include.com.google.common.base.Preconditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixBuilder;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.mixin.LivingEntityInvoker;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class MobEffectAffix extends Affix {

    public static final Codec<MobEffectAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("mob_effect").forGetter(a -> a.effect),
            Target.CODEC.fieldOf("target").forGetter(a -> a.target),
            LootRarity.mapCodec(EffectData.CODEC).fieldOf("values").forGetter(a -> a.values),
            LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types),
            Codec.BOOL.optionalFieldOf("stack_on_reapply", false).forGetter(a -> a.stackOnReapply),
            Codec.intRange(1, 255).optionalFieldOf("stacking_limit", 255).forGetter(a -> a.stackingLimit))
        .apply(inst, MobEffectAffix::new));

    protected final Holder<MobEffect> effect;
    protected final Target target;
    protected final Map<LootRarity, EffectData> values;
    protected final Set<LootCategory> types;
    protected final boolean stackOnReapply;
    protected final int stackingLimit;

    public MobEffectAffix(AffixDefinition def, Holder<MobEffect> effect, Target target, Map<LootRarity, EffectData> values, Set<LootCategory> types, boolean stackOnReapply, int stackingLimit) {
        super(def);
        this.effect = effect;
        this.target = target;
        this.values = values;
        this.types = types;
        this.stackOnReapply = stackOnReapply;
        this.stackingLimit = stackingLimit;
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        MobEffectInstance effectInst = this.values.get(inst.getRarity()).build(this.effect, inst.level());
        MutableComponent comp = this.target.toComponent(toComponent(effectInst, ctx.tickRate()));
        int cooldown = this.getCooldown(inst.getRarity());
        if (cooldown != 0) {
            Component cd = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(cooldown, ctx.tickRate()));
            comp = comp.append(" ").append(cd);
        }
        if (this.stackOnReapply) {
            comp = comp.append(" ").append(Component.translatable("affix.apotheosis.stacking"));
        }
        return comp;
    }

    @Override
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        LootRarity rarity = inst.getRarity();
        MobEffectInstance effectInst = this.values.get(rarity).build(this.effect, inst.level());
        MutableComponent comp = this.target.toComponent(toComponent(effectInst, ctx.tickRate()));

        MobEffectInstance min = this.values.get(rarity).build(this.effect, 0);
        MobEffectInstance max = this.values.get(rarity).build(this.effect, 1);

        if (min.getAmplifier() != max.getAmplifier()) {
            // Vanilla ships potion.potency.0 as an empty string, so we have to fix that here
            Component minComp = min.getAmplifier() == 0 ? Component.literal("I") : Component.translatable("potion.potency." + min.getAmplifier());
            Component maxComp = Component.translatable("potion.potency." + max.getAmplifier());
            comp.append(valueBounds(minComp, maxComp));
        }

        if (!this.effect.value().isInstantenous() && min.getDuration() != max.getDuration()) {
            Component minComp = MobEffectUtil.formatDuration(min, 1, ctx.tickRate());
            Component maxComp = MobEffectUtil.formatDuration(max, 1, ctx.tickRate());
            comp.append(valueBounds(minComp, maxComp));
        }

        int cooldown = this.getCooldown(rarity);
        if (cooldown != 0) {
            Component cd = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(cooldown, ctx.tickRate()));
            comp = comp.append(" ").append(cd);
        }
        if (this.stackOnReapply) {
            comp = comp.append(" ").append(Component.translatable("affix.apotheosis.stacking"));
        }

        return comp;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return (this.types.isEmpty() || this.types.contains(cat)) && this.values.containsKey(rarity);
    }

    @Override
    public void doPostHurt(AffixInstance inst, LivingEntity user, DamageSource source) {
        if (this.target == Target.HURT_SELF) {
            this.applyEffect(user, inst.getRarity(), inst.level());
        }
        else if (this.target == Target.HURT_ATTACKER) {
            if (source.getEntity() instanceof LivingEntity tLiving) {
                this.applyEffect(tLiving, inst.getRarity(), inst.level());
            }
        }
    }

    @Override
    public void doPostAttack(AffixInstance inst, LivingEntity user, Entity target) {
        if (this.target == Target.ATTACK_SELF) {
            this.applyEffect(user, inst.getRarity(), inst.level());
        }
        else if (this.target == Target.ATTACK_TARGET) {
            if (target instanceof LivingEntity tLiving) {
                this.applyEffect(tLiving, inst.getRarity(), inst.level());
            }
        }
    }

    @Override
    public void onBlockBreak(AffixInstance inst, Player player, LevelAccessor world, BlockPos pos, BlockState state) {
        if (this.target == Target.BREAK_SELF) {
            this.applyEffect(player, inst.getRarity(), inst.level());
        }
    }

    @Override
    public void onProjectileImpact(float level, LootRarity rarity, Projectile proj, HitResult res, Type type) {
        if (type == Type.ENTITY && ((EntityHitResult) res).getEntity() instanceof LivingEntity target) {
            switch (this.target) {
                case ARROW_SELF -> {
                    if (proj instanceof AbstractArrow && proj.getOwner() instanceof LivingEntity owner) {
                        this.applyEffect(owner, rarity, level);
                    }
                }
                case ARROW_TARGET -> {
                    if (proj instanceof AbstractArrow) {
                        this.applyEffect(target, rarity, level);
                    }
                }
                case PROJECTILE_SELF -> {
                    if (proj.getOwner() instanceof LivingEntity owner) {
                        this.applyEffect(owner, rarity, level);
                    }
                }
                case PROJECTILE_TARGET -> {
                    this.applyEffect(target, rarity, level);
                }
                default -> {}
            }
        }
    }

    @Override
    public float onShieldBlock(AffixInstance inst, LivingEntity entity, DamageSource source, float amount) {
        if (this.target == Target.BLOCK_SELF) {
            this.applyEffect(entity, inst.getRarity(), inst.level());
        }
        else if (this.target == Target.BLOCK_ATTACKER && source.getDirectEntity() instanceof LivingEntity target) {
            this.applyEffect(target, inst.getRarity(), inst.level());
        }
        return amount;
    }

    protected int getCooldown(LootRarity rarity) {
        EffectData data = this.values.get(rarity);
        return data.cooldown;
    }

    private void applyEffect(LivingEntity target, LootRarity rarity, float level) {
        if (target.level().isClientSide()) {
            return;
        }

        int cooldown = this.getCooldown(rarity);
        if (cooldown != 0 && isOnCooldown(this.id(), cooldown, target)) {
            return;
        }
        EffectData data = this.values.get(rarity);
        MobEffectInstance inst = target.getEffect(this.effect);
        if (this.stackOnReapply && inst != null) {
            if (inst != null) {
                int duration = Math.max(inst.getDuration(), data.duration.getInt(level));
                int amp = Math.min(this.stackingLimit, inst.getAmplifier() + 1 + data.amplifier.getInt(level));
                var newInst = new MobEffectInstance(this.effect, duration, amp, inst.isAmbient(), inst.isVisible());
                inst.update(newInst);
                ((LivingEntityInvoker) target).callOnEffectUpdated(inst, true, null);
                inst.onEffectStarted(target);
            }
        }
        else {
            target.addEffect(data.build(this.effect, level));
        }
        startCooldown(this.id(), target);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    @Override
    public boolean isLevelIndependent(AffixInstance inst) {
        return this.values.get(inst.getRarity()).isConstant();
    }

    public static Component toComponent(MobEffectInstance inst, float tickRate) {
        MutableComponent mutablecomponent = Component.translatable(inst.getDescriptionId());
        Holder<MobEffect> mobeffect = inst.getEffect();

        if (inst.getAmplifier() > 0) {
            mutablecomponent = Component.translatable("potion.withAmplifier", mutablecomponent, Component.translatable("potion.potency." + inst.getAmplifier()));
        }

        if (inst.getDuration() > 20) {
            mutablecomponent = Component.translatable("potion.withDuration", mutablecomponent, MobEffectUtil.formatDuration(inst, 1, tickRate));
        }

        return mutablecomponent.withStyle(mobeffect.value().getCategory().getTooltipFormatting());
    }

    public static record EffectData(StepFunction duration, StepFunction amplifier, int cooldown) {

        private static Codec<EffectData> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                StepFunction.CODEC.fieldOf("duration").forGetter(EffectData::duration),
                StepFunction.CODEC.fieldOf("amplifier").forGetter(EffectData::amplifier),
                Codec.INT.optionalFieldOf("cooldown", 0).forGetter(EffectData::cooldown))
            .apply(inst, EffectData::new));

        public MobEffectInstance build(Holder<MobEffect> effect, float level) {
            return new MobEffectInstance(effect, this.duration.getInt(level), this.amplifier.getInt(level));
        }

        public boolean isConstant() {
            return this.duration.isConstant() && this.amplifier.isConstant();
        }
    }

    /**
     * This enum is used to specify when a potion is applied.
     * The naming scheme is "<event>_<target>", so attack_self applies to yourself when you attack.
     */
    public static enum Target {
        ATTACK_SELF("attack_self"),
        ATTACK_TARGET("attack_target"),
        HURT_SELF("hurt_self"),
        HURT_ATTACKER("hurt_attacker"),
        BREAK_SELF("break_self"),
        ARROW_SELF("arrow_self"),
        ARROW_TARGET("arrow_target"),
        BLOCK_SELF("block_self"),
        BLOCK_ATTACKER("block_attacker"),
        PROJECTILE_SELF("projectile_self"),
        PROJECTILE_TARGET("projectile_target");

        public static final Codec<Target> CODEC = PlaceboCodecs.enumCodec(Target.class);

        private final String id;

        Target(String id) {
            this.id = id;
        }

        public MutableComponent toComponent(Object... args) {
            return Component.translatable("affix.apotheosis.target." + this.id, args);
        }
    }

    public static class Builder extends AffixBuilder<Builder> {
        protected final Holder<MobEffect> effect;
        protected final Target target;
        protected final Map<LootRarity, EffectData> values = new LinkedHashMap<>();
        protected final Set<LootCategory> categories = new LinkedHashSet<>();
        protected boolean stacking = false;
        private int limit = 255;

        public Builder(Holder<MobEffect> effect, Target target) {
            this.effect = effect;
            this.target = target;
        }

        public Builder categories(LootCategory... cats) {
            for (LootCategory cat : cats) {
                this.categories.add(cat);
            }
            return this;
        }

        public Builder value(LootRarity rarity, int duration, int amplifier, int cooldown) {
            return this.value(rarity, StepFunction.constant(duration), StepFunction.constant(amplifier), cooldown);
        }

        public Builder value(LootRarity rarity, int minDuration, int maxDuration, int amplifier, int cooldown) {
            return this.value(rarity, minDuration, maxDuration, StepFunction.constant(amplifier), cooldown);
        }

        public Builder value(LootRarity rarity, int minDuration, int maxDuration, StepFunction amplifier, int cooldown) {
            return this.value(rarity, StepFunction.fromBounds(minDuration, maxDuration, 20), amplifier, cooldown);
        }

        public Builder value(LootRarity rarity, StepFunction duration, StepFunction amplifier, int cooldown) {
            return this.value(rarity, new EffectData(duration, amplifier, cooldown));
        }

        public Builder value(LootRarity rarity, EffectData value) {
            this.values.put(rarity, value);
            return this;
        }

        public Builder stacking() {
            this.stacking = true;
            return this;
        }

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public MobEffectAffix build() {
            Preconditions.checkArgument(!this.values.isEmpty());
            return new MobEffectAffix(this.definition, this.effect, this.target, this.values, this.categories, this.stacking, this.limit);
        }
    }

}
