package dev.shadowsoffire.apotheosis.adventure.affix.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.Affixes;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Loot Pinata
 */
public class FestiveAffix extends Affix {

    public static Codec<FestiveAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            GemBonus.VALUES_CODEC.fieldOf("values").forGetter(a -> a.values))
        .apply(inst, FestiveAffix::new));

    protected final Map<LootRarity, StepFunction> values;

    public FestiveAffix(Map<LootRarity, StepFunction> values) {
        super(AffixType.ABILITY);
        this.values = values;
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        return Component.translatable("affix." + this.getId() + ".desc", fmt(100 * this.getTrueLevel(rarity, level)));
    }

    @Override
    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float level) {
        MutableComponent comp = this.getDescription(stack, rarity, level);

        Component minComp = Component.translatable("%s%%", fmt(100 * this.getTrueLevel(rarity, 0)));
        Component maxComp = Component.translatable("%s%%", fmt(100 * this.getTrueLevel(rarity, 1)));
        return comp.append(valueBounds(minComp, maxComp));
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat.isLightWeapon() && this.values.containsKey(rarity);
    }

    private float getTrueLevel(LootRarity rarity, float level) {
        return this.values.get(rarity).get(level);
    }

    private static String MARKER = "apoth.equipment";

    // EventPriority.LOW
    public void markEquipment(LivingDeathEvent e) {
        if (e.getEntity() instanceof Player || e.getEntity().getPersistentData().getBoolean("apoth.no_pinata")) return;

        IItemHandler inv = e.getEntity().getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        if (inv instanceof IItemHandlerModifiable iihm) {
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    stack.getOrCreateTag().putBoolean(MARKER, true);
                    iihm.setStackInSlot(i, stack);
                }
            }
        }

        e.getEntity().getAllSlots().forEach(i -> {
            if (!i.isEmpty()) i.getOrCreateTag().putBoolean(MARKER, true);
        });
    }

    // EventPriority.LOW
    public void drops(LivingDropsEvent e) {
        LivingEntity dead = e.getEntity();
        if (dead instanceof Player || dead.getPersistentData().getBoolean("apoth.no_pinata")) return;
        if (e.getSource().getEntity() instanceof Player player && !e.getDrops().isEmpty()) {
            AffixInstance inst = AffixHelper.getAffixes(player.getMainHandItem()).get(Affixes.FESTIVE);
            if (inst != null && inst.isValid() && player.level().random.nextFloat() < this.getTrueLevel(inst.rarity().get(), inst.level())) {

                player.level().playSound(null, dead.getX(), dead.getY(), dead.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F,
                    (1.0F + (player.level().random.nextFloat() - player.level().random.nextFloat()) * 0.2F) * 0.7F);
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.EXPLOSION_EMITTER, dead.getX(), dead.getY(), dead.getZ(), 2, 1.0D, 0.0D, 0.0D, 0);

                List<ItemEntity> drops = new ArrayList<>(e.getDrops());
                for (ItemEntity item : drops) {
                    if (item.getItem().hasTag() && item.getItem().getTag().contains(MARKER)) continue;
                    for (int i = 0; i < 20; i++) {
                        e.getDrops().add(new ItemEntity(player.level(), item.getX(), item.getY(), item.getZ(), item.getItem().copy()));
                    }
                }

                for (ItemEntity item : e.getDrops()) {
                    if (!item.getItem().getItem().canBeDepleted()) {
                        item.setPos(dead.getX(), dead.getY(), dead.getZ());
                        item.setDeltaMovement(-0.3 + dead.level().random.nextDouble() * 0.6, 0.3 + dead.level().random.nextDouble() * 0.3, -0.3 + dead.level().random.nextDouble() * 0.6);
                    }
                }
            }
        }
    }

    // Lowest prio + receive cancelled
    public void removeMarker(LivingDropsEvent e) {
        e.getDrops().stream().forEach(ent -> {
            ItemStack s = ent.getItem();
            if (s.hasTag() && s.getTag().contains(MARKER)) {
                s.getTag().remove(MARKER);
                if (s.getTag().isEmpty()) s.setTag(null);
            }
            ent.setItem(s);
        });
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}
