package dev.shadowsoffire.apotheosis.adventure.affix.effect;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.base.Predicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import dev.shadowsoffire.placebo.json.PSerializer;
import dev.shadowsoffire.placebo.util.StepFunction;

public class CleavingAffix extends Affix {

    public static final Codec<CleavingAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            LootRarity.mapCodec(CleaveValues.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, CleavingAffix::new));

    public static final PSerializer<CleavingAffix> SERIALIZER = PSerializer.fromCodec("Cleaving Affix", CODEC);

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
    public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
        list.accept(Component.translatable("affix." + this.getId() + ".desc", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(100 * this.getChance(rarity, level)), this.getTargets(rarity, level)).withStyle(ChatFormatting.YELLOW));
    }

    private float getChance(LootRarity rarity, float level) {
        return this.values.get(rarity).chance.get(level);
    }

    private int getTargets(LootRarity rarity, float level) {
        // We want targets to sort of be separate from chance, so we modulo and double.
        level %= 0.5F;
        level *= 2;
        return (int) this.values.get(rarity).targets.get(level);
    }

    @Override
    public void doPostAttack(ItemStack stack, LootRarity rarity, float level, LivingEntity user, Entity target) {
        if (Apotheosis.localAtkStrength >= 0.98 && !cleaving && !user.level().isClientSide) {
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
    public PSerializer<? extends Affix> getSerializer() {
        return SERIALIZER;
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
