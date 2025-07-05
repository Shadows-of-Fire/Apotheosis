package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.spongepowered.include.com.google.common.base.Preconditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixBuilder;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.affix.AttributeProvidingAffix;
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
 * An affix that applies multiple {@link AttributeModifier}s to a single item.
 */
public class MultiAttrAffix extends Affix implements AttributeProvidingAffix {

    public static final Codec<MultiAttrAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            ModifierInst.CODEC.listOf().fieldOf("modifiers").forGetter(a -> a.modifiers),
            Codec.STRING.fieldOf("desc").forGetter(a -> a.desc),
            LootCategory.SET_CODEC.fieldOf("categories").forGetter(a -> a.categories))
        .apply(inst, MultiAttrAffix::new));

    protected final List<ModifierInst> modifiers;
    protected final String desc;
    protected final Set<LootCategory> categories;

    protected transient final Set<LootRarity> rarities;

    public MultiAttrAffix(AffixDefinition def, List<ModifierInst> modifiers, String desc, Set<LootCategory> categories) {
        super(def);
        this.modifiers = modifiers;
        this.desc = desc;
        this.categories = categories;

        Set<LootRarity> rarities = new HashSet<>();

        for (int i = 0; i < modifiers.size(); i++) {
            ModifierInst inst = modifiers.get(i);
            if (rarities.isEmpty()) {
                rarities.addAll(inst.values.keySet());
            }

            // Each defined ModifierInst must have the same set of rarities.
            // We're not doing this thing where certain attribute modifiers disappear and reappear based on an item's rarity.
            if (!rarities.equals(inst.values.keySet())) {
                throw new IllegalArgumentException("Disjoint rarity sets at modifier index " + i + "! Specified set: " + rarities + " but found: " + inst.values.keySet());
            }
        }
        this.rarities = rarities;
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return Component.empty();
    }

    @Override
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        Object[] values = new Object[this.modifiers.size()];

        for (int i = 0; i < this.modifiers.size(); i++) {
            ModifierInst modif = this.modifiers.get(i);
            Attribute attr = modif.attr().value();
            MutableComponent comp = attr.toComponent(modif.build(inst, i), ctx.flag());
            StepFunction valueFactory = modif.values.get(inst.getRarity());

            if (valueFactory.get(0) != valueFactory.get(1)) {
                Component minComp = attr.toValueComponent(modif.op, valueFactory.get(0), ctx.flag());
                Component maxComp = attr.toValueComponent(modif.op, valueFactory.get(1), ctx.flag());
                comp.append(valueBounds(minComp, maxComp));
            }

            values[i] = comp;
        }

        return Component.translatable(this.desc, values).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public void addModifiers(AffixInstance inst, StackAttributeModifiersEvent event) {
        LootCategory cat = inst.category();
        if (cat.isNone()) {
            Apotheosis.LOGGER.debug("Attempted to apply the attributes of affix {} on item {}, but it is not an affix-compatible item!", this.id(), inst.stack().getHoverName().getString());
            return;
        }
        for (int i = 0; i < this.modifiers.size(); i++) {
            ModifierInst modif = this.modifiers.get(i);
            if (modif.attr == null) {
                Apotheosis.LOGGER.debug("The affix {} has attempted to apply a null attribute modifier to {}!", this.id(), inst.stack().getHoverName().getString());
                return;
            }
            event.addModifier(modif.attr(), modif.build(inst, i), cat.getSlots());
        }
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) {
            return false;
        }
        return (this.categories.isEmpty() || this.categories.contains(cat)) && this.rarities.contains(rarity);
    }

    @Override
    public void gatherModifierTooltips(AffixInstance inst, AttributeTooltipContext ctx, Consumer<Component> list) {
        for (int i = 0; i < this.modifiers.size(); i++) {
            ModifierInst modif = this.modifiers.get(i);
            Attribute attr = modif.attr.value();
            list.accept(attr.toComponent(modif.build(inst, i), ctx.flag()));
        }
    }

    @Override
    public void skipModifierIds(AffixInstance inst, AttributeTooltipContext ctx, Consumer<ResourceLocation> skip) {
        if (ctx.player() != null && WorldTier.isTutorialActive(ctx.player())) {
            for (int i = 0; i < this.modifiers.size(); i++) {
                skip.accept(inst.makeUniqueId("" + i));
            }
        }
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    @Override
    public boolean isLevelIndependent(AffixInstance inst) {
        for (ModifierInst modif : this.modifiers) {
            if (!modif.values.get(inst.getRarity()).isConstant()) {
                return false;
            }
        }
        return true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static record ModifierInst(Holder<Attribute> attr, Operation op, Map<LootRarity, StepFunction> values) {

        public static Codec<ModifierInst> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(ModifierInst::attr),
                PlaceboCodecs.enumCodec(Operation.class).fieldOf("operation").forGetter(ModifierInst::op),
                LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(ModifierInst::values))
            .apply(inst, ModifierInst::new));

        public AttributeModifier build(AffixInstance inst, int idx) {
            return new AttributeModifier(inst.makeUniqueId("" + idx), this.values.get(inst.getRarity()).get(inst.level()), this.op);
        }

        public static class Builder {
            private Holder<Attribute> attr;
            private Operation op;
            protected final Map<LootRarity, StepFunction> values = new HashMap<>();
            protected float step = 0.01F;

            public Builder attr(Holder<Attribute> attr) {
                this.attr = attr;
                return this;
            }

            public Builder op(Operation op) {
                this.op = op;
                return this;
            }

            public Builder step(float step) {
                this.step = step;
                return this;
            }

            public Builder value(LootRarity rarity, float min, float max) {
                return this.value(rarity, StepFunction.fromBounds(min, max, this.step));
            }

            public Builder value(LootRarity rarity, float value) {
                return this.value(rarity, StepFunction.constant(value));
            }

            public Builder value(LootRarity rarity, StepFunction function) {
                this.values.put(rarity, function);
                return this;
            }

            public ModifierInst build() {
                return new ModifierInst(this.attr, this.op, this.values);
            }
        }
    }

    public static class Builder extends AffixBuilder<Builder> {
        protected final Set<LootCategory> categories = new HashSet<>();
        protected final List<ModifierInst> modifiers = new ArrayList<>();
        protected String desc;

        public Builder modifier(UnaryOperator<ModifierInst.Builder> config) {
            this.modifiers.add(config.apply(new ModifierInst.Builder()).build());
            return this;
        }

        public Builder desc(String desc) {
            this.desc = desc;
            return this;
        }

        public Builder categories(LootCategory... cats) {
            for (LootCategory cat : cats) {
                this.categories.add(cat);
            }
            return this;
        }

        public MultiAttrAffix build() {
            Preconditions.checkArgument(!this.modifiers.isEmpty());
            Preconditions.checkArgument(this.desc != null);
            return new MultiAttrAffix(this.definition, this.modifiers, this.desc, this.categories);
        }
    }

}
