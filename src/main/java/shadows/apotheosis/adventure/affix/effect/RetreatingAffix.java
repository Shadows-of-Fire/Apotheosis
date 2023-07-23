package shadows.apotheosis.adventure.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.json.PSerializer;

/**
 * Disengage
 */
public class RetreatingAffix extends Affix {

    
    public static final Codec<RetreatingAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            LootRarity.CODEC.fieldOf("min_rarity").forGetter(a -> a.minRarity))
        .apply(inst, RetreatingAffix::new));
    
    public static final PSerializer<RetreatingAffix> SERIALIZER = PSerializer.fromCodec("Retreating Affix", CODEC);

    protected LootRarity minRarity;

    public RetreatingAffix(LootRarity minRarity) {
        super(AffixType.ABILITY);
        this.minRarity = minRarity;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat == LootCategory.SHIELD && rarity.isAtLeast(minRarity);
    }

    @Override
    public float onShieldBlock(ItemStack stack, LootRarity rarity, float level, LivingEntity entity, DamageSource source, float amount) {
        Entity tSource = source.getEntity();
        if (tSource != null && tSource.distanceToSqr(entity) <= 9) {
            Vec3 look = entity.getLookAngle();
            entity.setDeltaMovement(new Vec3(1 * -look.x, 0.25, 1 * -look.z));
            entity.hurtMarked = true;
            entity.setOnGround(false);
        }
        return super.onShieldBlock(stack, rarity, level, entity, source, amount);
    }

    @Override
    public PSerializer<? extends Affix> getSerializer() {
        return SERIALIZER;
    }

}
