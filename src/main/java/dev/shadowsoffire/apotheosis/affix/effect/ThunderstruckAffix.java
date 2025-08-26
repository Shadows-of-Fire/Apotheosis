package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.util.DamageSourceExtension;
import dev.shadowsoffire.apothic_attributes.ApothicAttributes;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

/**
 * Damage Chain
 */
public class ThunderstruckAffix extends Affix {

    public static final Codec<ThunderstruckAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            LootCategory.SET_CODEC.fieldOf("categories").forGetter(a -> a.categories),
            LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, ThunderstruckAffix::new));

    protected final Set<LootCategory> categories;
    protected final Map<LootRarity, StepFunction> values;

    public ThunderstruckAffix(AffixDefinition def, Set<LootCategory> categories, Map<LootRarity, StepFunction> values) {
        super(def);
        this.categories = categories;
        this.values = values;
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return Component.translatable("affix." + this.id() + ".desc", fmt(this.getTrueLevel(inst.getRarity(), inst.level())));
    }

    @Override
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        MutableComponent comp = this.getDescription(inst, ctx);
        LootRarity rarity = inst.getRarity();

        Component minComp = Component.literal(fmt(this.getTrueLevel(rarity, 0)));
        Component maxComp = Component.literal(fmt(this.getTrueLevel(rarity, 1)));
        return comp.append(valueBounds(minComp, maxComp));
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return this.categories.contains(cat) && this.values.containsKey(rarity);
    }

    @Override
    public void doPostAttack(AffixInstance inst, LivingEntity user, Entity target) {
        if (user.level().isClientSide) {
            return;
        }
        if (ApothicAttributes.getLocalAtkStrength(user) >= 0.98) {
            List<Entity> nearby = target.level().getEntities(target, new AABB(target.blockPosition()).inflate(6), CleavingAffix.cleavePredicate(user, target));
            for (Entity e : nearby) {
                DamageSource src = user.damageSources().mobAttack(user);
                ((DamageSourceExtension) src).addTag(DamageTypeTags.IS_LIGHTNING);
                ((DamageSourceExtension) src).addTag(DamageTypeTags.BYPASSES_ARMOR);
                e.hurt(src, this.getTrueLevel(inst.getRarity(), inst.level()));
            }
        }
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
