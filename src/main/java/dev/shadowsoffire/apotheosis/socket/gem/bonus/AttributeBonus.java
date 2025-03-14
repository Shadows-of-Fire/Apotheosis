package dev.shadowsoffire.apotheosis.socket.gem.bonus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemView;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apothic_attributes.modifiers.StackAttributeModifiersEvent;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class AttributeBonus extends GemBonus {

    public static Codec<AttributeBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(a -> a.attribute),
            PlaceboCodecs.enumCodec(Operation.class).fieldOf("operation").forGetter(a -> a.operation),
            Purity.mapCodec(Codec.DOUBLE).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, AttributeBonus::new));

    protected final Holder<Attribute> attribute;
    protected final Operation operation;
    protected final Map<Purity, Double> values;

    public AttributeBonus(GemClass gemClass, Holder<Attribute> attr, Operation op, Map<Purity, Double> values) {
        super(gemClass);
        this.attribute = attr;
        this.operation = op;
        this.values = values;
    }

    @Override
    public void addModifiers(GemInstance gem, StackAttributeModifiersEvent event) {
        event.addModifier(this.attribute, this.createModifier(gem), gem.category().getSlots());
    }

    @Override
    public void skipModifierIds(GemInstance gem, Consumer<ResourceLocation> skip) {
        skip.accept(makeUniqueId(gem));
    }

    @Override
    public Component getSocketBonusTooltip(GemView gem, AttributeTooltipContext ctx) {
        return this.attribute.value().toComponent(this.createModifier(gem), ctx.flag());
    }

    @Override
    public boolean supports(Purity purity) {
        return this.values.containsKey(purity);
    }

    public AttributeModifier createModifier(GemView gem) {
        double value = this.values.get(gem.purity());
        return new AttributeModifier(makeUniqueId(gem), value, this.operation);
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends GemBonus.Builder {
        private final Map<Purity, Double> values;

        private Holder<Attribute> attribute;
        private Operation operation;

        private Builder() {
            this.values = new HashMap<>();
        }

        public Builder attr(Holder<Attribute> attribute) {
            this.attribute = attribute;
            return this;
        }

        public Builder op(Operation operation) {
            this.operation = operation;
            return this;
        }

        public Builder value(Purity purity, double value) {
            this.values.put(purity, value);
            return this;
        }

        @Override
        public AttributeBonus build(GemClass gClass) {
            return new AttributeBonus(gClass, this.attribute, this.operation, this.values);
        }
    }

}
