package dev.shadowsoffire.apotheosis.adventure.affix.effect;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.mixin.LivingEntityInvoker;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ExecutingAffix extends Affix {

    public static final Codec<ExecutingAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            GemBonus.VALUES_CODEC.fieldOf("values").forGetter(a -> a.values))
        .apply(inst, ExecutingAffix::new));

    protected final Map<LootRarity, StepFunction> values;

    public ExecutingAffix(Map<LootRarity, StepFunction> values) {
        super(AffixType.ABILITY);
        this.values = values;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat == LootCategory.HEAVY_WEAPON && this.values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        return Component.translatable("affix." + this.getId() + ".desc", fmt(100 * this.getTrueLevel(rarity, level)));
    }

    @Override
    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float level) {
        MutableComponent comp = this.getDescription(stack, rarity, level);

        Component minComp = Component.translatable("%s%%", fmt(100 * this.getTrueLevel(rarity, 0)));
        Component maxComp = Component.translatable("%s%%", fmt(100 * this.getTrueLevel(rarity, 1)));
        return comp.append(valueBounds(minComp, maxComp));
    }

    private float getTrueLevel(LootRarity rarity, float level) {
        return this.values.get(rarity).get(level);
    }

    @Override
    public void doPostAttack(ItemStack stack, LootRarity rarity, float level, LivingEntity user, Entity target) {
        float threshold = this.getTrueLevel(rarity, level);
        if (Apotheosis.getLocalAtkStrength(user) >= 0.98 && target instanceof LivingEntity living && !living.level().isClientSide) {
            if (living.getHealth() / living.getMaxHealth() < threshold) {
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

}
