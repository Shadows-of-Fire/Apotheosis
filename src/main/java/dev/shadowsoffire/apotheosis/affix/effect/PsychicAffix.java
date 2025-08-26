package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.LootCategories;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

/**
 * When blocking an arrow, hurt the shooter.
 */
public class PsychicAffix extends Affix {

    public static final Codec<PsychicAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, PsychicAffix::new));

    protected final Map<LootRarity, StepFunction> values;

    public PsychicAffix(AffixDefinition def, Map<LootRarity, StepFunction> values) {
        super(def);
        this.values = values;
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return Component.translatable("affix." + this.id() + ".desc", fmt(100 * this.getTrueLevel(inst)));
    }

    @Override
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        MutableComponent comp = this.getDescription(inst, ctx);

        Component minComp = Component.translatable("%s%%", fmt(100 * this.getTrueLevel(inst.getRarity(), 0)));
        Component maxComp = Component.translatable("%s%%", fmt(100 * this.getTrueLevel(inst.getRarity(), 1)));
        return comp.append(valueBounds(minComp, maxComp));
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat == LootCategories.SHIELD && this.values.containsKey(rarity);
    }

    @Override
    public float onShieldBlock(AffixInstance inst, LivingEntity entity, DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof Projectile arrow) {
            Entity owner = arrow.getOwner();
            if (owner instanceof LivingEntity living) {
                living.hurt(entity.damageSources().source(Apoth.DamageTypes.PSYCHIC, entity), amount * this.getTrueLevel(inst));
            }
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

    private float getTrueLevel(AffixInstance inst) {
        return this.getTrueLevel(inst.getRarity(), inst.level());
    }

    private float getTrueLevel(LootRarity rarity, float level) {
        return this.values.get(rarity).get(level);
    }

}
