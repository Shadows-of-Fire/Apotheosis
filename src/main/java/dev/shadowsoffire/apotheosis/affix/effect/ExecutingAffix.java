package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.Map;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.mixin.LivingEntityInvoker;
import dev.shadowsoffire.apothic_attributes.ApothicAttributes;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class ExecutingAffix extends Affix {

    public static final Codec<ExecutingAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            LootCategory.SET_CODEC.fieldOf("categories").forGetter(a -> a.categories),
            LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, ExecutingAffix::new));

    protected final Set<LootCategory> categories;
    protected final Map<LootRarity, StepFunction> values;

    public ExecutingAffix(AffixDefinition def, Set<LootCategory> categories, Map<LootRarity, StepFunction> values) {
        super(def);
        this.categories = categories;
        this.values = values;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return this.categories.contains(cat) && this.values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return Component.translatable("affix." + this.id() + ".desc", fmt(100 * this.getTrueLevel(inst.getRarity(), inst.level())));
    }

    @Override
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        MutableComponent comp = this.getDescription(inst, ctx);

        Component minComp = Component.translatable("%s%%", fmt(100 * this.getTrueLevel(inst.getRarity(), 0)));
        Component maxComp = Component.translatable("%s%%", fmt(100 * this.getTrueLevel(inst.getRarity(), 1)));
        return comp.append(valueBounds(minComp, maxComp));
    }

    private float getTrueLevel(LootRarity rarity, float level) {
        return this.values.get(rarity).get(level);
    }

    @Override
    public void doPostAttack(AffixInstance inst, LivingEntity user, Entity target) {
        float threshold = this.getTrueLevel(inst.getRarity(), inst.level());
        if (ApothicAttributes.getLocalAtkStrength(user) >= 0.98 && target instanceof LivingEntity living && !living.level().isClientSide) {
            if (!living.isDeadOrDying() && living.getHealth() / living.getMaxHealth() < threshold) {
                DamageSource src = living.damageSources().source(Apoth.DamageTypes.EXECUTE, user);
                if (!((LivingEntityInvoker) living).callCheckTotemDeathProtection(src)) {
                    SoundEvent soundevent = ((LivingEntityInvoker) living).callGetDeathSound();
                    if (soundevent != null) {
                        living.playSound(soundevent, ((LivingEntityInvoker) living).callGetSoundVolume(), living.getVoicePitch());
                    }

                    living.setLastHurtByMob(user);
                    if (user instanceof Player p) {
                        living.setLastHurtByPlayer(p);
                    }
                    living.getCombatTracker().recordDamage(src, 99999);
                    living.setHealth(0);
                    living.die(src);
                }
            }
        }
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    @Override
    public boolean isLevelIndependent(AffixInstance inst) {
        return this.values.get(inst.getRarity()).isConstant();
    }

}
