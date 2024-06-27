package dev.shadowsoffire.apotheosis.adventure.affix.effect;

import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class CleavingAffix extends Affix {

    public static final Codec<CleavingAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            LootRarity.mapCodec(CleaveValues.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, CleavingAffix::new));

    protected final Map<LootRarity, CleaveValues> values;

    private static boolean cleaving = false;

    public CleavingAffix(Map<LootRarity, CleaveValues> values) {
        super(AffixType.ABILITY);
        this.values = values;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat == LootCategory.HEAVY_WEAPON && this.values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        return Component.translatable("affix." + this.getId() + ".desc", fmt(100 * this.getChance(rarity, level)), this.getTargets(rarity, level));
    }

    @Override
    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float level) {
        MutableComponent comp = this.getDescription(stack, rarity, level);

        float minChance = this.getChance(rarity, 0);
        float maxChance = this.getChance(rarity, 1);
        if (minChance != maxChance) {
            Component minComp = Component.translatable("%s%%", fmt(100 * minChance));
            Component maxComp = Component.translatable("%s%%", fmt(100 * maxChance));
            comp.append(valueBounds(minComp, maxComp));
        }

        int minTargets = this.getTargets(rarity, 0);
        int maxTargets = this.getTargets(rarity, 1);
        if (minTargets != maxTargets) {
            Component minComp = Component.literal(fmt(minTargets));
            Component maxComp = Component.literal(fmt(maxTargets));
            return comp.append(valueBounds(minComp, maxComp));
        }

        return comp;
    }

    private float getChance(LootRarity rarity, float level) {
        return this.values.get(rarity).chance.get(level);
    }

    private int getTargets(LootRarity rarity, float level) {
        return this.values.get(rarity).targets.getInt(level);
    }

    @Override
    public void doPostAttack(ItemStack stack, LootRarity rarity, float level, LivingEntity user, Entity target) {
        if (Apotheosis.getLocalAtkStrength(user) >= 0.98 && !cleaving && !user.level().isClientSide) {
            cleaving = true;
            float chance = this.getChance(rarity, level);
            int targets = this.getTargets(rarity, level);
            if (user.level().random.nextFloat() < chance && user instanceof Player player) {
                List<Entity> nearby = target.level().getEntities(target, new AABB(target.blockPosition()).inflate(6), cleavePredicate(user, target));
                for (Entity e : nearby) {
                    if (targets > 0) {
                        user.attackStrengthTicker = 300;
                        player.attack(e);
                        targets--;
                    }
                }
            }
            cleaving = false;
        }
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    public static Predicate<Entity> cleavePredicate(Entity user, Entity target) {
        return e -> {
            if (e instanceof Animal && !(target instanceof Animal) || e instanceof AbstractVillager && !(target instanceof AbstractVillager)) return false;
            if (!AdventureConfig.cleaveHitsPlayers && e instanceof Player) return false;
            if (target instanceof Enemy && !(e instanceof Enemy)) return false;
            return e != user && e instanceof LivingEntity le && le.isAlive();
        };
    }

    static record CleaveValues(StepFunction chance, StepFunction targets) {

        public static final Codec<CleaveValues> CODEC = RecordCodecBuilder
            .create(inst -> inst.group(StepFunction.CODEC.fieldOf("chance").forGetter(c -> c.chance), StepFunction.CODEC.fieldOf("targets").forGetter(c -> c.targets)).apply(inst, CleaveValues::new));

    }

}
