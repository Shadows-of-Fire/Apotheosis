package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import org.spongepowered.include.com.google.common.base.Preconditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixBuilder.ValuedAffixBuilder;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apothic_attributes.util.AttributesUtil;
import dev.shadowsoffire.placebo.util.StepFunction;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class DamageReductionAffix extends Affix {

    public static final Codec<DamageReductionAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            DamageType.CODEC.fieldOf("damage_type").forGetter(a -> a.type),
            LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
            LootCategory.SET_CODEC.fieldOf("categories").forGetter(a -> a.categories))
        .apply(inst, DamageReductionAffix::new));

    protected final DamageType type;
    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> categories;

    public DamageReductionAffix(AffixDefinition def, DamageType type, Map<LootRarity, StepFunction> values, Set<LootCategory> categories) {
        super(def);
        this.type = type;
        this.values = values;
        this.categories = categories;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return !cat.isNone() && (this.categories.isEmpty() || this.categories.contains(cat)) && this.values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return Component.translatable("affix.apotheosis:damage_reduction.desc", Component.translatable("misc.apotheosis." + this.type.id), fmt(100 * this.getTrueLevel(inst.getRarity(), inst.level())));
    }

    @Override
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        MutableComponent comp = this.getDescription(inst, ctx);

        Component minComp = Component.translatable("%s%%", fmt(100 * this.getTrueLevel(inst.getRarity(), 0)));
        Component maxComp = Component.translatable("%s%%", fmt(100 * this.getTrueLevel(inst.getRarity(), 1)));
        return comp.append(valueBounds(minComp, maxComp));
    }

    @Override
    public float onHurt(AffixInstance inst, DamageSource src, LivingEntity ent, float amount) {
        if (!src.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !src.is(DamageTypeTags.BYPASSES_ENCHANTMENTS) && this.type.test(src)) {
            return amount * (1 - this.getTrueLevel(inst.getRarity(), inst.level()));
        }
        return super.onHurt(inst, src, ent, amount);
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

    public static enum DamageType implements Predicate<DamageSource>, StringRepresentable {
        PHYSICAL("physical", AttributesUtil::isPhysicalDamage),
        MAGIC("magic", d -> d.is(Tags.DamageTypes.IS_MAGIC)),
        FIRE("fire", d -> d.is(DamageTypeTags.IS_FIRE)),
        FALL("fall", d -> d.is(DamageTypeTags.IS_FALL)),
        EXPLOSION("explosion", d -> d.is(DamageTypeTags.IS_EXPLOSION)),
        PROJECTILE("projectile", d -> d.is(DamageTypeTags.IS_PROJECTILE)),
        LIGHTNING("lightning", d -> d.is(DamageTypeTags.IS_LIGHTNING));

        public static final IntFunction<DamageType> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
        public static final Codec<DamageType> CODEC = StringRepresentable.fromValues(DamageType::values);
        public static final StreamCodec<ByteBuf, DamageType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);

        private final String id;
        private final Predicate<DamageSource> predicate;

        private DamageType(String id, Predicate<DamageSource> predicate) {
            this.id = id;
            this.predicate = predicate;
        }

        @Override
        public boolean test(DamageSource t) {
            return this.predicate.test(t);
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }
    }

    public static class Builder extends ValuedAffixBuilder<Builder> {
        protected final DamageType type;
        protected final Set<LootCategory> categories = new LinkedHashSet<>();

        public Builder(DamageType type) {
            this.type = type;
        }

        public Builder categories(LootCategory... cats) {
            for (LootCategory cat : cats) {
                this.categories.add(cat);
            }
            return this;
        }

        public DamageReductionAffix build() {
            Preconditions.checkArgument(!this.values.isEmpty());
            return new DamageReductionAffix(this.definition, this.type, this.values, this.categories);
        }
    }

}
