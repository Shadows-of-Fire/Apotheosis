package dev.shadowsoffire.apotheosis.tiers.augments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.Attachments;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.effect.DamageReductionAffix.DamageType;
import dev.shadowsoffire.apotheosis.attachments.DamageReductions;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

/**
 * Applies a damage reduction to the target entity via the {@link Attachments#DAMAGE_REDUCTIONS} attachment.
 */
public record DamageReductionAugment(WorldTier tier, Target target, int sortIndex, DamageType type, float amount, ResourceLocation id) implements TierAugment {

    public static final Codec<DamageReductionAugment> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            WorldTier.CODEC.fieldOf("tier").forGetter(TierAugment::tier),
            Target.CODEC.fieldOf("target").forGetter(TierAugment::target),
            Codec.intRange(0, 2000).optionalFieldOf("sort_index", 1000).forGetter(TierAugment::sortIndex),
            DamageType.CODEC.fieldOf("damage_type").forGetter(DamageReductionAugment::type),
            Codec.floatRange(0, 1).fieldOf("amount").forGetter(DamageReductionAugment::amount),
            ResourceLocation.CODEC.fieldOf("reduction_id").forGetter(DamageReductionAugment::id))
        .apply(inst, DamageReductionAugment::new));

    @Override
    public Codec<? extends DamageReductionAugment> getCodec() {
        return CODEC;
    }

    @Override
    public void apply(ServerLevelAccessor level, LivingEntity entity) {
        DamageReductions.Mutable mutable = new DamageReductions.Mutable(entity.getData(Attachments.DAMAGE_REDUCTIONS));
        mutable.set(this.type, this.id, this.amount);
        entity.setData(Attachments.DAMAGE_REDUCTIONS, mutable.toImmutable());
    }

    @Override
    public void remove(ServerLevelAccessor level, LivingEntity entity) {
        DamageReductions.Mutable mutable = new DamageReductions.Mutable(entity.getData(Attachments.DAMAGE_REDUCTIONS));
        mutable.remove(this.type, this.id);
        entity.setData(Attachments.DAMAGE_REDUCTIONS, mutable.toImmutable());
    }

    @Override
    public Component getDescription(AttributeTooltipContext ctx) {
        return Component.translatable("tier_augment.apotheosis.damage_reduction", Affix.fmt(100 * this.amount), Component.translatable("misc.apotheosis." + this.type.getSerializedName()));
    }

}
