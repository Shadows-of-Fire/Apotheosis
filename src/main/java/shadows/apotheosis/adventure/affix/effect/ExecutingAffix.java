package shadows.apotheosis.adventure.affix.effect;

import java.util.Map;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.affix.socket.gem.bonus.GemBonus;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.mixin.LivingEntityInvoker;
import shadows.placebo.json.PSerializer;
import shadows.placebo.util.StepFunction;

public class ExecutingAffix extends Affix {

    public static final Codec<ExecutingAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            GemBonus.VALUES_CODEC.fieldOf("values").forGetter(a -> a.values))
        .apply(inst, ExecutingAffix::new));

    public static final PSerializer<ExecutingAffix> SERIALIZER = PSerializer.fromCodec("Executing Affix", CODEC);

    protected final Map<LootRarity, StepFunction> values;

    public ExecutingAffix(Map<LootRarity, StepFunction> values) {
        super(AffixType.ABILITY);
        this.values = values;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat == LootCategory.HEAVY_WEAPON && this.values.containsKey(rarity);
    }

    @Override
    public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
        list.accept(Component.translatable("affix." + this.getId() + ".desc", fmt(100 * this.getTrueLevel(rarity, level))));
    }

    private float getTrueLevel(LootRarity rarity, float level) {
        return this.values.get(rarity).get(level);
    }

    @Override
    public void doPostAttack(ItemStack stack, LootRarity rarity, float level, LivingEntity user, Entity target) {
        float threshold = this.getTrueLevel(rarity, level);
        if (Apotheosis.localAtkStrength >= 0.98 && target instanceof LivingEntity living && !living.level.isClientSide) {
            if (living.getHealth() / living.getMaxHealth() < threshold) {
                DamageSource src = new EntityDamageSource("apotheosis.execute", user).bypassArmor().bypassMagic();
                if (!((LivingEntityInvoker) living).callCheckTotemDeathProtection(src)) {
                    SoundEvent soundevent = ((LivingEntityInvoker) living).callGetDeathSound();
                    if (soundevent != null) {
                        living.playSound(soundevent, ((LivingEntityInvoker) living).callGetSoundVolume(), living.getVoicePitch());
                    }

                    living.setHealth(0);
                    living.die(src);
                }
            }
        }
    }

    @Override
    public PSerializer<? extends Affix> getSerializer() {
        return SERIALIZER;
    }

}
