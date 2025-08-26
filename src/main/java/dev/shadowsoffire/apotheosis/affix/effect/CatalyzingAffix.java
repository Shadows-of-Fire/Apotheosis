package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.LootCategories;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * When blocking an explosion, gain great power.
 */
public class CatalyzingAffix extends Affix {

    public static final Codec<CatalyzingAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, CatalyzingAffix::new));

    protected final Map<LootRarity, StepFunction> values;

    public CatalyzingAffix(AffixDefinition def, Map<LootRarity, StepFunction> values) {
        super(def);
        this.values = values;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat == LootCategories.SHIELD && this.values.containsKey(rarity);
    }

    @Override
    public float onShieldBlock(AffixInstance inst, LivingEntity entity, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            int time = this.values.get(inst.getRarity()).getInt(inst.level());
            int modifier = 1 + (int) (Math.log(amount) / Math.log(3));
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, time, modifier));
        }

        return super.onShieldBlock(inst, entity, source, amount);
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
