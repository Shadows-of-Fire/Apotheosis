package dev.shadowsoffire.apotheosis.adventure.affix.effect;

import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Damage Chain
 */
public class ThunderstruckAffix extends Affix {

    public static final Codec<ThunderstruckAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            GemBonus.VALUES_CODEC.fieldOf("values").forGetter(a -> a.values))
        .apply(inst, ThunderstruckAffix::new));

    protected final Map<LootRarity, StepFunction> values;

    public ThunderstruckAffix(Map<LootRarity, StepFunction> values) {
        super(AffixType.ABILITY);
        this.values = values;
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        return Component.translatable("affix." + this.getId() + ".desc", fmt(this.getTrueLevel(rarity, level)));
    }

    @Override
    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float level) {
        MutableComponent comp = this.getDescription(stack, rarity, level);

        Component minComp = Component.literal(fmt(this.getTrueLevel(rarity, 0)));
        Component maxComp = Component.literal(fmt(this.getTrueLevel(rarity, 1)));
        return comp.append(valueBounds(minComp, maxComp));
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat.isLightWeapon() && this.values.containsKey(rarity);
    }

    @Override
    public void doPostAttack(ItemStack stack, LootRarity rarity, float level, LivingEntity user, Entity target) {
        if (user.level().isClientSide) return;
        if (Apotheosis.getLocalAtkStrength(user) >= 0.98) {
            List<Entity> nearby = target.level().getEntities(target, new AABB(target.blockPosition()).inflate(6), CleavingAffix.cleavePredicate(user, target));
            for (Entity e : nearby) {
                e.hurt(user.damageSources().mobAttack(user), this.getTrueLevel(rarity, level));
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

}
