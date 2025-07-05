package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spongepowered.include.com.google.common.base.Preconditions;

import com.google.common.base.Predicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixBuilder;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apothic_attributes.ApothicAttributes;
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
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class CleavingAffix extends Affix {

    public static final Codec<CleavingAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            LootRarity.mapCodec(CleaveValues.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, CleavingAffix::new));

    protected final Map<LootRarity, CleaveValues> values;

    private static boolean cleaving = false;

    public CleavingAffix(AffixDefinition def, Map<LootRarity, CleaveValues> values) {
        super(def);
        this.values = values;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat.isMelee() && this.values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return Component.translatable("affix." + this.id() + ".desc", fmt(100 * this.getChance(inst.getRarity(), inst.level())), this.getTargets(inst.getRarity(), inst.level()));
    }

    @Override
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        MutableComponent comp = this.getDescription(inst, ctx);
        LootRarity rarity = inst.getRarity();

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
    public void doPostAttack(AffixInstance inst, LivingEntity user, Entity target) {
        if (ApothicAttributes.getLocalAtkStrength(user) >= 0.98 && !cleaving && !user.level().isClientSide) {
            cleaving = true;
            float chance = this.getChance(inst.getRarity(), inst.level());
            int targets = this.getTargets(inst.getRarity(), inst.level());
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

    @Override
    public boolean isLevelIndependent(AffixInstance inst) {
        return this.values.get(inst.getRarity()).isConstant();
    }

    public static Predicate<Entity> cleavePredicate(Entity user, Entity target) {
        return e -> {
            if (e instanceof Animal && !(target instanceof Animal) || e instanceof AbstractVillager && !(target instanceof AbstractVillager)) {
                return false;
            }
            if ((!AdventureConfig.cleaveHitsPlayers && e instanceof Player) || (target instanceof Enemy && !(e instanceof Enemy))) {
                return false;
            }
            return e != user && e instanceof LivingEntity le && le.isAlive();
        };
    }

    static record CleaveValues(StepFunction chance, StepFunction targets) {

        public static final Codec<CleaveValues> CODEC = RecordCodecBuilder
            .create(inst -> inst.group(StepFunction.CODEC.fieldOf("chance").forGetter(c -> c.chance), StepFunction.CODEC.fieldOf("targets").forGetter(c -> c.targets)).apply(inst, CleaveValues::new));

        public boolean isConstant() {
            return this.chance.isConstant() && this.targets.isConstant();
        }
    }

    public static class Builder extends AffixBuilder<Builder> {

        protected final Map<LootRarity, CleaveValues> values = new HashMap<>();

        public Builder value(LootRarity rarity, float minChance, float maxChance, int minTargets, int maxTargets) {
            StepFunction chance = StepFunction.fromBounds(minChance, maxChance, 0.05F);
            StepFunction targets = StepFunction.fromBounds(minTargets, maxTargets, 1);
            this.values.put(rarity, new CleaveValues(chance, targets));
            return this;
        }

        public CleavingAffix build() {
            Preconditions.checkNotNull(this.definition);
            Preconditions.checkArgument(this.values.size() > 0);
            return new CleavingAffix(this.definition, this.values);
        }
    }

}
