package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class SpectralShotAffix extends Affix {

    public static final Codec<SpectralShotAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, SpectralShotAffix::new));

    protected final Map<LootRarity, StepFunction> values;

    public SpectralShotAffix(AffixDefinition def, Map<LootRarity, StepFunction> values) {
        super(def);
        this.values = values;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat.isRanged() && this.values.containsKey(rarity);
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

    @Override
    public void onProjectileFired(AffixInstance inst, LivingEntity user, Projectile proj) {
        if (user.level().random.nextFloat() <= this.getTrueLevel(inst.getRarity(), inst.level())) {
            if (!user.level().isClientSide && proj instanceof AbstractArrow arrow) {
                ArrowItem arrowitem = (ArrowItem) Items.SPECTRAL_ARROW;
                AbstractArrow spectralArrow = arrowitem.createArrow(user.level(), Items.SPECTRAL_ARROW.getDefaultInstance(), user, inst.stack());
                spectralArrow.shoot(user.getXRot(), user.getYRot(), 0.0F, 2.0F, 1.0F);
                this.cloneMotion(arrow, spectralArrow);
                spectralArrow.setCritArrow(arrow.isCritArrow());
                spectralArrow.setBaseDamage(arrow.getBaseDamage());
                spectralArrow.setRemainingFireTicks(arrow.getRemainingFireTicks());
                spectralArrow.pickup = Pickup.CREATIVE_ONLY;
                spectralArrow.getPersistentData().putBoolean("apoth.generated", true);
                arrow.level().addFreshEntity(spectralArrow);
            }
        }
    }

    private void cloneMotion(AbstractArrow src, AbstractArrow dest) {
        dest.setDeltaMovement(src.getDeltaMovement().scale(1));
        dest.setYRot(src.getYRot());
        dest.setXRot(src.getXRot());
        dest.yRotO = dest.yRotO;
        dest.xRotO = dest.xRotO;
    }

    private float getTrueLevel(LootRarity rarity, float level) {
        return this.values.get(rarity).get(level);
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
