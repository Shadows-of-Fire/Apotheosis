package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.LootCategories;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Disengage
 */
public class RetreatingAffix extends Affix {

    public static final Codec<RetreatingAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            PlaceboCodecs.setOf(LootRarity.CODEC).fieldOf("rarities").forGetter(a -> a.rarities))
        .apply(inst, RetreatingAffix::new));

    protected Set<LootRarity> rarities;

    public RetreatingAffix(AffixDefinition def, Set<LootRarity> rarities) {
        super(def);
        this.rarities = rarities;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat == LootCategories.SHIELD && this.rarities.contains(rarity);
    }

    @Override
    public float onShieldBlock(AffixInstance inst, LivingEntity entity, DamageSource source, float amount) {
        Entity tSource = source.getEntity();
        if (tSource != null && tSource.distanceToSqr(entity) <= 9) {
            Vec3 look = entity.getLookAngle();
            entity.setDeltaMovement(new Vec3(1 * -look.x, 0.25, 1 * -look.z));
            entity.hurtMarked = true;
            entity.setOnGround(false);
        }
        return super.onShieldBlock(inst, entity, source, amount);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    @Override
    public boolean isLevelIndependent(AffixInstance inst) {
        return true;
    }

}
