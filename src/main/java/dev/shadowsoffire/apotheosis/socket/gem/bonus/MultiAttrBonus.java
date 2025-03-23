package dev.shadowsoffire.apotheosis.socket.gem.bonus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemView;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apothic_attributes.modifiers.StackAttributeModifiersEvent;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class MultiAttrBonus extends GemBonus {

    public static Codec<MultiAttrBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            ModifierInst.CODEC.listOf().fieldOf("modifiers").forGetter(a -> a.modifiers),
            Codec.STRING.fieldOf("desc").forGetter(a -> a.desc))
        .apply(inst, MultiAttrBonus::new));

    protected final List<ModifierInst> modifiers;
    protected final String desc;

    public MultiAttrBonus(GemClass gemClass, List<ModifierInst> modifiers, String desc) {
        super(gemClass);
        this.modifiers = modifiers;
        this.desc = desc;
    }

    @Override
    public void addModifiers(GemInstance inst, StackAttributeModifiersEvent event) {
        int i = 0;
        for (ModifierInst modifier : this.modifiers) {
            event.addModifier(modifier.attr, modifier.build(makeUniqueId(inst, String.valueOf(i++)), inst.purity()), inst.category().getSlots());
        }
    }

    @Override
    public void skipModifierIds(GemInstance inst, Consumer<ResourceLocation> skip) {
        for (int i = 0; i < this.modifiers.size(); i++) {
            skip.accept(makeUniqueId(inst, String.valueOf(i)));
        }
    }

    @Override
    public Component getSocketBonusTooltip(GemView inst, AttributeTooltipContext ctx) {
        Object[] values = new Object[this.modifiers.size() * 2];
        int i = 0;
        for (ModifierInst modifier : this.modifiers) {
            values[i] = modifier.attr.value().toComponent(modifier.build(makeUniqueId(inst, "" + i), inst.purity()), ctx.flag());
            values[this.modifiers.size() + i] = modifier.attr.value().toValueComponent(modifier.op, i, ctx.flag());
            i++;
        }
        return Component.translatable(this.desc, values).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public boolean supports(Purity purity) {
        return this.modifiers.get(0).values.containsKey(purity);
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static record ModifierInst(Holder<Attribute> attr, Operation op, Map<Purity, Float> values) {

        public static Codec<ModifierInst> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(ModifierInst::attr),
                PlaceboCodecs.enumCodec(Operation.class).fieldOf("operation").forGetter(ModifierInst::op),
                Purity.mapCodec(Codec.FLOAT).fieldOf("values").forGetter(ModifierInst::values))
            .apply(inst, ModifierInst::new));

        public AttributeModifier build(ResourceLocation id, Purity purity) {
            return new AttributeModifier(id, this.values.get(purity), this.op);
        }

        public static class Builder {
            private Holder<Attribute> attr;
            private Operation op;
            private Map<Purity, Float> values = new HashMap<>();

            public Builder attr(Holder<Attribute> attr) {
                this.attr = attr;
                return this;
            }

            public Builder op(Operation op) {
                this.op = op;
                return this;
            }

            public Builder value(Purity purity, float value) {
                this.values.put(purity, value);
                return this;
            }

            public ModifierInst build() {
                return new ModifierInst(this.attr, this.op, this.values);
            }
        }

    }

    public static class Builder extends GemBonus.Builder {
        private List<ModifierInst> modifiers;
        private String desc;

        public Builder() {
            this.modifiers = new ArrayList<>();
        }

        public Builder modifier(UnaryOperator<ModifierInst.Builder> config) {
            this.modifiers.add(config.apply(new ModifierInst.Builder()).build());
            return this;
        }

        public Builder desc(String desc) {
            this.desc = desc;
            return this;
        }

        @Override
        public MultiAttrBonus build(GemClass gemClass) {
            return new MultiAttrBonus(gemClass, this.modifiers, this.desc);
        }
    }

}
