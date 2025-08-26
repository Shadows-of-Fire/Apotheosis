package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class EnlightenedAffix extends Affix {

    public static final Codec<EnlightenedAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, EnlightenedAffix::new));

    protected final Map<LootRarity, StepFunction> values;

    public EnlightenedAffix(AffixDefinition def, Map<LootRarity, StepFunction> values) {
        super(def);
        this.values = values;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat.isBreaker() && this.values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return Component.translatable("affix." + this.id() + ".desc", this.getTrueLevel(inst.getRarity(), inst.level()));
    }

    @Override
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        MutableComponent comp = this.getDescription(inst, ctx);

        Component minComp = Component.literal(fmt(this.getTrueLevel(inst.getRarity(), 0)));
        Component maxComp = Component.literal(fmt(this.getTrueLevel(inst.getRarity(), 1)));
        return comp.append(valueBounds(minComp, maxComp));
    }

    @Override
    public InteractionResult onItemUse(AffixInstance inst, UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (AdventureConfig.torchItem.useOn(ctx).consumesAction()) {
            if (ctx.getItemInHand().isEmpty()) {
                ctx.getItemInHand().grow(1);
            }

            int cost = this.getTrueLevel(inst.getRarity(), inst.level());
            player.getItemInHand(ctx.getHand()).hurtAndBreak(cost, player, LivingEntity.getSlotForHand(ctx.getHand()));
            return InteractionResult.SUCCESS;
        }
        return super.onItemUse(inst, ctx);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    @Override
    public boolean isLevelIndependent(AffixInstance inst) {
        return this.values.get(inst.getRarity()).isConstant();
    }

    protected int getTrueLevel(LootRarity rarity, float level) {
        return this.values.get(rarity).getInt(level);
    }

}
