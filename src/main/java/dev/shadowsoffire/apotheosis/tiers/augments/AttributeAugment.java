package dev.shadowsoffire.apotheosis.tiers.augments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.placebo.json.RandomAttributeModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

/**
 * Applies an attribute modifier to the target entity.
 * <p>
 * The modifier will be ignored if the entity does not have the attribute.
 */
public record AttributeAugment(WorldTier tier, Target target, int sortIndex, RandomAttributeModifier modifier) implements TierAugment {

    public static final Codec<AttributeAugment> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            WorldTier.CODEC.fieldOf("tier").forGetter(TierAugment::tier),
            Target.CODEC.fieldOf("target").forGetter(TierAugment::target),
            Codec.intRange(0, 2000).optionalFieldOf("sort_index", 1000).forGetter(TierAugment::sortIndex),
            RandomAttributeModifier.CODEC.fieldOf("modifier").forGetter(AttributeAugment::modifier))
        .apply(inst, AttributeAugment::new));

    @Override
    public Codec<? extends AttributeAugment> getCodec() {
        return CODEC;
    }

    @Override
    public void apply(ServerLevelAccessor level, LivingEntity entity) {
        AttributeInstance inst = entity.getAttribute(this.modifier.attribute());
        if (inst != null) {
            AttributeModifier modif = this.modifier.createDeterministic();
            inst.addOrReplacePermanentModifier(modif);
        }
    }

    @Override
    public void remove(ServerLevelAccessor level, LivingEntity entity) {
        AttributeInstance inst = entity.getAttribute(this.modifier.attribute());
        if (inst != null) {
            inst.removeModifier(this.modifier.modifierId());
        }
    }

    @Override
    public Component getDescription(AttributeTooltipContext ctx) {
        AttributeModifier modif = this.modifier.createDeterministic();
        return this.modifier.attribute().value().toComponent(modif, ctx.flag());
    }

}
