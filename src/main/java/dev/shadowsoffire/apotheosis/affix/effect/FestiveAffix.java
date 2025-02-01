package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/**
 * Loot Pinata
 */
public class FestiveAffix extends Affix {

    public static Codec<FestiveAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, FestiveAffix::new));

    protected final Map<LootRarity, StepFunction> values;

    public FestiveAffix(AffixDefinition def, Map<LootRarity, StepFunction> values) {
        super(def);
        this.values = values;
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return Component.translatable("affix." + this.id() + ".desc", fmt(100 * this.getTrueLevel(inst.getRarity(), inst.level())));
    }

    @Override
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        MutableComponent comp = this.getDescription(inst, ctx);

        Component minComp = Component.translatable("%s%%", fmt(100 * this.getTrueLevel(inst.getRarity(), 0)));
        Component maxComp = Component.translatable("%s%%", fmt(100 * this.getTrueLevel(inst.getRarity(), 1)));
        return comp.append(valueBounds(minComp, maxComp));
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat.isMelee() && this.values.containsKey(rarity);
    }

    private float getTrueLevel(LootRarity rarity, float level) {
        return this.values.get(rarity).get(level);
    }

    // EventPriority.LOW
    public static void markEquipment(LivingDeathEvent e) {
        if (e.getEntity() instanceof Player || e.getEntity().getPersistentData().getBoolean("apoth.no_pinata")) return;

        IItemHandler inv = e.getEntity().getCapability(Capabilities.ItemHandler.ENTITY);

        if (inv instanceof IItemHandlerModifiable iihm) {
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    stack.set(Components.FESTIVE_MARKER, true);
                    iihm.setStackInSlot(i, stack);
                }
            }
        }

        e.getEntity().getAllSlots().forEach(i -> {
            if (!i.isEmpty()) {
                i.set(Components.FESTIVE_MARKER, true);
            }
        });
    }

    @Override
    public void modifyEntityLoot(AffixInstance inst, LivingDropsEvent e) {
        LivingEntity dead = e.getEntity();
        if (dead instanceof Player || dead.getPersistentData().getBoolean("apoth.no_pinata")) return;
        if (e.getSource().getEntity() instanceof Player player && !e.getDrops().isEmpty()) {
            if (inst != null && inst.isValid() && player.level().random.nextFloat() < this.getTrueLevel(inst.rarity().get(), inst.level())) {

                player.level().playSound(null, dead.getX(), dead.getY(), dead.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F,
                    (1.0F + (player.level().random.nextFloat() - player.level().random.nextFloat()) * 0.2F) * 0.7F);
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.EXPLOSION_EMITTER, dead.getX(), dead.getY(), dead.getZ(), 2, 1.0D, 0.0D, 0.0D, 0);

                List<ItemEntity> drops = new ArrayList<>(e.getDrops());
                for (ItemEntity item : drops) {
                    if (item.getItem().getOrDefault(Components.FESTIVE_MARKER, false)) {
                        continue;
                    }
                    for (int i = 0; i < 20; i++) {
                        e.getDrops().add(new ItemEntity(player.level(), item.getX(), item.getY(), item.getZ(), item.getItem().copy()));
                    }
                }

                for (ItemEntity item : e.getDrops()) {
                    item.setPos(dead.getX(), dead.getY(), dead.getZ());
                    item.setDeltaMovement(-0.3 + dead.level().random.nextDouble() * 0.6, 0.3 + dead.level().random.nextDouble() * 0.3, -0.3 + dead.level().random.nextDouble() * 0.6);
                }
            }
        }
    }

    // Lowest prio + receive cancelled
    public static void removeMarker(LivingDropsEvent e) {
        e.getDrops().stream().forEach(ent -> {
            ItemStack s = ent.getItem();
            s.remove(Components.FESTIVE_MARKER);
            ent.setItem(s);
        });
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}
