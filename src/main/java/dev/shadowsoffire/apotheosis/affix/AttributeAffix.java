package dev.shadowsoffire.apotheosis.affix;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.AffixBuilder.ValuedAffixBuilder;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apothic_attributes.modifiers.StackAttributeModifiersEvent;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

/**
 * Helper class for affixes that modify attributes, as the apply method is the same for most of those.
 */
public class AttributeAffix extends Affix implements AttributeProvidingAffix {

    public static final Codec<AttributeAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(a -> a.attribute),
            PlaceboCodecs.enumCodec(Operation.class).fieldOf("operation").forGetter(a -> a.operation),
            LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
            LootCategory.SET_CODEC.fieldOf("categories").forGetter(a -> a.categories))
        .apply(inst, AttributeAffix::new));

    protected final Holder<Attribute> attribute;
    protected final Operation operation;
    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> categories;

    protected transient final Map<LootRarity, ModifierInst> modifiers;

    public AttributeAffix(AffixDefinition def, Holder<Attribute> attr, Operation op, Map<LootRarity, StepFunction> values, Set<LootCategory> categories) {
        super(def);
        this.attribute = attr;
        this.operation = op;
        this.values = values;
        this.categories = categories;
        this.modifiers = values.entrySet().stream().map(entry -> Pair.of(entry.getKey(), new ModifierInst(attr, op, entry.getValue()))).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return Component.empty();
    }

    @Override
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        ModifierInst modif = this.modifiers.get(inst.getRarity());
        double value = modif.valueFactory.get(inst.level());
        Attribute attr = this.attribute.value();

        MutableComponent comp;
        MutableComponent valueComp = attr.toValueComponent(this.operation, value, ctx.flag());
        ChatFormatting color = attr.getStyle(value > 0);

        if (value > 0.0D) {
            comp = Component.translatable("neoforge.modifier.plus", valueComp, Component.translatable(attr.getDescriptionId())).withStyle(color);
        }
        else {
            comp = Component.translatable("neoforge.modifier.take", valueComp, Component.translatable(attr.getDescriptionId())).withStyle(color);
        }

        if (modif.valueFactory.get(0) != modif.valueFactory.get(1)) {
            Component minComp = attr.toValueComponent(this.operation, modif.valueFactory.get(0), ctx.flag());
            Component maxComp = attr.toValueComponent(this.operation, modif.valueFactory.get(1), ctx.flag());
            comp.append(valueBounds(minComp, maxComp));
        }

        return comp;
    }

    @Override
    public void addModifiers(AffixInstance inst, StackAttributeModifiersEvent event) {
        LootCategory cat = inst.category();
        if (cat.isNone()) {
            Apotheosis.LOGGER.debug("Attempted to apply the attributes of affix {} on item {}, but it is not an affix-compatible item!", this.id(), inst.stack().getHoverName().getString());
            return;
        }
        ModifierInst modif = this.modifiers.get(inst.getRarity());
        if (modif.attr == null) {
            Apotheosis.LOGGER.debug("The affix {} has attempted to apply a null attribute modifier to {}!", this.id(), inst.stack().getHoverName().getString());
            return;
        }
        event.addModifier(this.attribute, modif.build(inst), cat.getSlots());
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) {
            return false;
        }
        return (this.categories.isEmpty() || this.categories.contains(cat)) && this.modifiers.containsKey(rarity);
    }

    @Override
    public void gatherModifierTooltips(AffixInstance inst, AttributeTooltipContext ctx, Consumer<Component> list) {
        ModifierInst modif = this.modifiers.get(inst.getRarity());
        Attribute attr = this.attribute.value();
        list.accept(attr.toComponent(modif.build(inst), ctx.flag()));
    }

    @Override
    public void skipModifierIds(AffixInstance inst, AttributeTooltipContext ctx, Consumer<ResourceLocation> skip) {
        if (ctx.player() != null && WorldTier.isTutorialActive(ctx.player())) {
            skip.accept(inst.makeUniqueId());
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

    public static record ModifierInst(Holder<Attribute> attr, Operation op, StepFunction valueFactory) {

        public AttributeModifier build(AffixInstance inst) {
            return new AttributeModifier(inst.makeUniqueId(), this.valueFactory.get(inst.level()), this.op);
        }
    }

    public static class Builder extends ValuedAffixBuilder<Builder> {
        protected final Holder<Attribute> attribute;
        protected final Operation operation;
        protected final Set<LootCategory> categories = new LinkedHashSet<>();

        public Builder(Holder<Attribute> attribute, Operation operation) {
            this.attribute = attribute;
            this.operation = operation;
        }

        public Builder categories(LootCategory... cats) {
            for (LootCategory cat : cats) {
                this.categories.add(cat);
            }
            return this;
        }

        public AttributeAffix build() {
            Preconditions.checkArgument(!this.values.isEmpty());
            return new AttributeAffix(this.definition, this.attribute, this.operation, this.values, this.categories);
        }
    }

}
