package dev.shadowsoffire.apotheosis.adventure.affix.effect;

import java.util.Map;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.json.PSerializer;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;

/**
 * When blocking an arrow, hurt the shooter.
 */
public class PsychicAffix extends Affix {

    public static final Codec<PsychicAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            GemBonus.VALUES_CODEC.fieldOf("values").forGetter(a -> a.values))
        .apply(inst, PsychicAffix::new));

    public static final PSerializer<PsychicAffix> SERIALIZER = PSerializer.fromCodec("Psychic Affix", CODEC);

    protected final Map<LootRarity, StepFunction> values;

    public PsychicAffix(Map<LootRarity, StepFunction> values) {
        super(AffixType.ABILITY);
        this.values = values;
    }

    @Override
    public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
        list.accept(Component.translatable("affix." + this.getId() + ".desc", fmt(100 * this.getTrueLevel(rarity, level))));
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat == LootCategory.SHIELD && this.values.containsKey(rarity);
    }

    @Override
    public float onShieldBlock(ItemStack stack, LootRarity rarity, float level, LivingEntity entity, DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof Projectile arrow) {
            Entity owner = arrow.getOwner();
            if (owner instanceof LivingEntity living) {
                living.hurt(entity.damageSources().source(Apoth.DamageTypes.PSYCHIC, entity), amount * this.getTrueLevel(rarity, level));
            }
        }

        return super.onShieldBlock(stack, rarity, level, entity, source, amount);
    }

    private float getTrueLevel(LootRarity rarity, float level) {
        return this.values.get(rarity).get(level);
    }

    @Override
    public PSerializer<? extends Affix> getSerializer() {
        return SERIALIZER;
    }

}
